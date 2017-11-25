package com.camatica.camaticacontrol;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Menu extends AppCompatActivity {
    long time = System.currentTimeMillis();
    private static final String KEYUPPER1 = "colorUpper1";
    private static final String KEYUPPER2 = "colorUpper2";
    private static final String KEYUPPER3 = "colorUpper3";
    private static final String KEYLower1 = "colorLower1";
    private static final String KEYLower2 = "colorLower2";
    private static final String KEYLower3 = "colorLower3";
    private EditText matMin;
    private EditText satMin;
    private EditText valMin;
    private EditText matMax;
    private EditText satMax;
    private EditText valMax;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            }
        }
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        matMin = findViewById(R.id.number_MatizMin);
        matMin.setText(String.valueOf(sharedPreferences.getInt(KEYLower1, 0)));
        satMin = findViewById(R.id.number_SatMin);
        satMin.setText(String.valueOf(sharedPreferences.getInt(KEYLower2, 0)));
        valMin = findViewById(R.id.number_ValorMin);
        valMin.setText(String.valueOf(sharedPreferences.getInt(KEYLower3, 0)));
        matMax = findViewById(R.id.number_MatizMax);
        matMax.setText(String.valueOf(sharedPreferences.getInt(KEYUPPER1, 255)));
        satMax = findViewById(R.id.number_SatMax);
        satMax.setText(String.valueOf(sharedPreferences.getInt(KEYUPPER2, 255)));
        valMax = findViewById(R.id.number_ValorMax);
        valMax.setText(String.valueOf(sharedPreferences.getInt(KEYUPPER3, 255)));
        Button calibrar = findViewById(R.id.button_Calibrar);
        calibrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(System.currentTimeMillis()>time+1000) {
                    time = System.currentTimeMillis();
                    Intent intent;
                    intent = new Intent(Menu.this, MainActivity.class);
                    Bundle b = new Bundle();
                    b.putBoolean("Calibrar", true);
                    intent.putExtras(b);
                    Log.d("MYAPPBluetooth", "Comecei");
                    startActivity(intent);
                }
            }
        });
        Button manual = findViewById(R.id.button_Manual);
        manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Menu.this, MainActivity.class);
                Bundle b = new Bundle();
                if(!TextUtils.isEmpty(matMin.getText().toString()))
                b.putInt("MatMin", Integer.parseInt(matMin.getText().toString()));
                if(!TextUtils.isEmpty(satMin.getText().toString()))
                b.putInt("SatMin", Integer.parseInt(satMin.getText().toString()));
                if(!TextUtils.isEmpty(valMin.getText().toString()))
                b.putInt("ValMin", Integer.parseInt(valMin.getText().toString()));
                if(!TextUtils.isEmpty(matMax.getText().toString()))
                b.putInt("MatMax", Integer.parseInt(matMax.getText().toString()));
                if(!TextUtils.isEmpty(satMax.getText().toString()))
                b.putInt("SatMax", Integer.parseInt(satMax.getText().toString()));
                if(!TextUtils.isEmpty(valMax.getText().toString()))
                b.putInt("ValMax", Integer.parseInt(valMax.getText().toString()));
                b.putBoolean("Calibrar",false);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
        Button parear = findViewById(R.id.button_Bluetooth);
        parear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentOpenBluetoothSettings = new Intent();
                intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intentOpenBluetoothSettings);
            }
        });
        Button sair = findViewById(R.id.button_Sair);
        sair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        matMin.setText(String.valueOf(sharedPreferences.getInt(KEYLower1, 0)));
        satMin.setText(String.valueOf(sharedPreferences.getInt(KEYLower2, 0)));
        valMin.setText(String.valueOf(sharedPreferences.getInt(KEYLower3, 0)));
        matMax.setText(String.valueOf(sharedPreferences.getInt(KEYUPPER1, 255)));
        satMax.setText(String.valueOf(sharedPreferences.getInt(KEYUPPER2, 255)));
        valMax.setText(String.valueOf(sharedPreferences.getInt(KEYUPPER3, 255)));


    }
}
