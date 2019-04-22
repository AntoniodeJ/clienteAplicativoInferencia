package com.example.apptest2;

public class Util {

    public static String gerarStringPorSensorData(SensorData sensorData){
        String mensagem = "";
        if(sensorData!=null){
            mensagem = sensorData.getTipo()+";"+sensorData.getX()+";"+
                    sensorData.getY()+";"+sensorData.getZ();
        }
        return mensagem;
    }

    public static SensorData gerarSensorDataPorVetorString(String[] string){
        SensorData sensorData = null;
        String sensorTipo;
        float x,y,z;
        if (string != null){
            if(string[0].equals("DADOS")){
                sensorTipo = string[1];
                x = Float.parseFloat(string[2]);
                y = Float.parseFloat(string[3]);
                z = Float.parseFloat(string[4]);
                sensorData = new SensorData(sensorTipo,x,y,z);
            }
        }
        return sensorData;
    }
}
