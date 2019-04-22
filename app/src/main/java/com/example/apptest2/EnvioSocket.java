package com.example.apptest2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

import android.os.AsyncTask;
import android.util.Log;


public class EnvioSocket extends AsyncTask<String, String, Boolean> {

    private Socket socket;

    public EnvioSocket(Socket socket){
        this.socket = socket;
    }


    @Override
    protected Boolean doInBackground(String... params) {
        boolean result = false;
        try {
            if (socket.isConnected()) {
                System.out.println("Chegou no envio");
                PrintStream saida = new PrintStream(socket.getOutputStream());
                //BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                saida.println(params[0]);
            } else {
                publishProgress("CONNECT ERROR");
            }
        } catch (IOException e) {
            publishProgress("ERROR");
            Log.e("SocketAndroid", "Erro de entrada e saida ao enviar", e);
            result = true;
        } catch (Exception e) {
            publishProgress("ERROR");
            Log.e("SocketAndroid", "Erro generico", e);
            result = true;
        }
        return result;
    }
}
