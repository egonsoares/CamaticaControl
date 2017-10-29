package com.camatica.camaticacontrol;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;

/**
 * Criado por Egon Soares em 29/10/2017
 */

class Calibrador {
    private static final String TAG = "MYAPP::OPENCV";

    Scalar colorUpper = new Scalar(0, 0, 0);
    Scalar colorLower = new Scalar(255, 255, 255);

    private Mat inv;
    private Mat lowHsv;
    private Mat thresh;
    private Mat mHierarchy;
    private Mat cropped;

    private int tempo = 0; 
    int calib = 0;
    private final int[][] rects = {{15,25,15,25}, {15,25,65,75}, {65,75,15,25}, {65,75,65,75}};
    private Scalar lowerB = new Scalar(0,0,0), upperB = new Scalar(255,255,255);
    private double lastScore = 0;

    void initMats(){
        inv = new Mat();
        lowHsv = new Mat();
        thresh = new Mat();
        mHierarchy = new Mat();
        cropped = new Mat();

    }
    Mat calibrar(Mat input){
        
        // Inverter imagem
        Core.flip(input,inv,1);


        // Recriar a imagem em HSV
        Imgproc.cvtColor(inv,lowHsv,Imgproc.COLOR_RGB2HSV_FULL);
        
        //Calibração
            Log.d(TAG,"Calibrar");

            //Esperar um tempo
            if(tempo<30) {
                tempo++;
                int h = inv.height(); // Altura da imagem invertida
                int w = inv.width(); // Largura da imagem invertida
                Imgproc.putText(inv, "Preencha o quadrado com a cor",
                        new Point(5 * w / 100, 6 * h / 10),
                        0, 1, new Scalar(255, 255, 255)); // Título de ordem

                // Retângulo a ser maximizado
                Imgproc.rectangle(inv, new Point(rects[calib][0] * w / 100,
                        rects[calib][2] * h / 100), new Point(rects[calib][1] * w / 100,
                        rects[calib][3] * h / 100), new Scalar(0, 0, 0), 5);
                return inv;
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

                    Log.d(TAG,"Last Score " + lastScore + " score" + String.valueOf(score));
                    Log.d(TAG,"lowerB" + lowerB + "upperB" + upperB);

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
                    Log.d(TAG,"There are " + loops + " loops and maximizing" + optElement);
                }

                // Copiar valores para os thresholds
                for(int i = 0; i < 3; i++) colorLower.val[i] = Math.min(lowerB.val[i],
                        colorLower.val[i]);
                for(int i = 0; i < 3; i++) colorUpper.val[i] = Math.max(upperB.val[i],
                        colorUpper.val[i]);
                lowerB = new Scalar(0,0,0);
                upperB = new Scalar(255,255,255);
                calib++;
                tempo = 0;
                return inv;
            
        }
    }
    Mat processar(Mat input) {
        // Inverter imagem
        Core.flip(input,inv,1);


        // Recriar a imagem em HSV
        Imgproc.cvtColor(inv,lowHsv,Imgproc.COLOR_RGB2HSV_FULL);
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
        Imgproc.drawContours(inv,contours,-1,new Scalar(255,0,0,255));
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
            if (radius[0] > 5) {
                // Desenhar círculo e centroide
                Imgproc.circle(inv, centerPoint,(int)(radius[0]),
                        new Scalar(0,255,255),2);
                Imgproc.circle(inv, centerPoint, 5, new Scalar(0, 0, 255), -1);
            }
        }
        return inv;
    }
}
