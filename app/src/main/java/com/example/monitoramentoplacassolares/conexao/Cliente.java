package com.example.monitoramentoplacassolares.conexao;

import android.os.AsyncTask;
import android.util.Log;

import com.example.monitoramentoplacassolares.LoginAct;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Arrays;

public class Cliente extends AsyncTask<String, Integer, String> {
    public static final String TAG = "Cliente";

//    private static final String hostname = "172.16.116.172";
//    private static final String hostname = "192.168.1.110";
    public static final String hostname = "192.168.25.11";
    private static final int portaServidor = 12345;

    private static Socket socket = null;
    private static IAsyncHandler mHandler;
    private static IAsyncHandler mHandlerAnt;
    BufferedReader br;
    BufferedWriter bw;

    public Cliente(IAsyncHandler mHandler) {
        if(!(mHandler instanceof LoginAct))
            if(this.mHandlerAnt == null)
                mHandlerAnt = mHandler;
            else
                mHandlerAnt = this.mHandler;

        this.mHandler = mHandler;
    }

    @Override
    protected String doInBackground(String... params) {
        Log.i(TAG, "doInBackground: start");
        try {
            if (socket == null) {
                Log.i(TAG, "doInBackground: socket is null, creating new");
                socket = new Socket(hostname, portaServidor);
            }
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            if (socket.isConnected()) {
                Log.i(TAG, "doInBackground: socket is connected");
                if (Arrays.toString(params).contains("login")) {
                    enviarMsg(" ");
                    String aux = br.readLine();
                    Log.i(TAG, "doInBackground: " + aux);
//                    System.out.println(aux);
                    enviarMsg(Arrays.toString(params));
                    String res = br.readLine();
                    Log.i(TAG, "doInBackground: " + res);
//                    System.out.println(res);
                    mHandler.postResult(res);
                }else {
                    new Thread(ouvir).start();
                    Log.i(TAG, "doInBackground: " + Arrays.toString(params));
                    enviarMsg(Arrays.toString(params));
                }
            }else{
                Log.i(TAG, "doInBackground: socket isnt connected");
                mHandler.postResult("sem conexao");
            }

            return null;
            } catch(IOException e){
                return e.getMessage();
            }
    }

    public void enviarMsg(String msg){
        try {
            bw.write(msg);
            bw.newLine();
            bw.flush();
        }catch (IOException ioex){
            ioex.printStackTrace();
        }
    }

    Runnable ouvir = new Runnable(){
        public void run() {
            while (mHandler == mHandlerAnt) {
                String retorno = null;
                try {
                    retorno = br.readLine();
                    Log.i("Runnable ouvir", "run: " + retorno);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(retorno != null) mHandler.postResult(retorno);
            }
            mHandlerAnt = mHandler;
        }
    };


    @Override
    protected void onPostExecute(String result) {
        //Aqui você utiliza o retorno do doInBackground
        //Aqui é interessante usar um EventBus, ou alguma outra classe para poder enviar essa
        //informação para a interface.
        //System.out.println(result);
        //mHandler.postResult(result);
    }
}