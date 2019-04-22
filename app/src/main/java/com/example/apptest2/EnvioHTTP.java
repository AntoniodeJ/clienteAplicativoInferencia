package com.example.apptest2;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class EnvioHTTP extends AsyncTask<Void, Void , Boolean> {

    String nome;
    String tipo;
    String atividade;
    ArrayList<SensorData> listaDados;
    String stringListaDados;

    public EnvioHTTP(String nome, String tipo, String atividade, ArrayList<SensorData> listaDados){

        this.nome = nome;
        this.tipo = tipo;
        this.atividade = atividade;
        this.listaDados = listaDados;
        this.stringListaDados = "";
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        for (SensorData sensorData : this.listaDados){
            stringListaDados += sensorData.getTipo() + ";" + sensorData.getX() + ";" +
                    sensorData.getY() + ";" + sensorData.getZ() + "<SD>";
        }

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("https://activity-inferencer.herokuapp.com/echoPost");
        //HttpPost httppost = new HttpPost("http://localhost:9000/echoPost");

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(4);
        params.add(new BasicNameValuePair("Nome", nome));
        params.add(new BasicNameValuePair("Tipo", tipo));
        params.add(new BasicNameValuePair("Atividade", atividade));
        params.add(new BasicNameValuePair("SensorsData", stringListaDados));
        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //Execute and get the response.
        HttpResponse response = null;
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            try (InputStream instream = entity.getContent()) {
                BufferedReader entrada = new BufferedReader(new InputStreamReader(instream));
                String recebido;
                while((recebido = entrada.readLine()) != null) {
                    System.out.println(recebido);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
