package com.example.apptest2;

import android.os.Handler;


public class Espera {

    // Delay mechanism

    public interface DelayCallback{
        void afterDelay();
    }

    public static void executar(int secs, final DelayCallback delayCallback){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                delayCallback.afterDelay();
            }
        }, secs * 1000); // afterDelay will be executed after (secs*1000) milliseconds.
    }
}