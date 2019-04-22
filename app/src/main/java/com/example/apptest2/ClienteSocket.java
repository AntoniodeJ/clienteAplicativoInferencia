package com.example.apptest2;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class ClienteSocket extends AsyncTask<Void, String, Socket> {

    private String host;
    private int port;
    private int timeout;
    private Socket socket;

    public ClienteSocket(String host, int port, int timeout){
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    @Override
    protected Socket doInBackground(Void... params) {
        socket = null;
        try {
            SocketAddress sockaddr = new InetSocketAddress(host, port);
            socket = new Socket();
            socket.connect(sockaddr, timeout); // milisegundos
            if (socket.isConnected()) {
                System.out.println("Conectado com sucesso no servidor!");
                return socket;
            } else {
                publishProgress("CONNECT ERROR");
            }
        } catch (IOException e) {
            publishProgress("ERROR");
            Log.e("SocketAndroid", "Erro de entrada e saida ao conectar", e);
        } catch (Exception e) {
            publishProgress("ERROR");
            Log.e("SocketAndroid", "Erro generico", e);
        }
        return socket;
    }
}
