package com.example.apptest2;

import android.app.AlertDialog;
import android.content.Context; //Cuidado!
import android.content.DialogInterface;
import android.media.MediaPlayer;
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
    public Context context;


    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
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
        this.botaoSensores = (Button) findViewById(R.id.botaoSensores);

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
        //Gambiarra para desligar sensores caso a contagem regressiva acabe
        if(!sensoresRodando){
            sensorManager.unregisterListener(this);
        }

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
            botaoSensores.setEnabled(false);

            this.nomeUsuario = nomeText.getText().toString();

            //Delay para ativar sensores, usado para dar tempo extra ao usuário
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            sensorManager.registerListener(this, aSensor, SensorManager.SENSOR_DELAY_FASTEST);
            if(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
                sensorManager.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_FASTEST);
            }

            // Dados são enviados depois de tempo especificado
            int secs = Integer.parseInt(tempoText.getText().toString()); // Delay in seconds
            Espera.executar(secs, new Espera.DelayCallback() {
                @Override
                public void afterDelay() {
                    System.out.println("Depois de " + secs + " segundos");
                    sensoresRodando = false;
                    botaoInferencia.setEnabled(true);
                    botaoSensores.setEnabled(true);

                    if(inferenciaAtiva){
                        EnvioHTTP envioHTTP = new EnvioHTTP(nomeUsuario, "Inferencia", "?", listaSensorData);
                        emitirSom(0);
                        try {
                            if(envioHTTP.execute().get()) {
                                dialogoConexaoOk();
                                emitirSom(1);
                            }
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else{
                        EnvioHTTP envioHTTP = new EnvioHTTP(nomeUsuario, "Treinamento", atividadeTreinamento, listaSensorData);
                        emitirSom(0);
                        try {
                            if(envioHTTP.execute().get()) {
                                dialogoConexaoOk();
                                emitirSom(1);
                            }
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    //Limpando lista para novo envio
                    listaSensorData = new ArrayList<SensorData>();
                }
            });
        }
    }

    private void dialogoConexaoOk() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Aviso");
        builder.setMessage("Dados enviados com sucesso");
        builder.setPositiveButton("Ok!",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void emitirSom(int opcao){
        MediaPlayer mp = null;
        if(opcao==0) {
            mp = MediaPlayer.create(MainActivity.this, R.raw.beep_fim);
        }else if(opcao==1){
            mp = MediaPlayer.create(MainActivity.this, R.raw.beep_envio);
        }
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {

                mp.release();
            }

        });
        mp.start();
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
