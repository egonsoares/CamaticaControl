package com.camatica.camaticacontrol;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {
    private static final String TAG = "MYAPP::OPENCV";
    private boolean startMats = true;
    int i = 0;
    private Calibrador mCalibrador;
    private CameraBridgeViewBase mOpenCvCameraView;
    SharedPreferences sharedPreferences;



    private final BaseLoaderCallback mCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:
                {
                    Log.i("MYAPPO", "OpenCV loaded successfully");
                    mOpenCvCameraView = findViewById(R.id.OpenCvView);
                    mOpenCvCameraView.setMaxFrameSize(720,480);
                    mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
                    mOpenCvCameraView.setCvCameraViewListener(MainActivity.this);
                    mOpenCvCameraView.enableView();
                    if(startMats){
                        i++;
                        startMats = false;
                        Log.d("MYAPPBluetooth", "initMats"+i);
                    }
                    break;
                }
                default:
                {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // First check android version
        // Here, thisActivity is the current activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            }
        }
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if(savedInstanceState == null){
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mCallBack);
            Log.d("MYAPPBluetooth", "Iniciei");
            // everything else that doesn't update UI
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        mCalibrador = new Calibrador(getApplicationContext(), sharedPreferences);

        Bundle b = getIntent().getExtras();
        if(b != null) {
            if(!b.getBoolean("Calibrar", true)) {
                mCalibrador.calib = b.getBoolean("Calibrar", true) ? 0 : 4;
                mCalibrador.colorLower = new Scalar(b.getInt("MatMin", 0),
                        b.getInt("SatMin", 0), b.getInt("ValMin", 0));
                mCalibrador.colorUpper = new Scalar(b.getInt("MatMax", 255),
                        b.getInt("SatMax", 255), b.getInt("ValMax", 255));
            }
            else {
                mCalibrador.calib = 0;
            }
        }
        else {
            mCalibrador.calib = 0;
            mCalibrador.colorUpper = new Scalar(0, 0, 0);
            mCalibrador.colorLower = new Scalar(255, 255, 255);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        if (mCalibrador != null)
            mCalibrador.resetConnection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0,this,mCallBack);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mCalibrador.initMats();
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if(mCalibrador.calib<4) {
            return mCalibrador.calibrar(inputFrame.rgba(),inputFrame.gray());
        }

        // Calibração já foi feita
        else {
           return mCalibrador.processar(inputFrame.rgba(), inputFrame.gray());
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}