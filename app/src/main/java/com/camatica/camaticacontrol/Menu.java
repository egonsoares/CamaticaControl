package com.camatica.camaticacontrol;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        final EditText matMin = findViewById(R.id.number_MatizMin);
        final EditText satMin = findViewById(R.id.number_SatMin);
        final EditText valMin = findViewById(R.id.number_ValorMin);
        final EditText matMax = findViewById(R.id.number_MatizMax);
        final EditText satMax = findViewById(R.id.number_SatMax);
        final EditText valMax = findViewById(R.id.number_ValorMax);
        Button calibrar = findViewById(R.id.button_Calibrar);
        calibrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Menu.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putBoolean("Calibrar",true);
                intent.putExtras(b);
                startActivity(intent);
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

}
