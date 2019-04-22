package com.example.apptest2;

import android.content.Context; //Cuidado!
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.*;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity implements SensorEventListener, AdapterView.OnItemSelectedListener {
    private SensorManager sensorManager;
    private Sensor aSensor;
    private Sensor gSensor;

    private TextView accelXText, accelYText, accelZText;
    private TextView gyroXText, gyroYText, gyroZText;

    private Button botaoSensores;
    private ToggleButton botaoInferencia;
    private EditText nomeText, tempoText;
    private Spinner atividadeSpinner;

    private Boolean sensoresRodando;
    private Boolean inferenciaAtiva;
    private String atividadeTreinamento;
    private ArrayList<SensorData> listaSensorData;
    private String nomeUsuario;


    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listaSensorData = new ArrayList<SensorData>();
        this.accelXText = (TextView) findViewById(R.id.accelXText);
        this.accelYText = (TextView) findViewById(R.id.accelYText);
        this.accelZText = (TextView) findViewById(R.id.accelZText);
        this.gyroXText = (TextView) findViewById(R.id.giroXText);
        this.gyroYText = (TextView) findViewById(R.id.giroYText);
        this.gyroZText = (TextView) findViewById(R.id.giroZText);
        this.nomeText = (EditText) findViewById(R.id.nomeText);
        this.tempoText = (EditText) findViewById(R.id.tempoText);

        this.botaoInferencia = (ToggleButton) findViewById(R.id.botaoInferencia);

        //Implementação do spinner de atividades
        this.atividadeSpinner = (Spinner) findViewById(R.id.atividadeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.atividades_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        atividadeSpinner.setAdapter(adapter);
        atividadeSpinner.setOnItemSelectedListener(this);


        sensoresRodando = false;
        inferenciaAtiva = false;
        atividadeTreinamento = "Andando";

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

        //Botao toggle de inferência
        botaoInferencia.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    inferenciaAtiva = true;
                    atividadeSpinner.setEnabled(false);
                } else {
                    inferenciaAtiva = false;
                    atividadeSpinner.setEnabled(true);
                }
            }
        });
    }

    //Listener do Spinner
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        String selecionado = (String) parent.getItemAtPosition(pos);
        atividadeTreinamento = selecionado;
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    // Listener dos Sensores
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        //EXPERIMENTO : PEGAR NO CELULAR
        float offsetX = (float) 0; //-0.043315083;
        float offsetY = (float) 0; //-0.18616095;
        float offsetZ = (float) 0; //-9.937521;

        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            SensorData sensorDataAccel = new SensorData("TYPE_ACCELEROMETER",event.values[0]+offsetX,event.values[1]+offsetY,event.values[2]+offsetZ);
            listaSensorData.add(sensorDataAccel);
            this.accelXText.setText("AccelX: "+Float.toString(sensorDataAccel.getX()));
            this.accelYText.setText("AccelY: "+Float.toString(sensorDataAccel.getY()));
            this.accelZText.setText("AccelZ: "+Float.toString(sensorDataAccel.getZ()));
        }else if(sensor.getType() == Sensor.TYPE_GYROSCOPE){
            SensorData sensorDataGyro = new SensorData("TYPE_GYROSCOPE",event.values[0],event.values[1],event.values[2]);
            listaSensorData.add(sensorDataGyro);
            this.gyroXText.setText("GyroX: "+Float.toString(sensorDataGyro.getX()));
            this.gyroYText.setText("GyroY: "+Float.toString(sensorDataGyro.getY()));
            this.gyroZText.setText("GyroZ: "+Float.toString(sensorDataGyro.getZ()));
        }
    }


    //Listener do Botão de envio
    public void controlarSensores(View view){
        if(!sensoresRodando){
            sensoresRodando = true;
            botaoInferencia.setEnabled(false);

            this.nomeUsuario = nomeText.getText().toString();


            sensorManager.registerListener(this, aSensor, SensorManager.SENSOR_DELAY_FASTEST);
            if(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
                sensorManager.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_FASTEST);
            }
        }
        else if(sensoresRodando){
            sensoresRodando = false;
            botaoInferencia.setEnabled(true);
            sensorManager.unregisterListener(this);

            if(inferenciaAtiva){
                EnvioHTTP envioHTTP = new EnvioHTTP(nomeUsuario, "Inferencia", "?", listaSensorData);
                envioHTTP.execute();
            }else{
                EnvioHTTP envioHTTP = new EnvioHTTP(nomeUsuario, "Treinamento", atividadeTreinamento, listaSensorData);
                envioHTTP.execute();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        finish();
    }
}
