package com.camatica.camaticacontrol;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.opencv.core.CvType.CV_8UC4;

/**
 * Criado por Egon Soares em 29/10/2017
 */

class Calibrador {
    private static final String TAG = "MYAPP::OPENCV";

    Scalar colorUpper = new Scalar(0, 0, 0);
    Scalar colorLower = new Scalar(255, 255, 255);
    private boolean feito = false;

    private BluetoothSocket btSocket = null;
    //SPP UUID. Look for it

    private Mat inv;
    private Mat invGray;
    private Mat lowHsv;
    private Mat thresh;
    private Mat mHierarchy;
    private Mat cropped;

    private int tempo = 0; 
    int calib = 0;
    private final int[][] rects = {{15,25,15,25}, {15,25,65,75}, {65,75,15,25}, {65,75,65,75}};
    private final int[][] quadrantes = {{0,50,0,50}, {50,100,0,50}, {0,50,50,100}, {50,100,50,100}};
    private Scalar lowerB = new Scalar(0,0,0), upperB = new Scalar(255,255,255);
    private double lastScore = 0;


    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            if(!feito) {
                feito = true;
                try {
                    if (btSocket == null) {
                        BluetoothAdapter myBluetooth = BluetoothAdapter.getDefaultAdapter();
                        BluetoothDevice dispositivo = null;
                        Set<BluetoothDevice> pairedDevices = myBluetooth.getBondedDevices();
                        if (pairedDevices.size() > 0) {
                            for (BluetoothDevice device : pairedDevices) {
                                if (device.getAddress().equals("98:D3:32:30:A2:81")) {
                                    dispositivo = device;
                                    Log.d("MYAPPBT", "Good");
                                    break;
                                }
                            }
                        }
                        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard //SerialPortService ID
                        btSocket = dispositivo.createRfcommSocketToServiceRecord(uuid);
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        btSocket.connect();
                    }
                } catch (IOException e) {
                    Log.d("MYAPPBT", "Falhou", e);
                }
                return null;
            }
            else{return null;}
        }
    }

    void initMats(){
        inv = new Mat();
        invGray = new Mat();
        lowHsv = new Mat();
        thresh = new Mat();
        mHierarchy = new Mat();
        cropped = new Mat();
        new ConnectBT().doInBackground();
        Log.d("MYAPPBluetooth", "Chamei");
    }
    Mat calibrar(Mat input, Mat inputGray){

            Core.flip(input,inv,1);
            Core.flip(inputGray,invGray,1);


        // Recriar a imagem em HSV
        Imgproc.cvtColor(inv,lowHsv,Imgproc.COLOR_RGB2HSV_FULL);
        Imgproc.cvtColor(invGray,invGray,Imgproc.COLOR_GRAY2RGBA);
        
        //Calibração
            Log.d(TAG,"Calibrar");

            //Esperar um tempo
            if(tempo<90) {
                tempo++;
                int h = inv.height(); // Altura da imagem invertida
                int w = inv.width(); // Largura da imagem invertida
                Imgproc.putText(invGray, "Preencha o quadrado com a cor",
                new Point(5 * w / 100, 4 * h / 10),
                        0, 1.2, Scalar.all(255), 2); // Título de ordem
                Imgproc.putText(invGray, "Calibrando em " + ((90-tempo)/30 + 1),
                        new Point(2 * w / 10, 6 * h / 10),
                        0, 1.2, Scalar.all(255), 2); // Título de ordem
                Rect roi = new Rect(new Point(rects[calib][0]*w/100, rects[calib][2]*h/100),
                        new Point(rects[calib][1]*w/100,rects[calib][3]*h/100));
                cropped = Mat.zeros(invGray.size(),CV_8UC4);
                cropped.submat(roi).setTo(Scalar.all(255));
                inv.copyTo(invGray,cropped);

                return invGray;
            }
            else {
                //variables
                int h = lowHsv.height(); // Altura da imagem de baixa resolução
                int w = lowHsv.width(); // largura da imagem de baixa resolução
                int optElement = 0; // Componente que está sendo otimizado
                int loops = 0; // Loops executados

                //loop de maximização
                while(optElement < 6 && loops < 1000) {

                    // Modificar elemento a ser otimizado
                    if(optElement > 2) { // elemento upper
                        int vOptElement = optElement - 3; // adaptar para upper

                        //verificar se não ultrapassa o limite
                        if(upperB.val[vOptElement] > lowerB.val[vOptElement] + 6){
                            upperB.val[vOptElement] -= 5;
                        }
                        else {
                            optElement++;
                            continue;
                        }
                    }
                    else { // elemento lower
                        //verificar se não ultrapassa o limite
                        if(lowerB.val[optElement] + 5 < 255) {
                            lowerB.val[optElement] += 5;
                        }
                        else {
                            optElement++;
                            continue;
                        }
                    }

                    // Criar região para cortar a matriz
                    Rect roi = new Rect(new Point(rects[calib][0]*w/100, rects[calib][2]*h/100),
                            new Point(rects[calib][1]*w/100,rects[calib][3]*h/100));

                    cropped = new Mat(lowHsv,roi);

                    // Processar o threshold
                    Core.inRange(cropped, lowerB, upperB, thresh);

                    // Somar os elementos para cada canal
                    Scalar scoreScalar = Core.sumElems(thresh);

                    // Calcular pontuação
                    double score = 0;
                    for(double d : scoreScalar.val) score += d;

                    // Verificar se a pontuação caiu
                    if (lastScore * 0.97 > score || score == 0.0) {
                        if(optElement > 2) {
                            upperB.val[optElement - 3] += 5;
                        }
                        else {
                            lowerB.val[optElement] -= 5;
                        }
                        optElement++;
                        lastScore = 0;
                    }
                    else {
                        lastScore = score;
                        loops++;
                    }
                }

                double[] aux = colorLower.val;
                // Copiar valores para os thresholds
                for(int i = 0; i < 3; i++) aux[i] = Math.min(lowerB.val[i],
                        aux[i]);
                colorLower = new Scalar(aux);
                aux = colorUpper.val;
                for(int i = 0; i < 3; i++) aux[i] = Math.max(upperB.val[i],
                        aux[i]);
                colorUpper = new Scalar(aux);
                lowerB = new Scalar(0,0,0);
                upperB = new Scalar(255,255,255);
                calib++;
                tempo = 0;
                return invGray;
            
        }
    }
    Mat processar(Mat input, Mat inputGray) {


            // Inverter imagem
            Core.flip(input,inv,1);
            Core.flip(inputGray,invGray,1);


        // Recriar a imagem em HSV
        Imgproc.cvtColor(inv,lowHsv,Imgproc.COLOR_RGB2HSV_FULL);
        Imgproc.cvtColor(invGray,invGray,Imgproc.COLOR_GRAY2RGBA);
        // Processar Threshold
        Core.inRange(lowHsv, colorLower, colorUpper, thresh);

        // Erodir e dilatar
        Imgproc.dilate(thresh,thresh, new Mat());
        Imgproc.erode(thresh,thresh, new Mat());

        // Lista de contornos
        List<MatOfPoint> contours = new ArrayList<>();
        // Encontrar contornos
        Imgproc.findContours(thresh.clone(),contours, mHierarchy, Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE);
        //Imgproc.drawContours(inv,contours,-1,new Scalar(255,0,0,255));
        Moments M = new Moments();
        // Só continue se encontrou algum contorno
        if (contours.size() > 0) {
            // Se encontrou algum contorno, procure pelo de maior área
            double maxArea = 0; // área máxima
            Point centerPoint = new Point(); // ponto central
            float[] radius = new float[1]; // raio
            for (MatOfPoint wrapper : contours) {
                MatOfPoint2f wrapper2 = new MatOfPoint2f(wrapper.toArray()); // Transformar
                double area = Imgproc.contourArea(wrapper); // Calcular área do contorno
                if (area > maxArea) { // Se for a maior área até agora
                    maxArea = area;
                    Imgproc.minEnclosingCircle(wrapper2, centerPoint, radius); // calcular círculo
                    M = Imgproc.contourMoments(wrapper);
                }
            }
            centerPoint.set(new double[]{(M.get_m10() / M.get_m00()),(M.get_m01() / M.get_m00())});

            // Só continuar se o raio do círculo for maior que 10
            if (radius[0] > 50) {
                // Desenhar círculo e centroide
                Imgproc.circle(inv, centerPoint,(int)(radius[0]),
                        new Scalar(0,255,255),2);
                Imgproc.circle(inv, centerPoint, 5, new Scalar(0, 0, 255), -1);
                int x = 0;
                int w = inv.width();
                int h = inv.height();
                if (centerPoint.x>w/2) x++;
                if (centerPoint.y>h/2) x+=2;
                Rect roi = new Rect(new Point(quadrantes[x][0]*w/100, quadrantes[x][2]*h/100),
                        new Point(quadrantes[x][1]*w/100,quadrantes[x][3]*h/100));
                cropped = Mat.zeros(invGray.size(),CV_8UC4);
                cropped.submat(roi).setTo(Scalar.all(255));
                inv.copyTo(invGray,cropped);
                if (btSocket!=null)
                {
                    try
                    {
                        btSocket.getOutputStream().write(Integer.toString(x).getBytes());
                    }
                    catch (IOException e)
                    {
                        new ConnectBT().execute();
                    }
                }
            }
            else{
                if (btSocket!=null)
                {
                    try
                    {
                        btSocket.getOutputStream().write(Integer.toString(5).getBytes());
                    }
                    catch (IOException e)
                    {
                        Log.d("MYAPPBT", "Falhou", e);
                        new ConnectBT().execute();
                    }
                }
            }

        }


        return invGray;
    }
}
