package com.example.monitoramentoplacassolares.conexao;

import android.util.Log;

import com.example.monitoramentoplacassolares.LoginAct;
import com.example.monitoramentoplacassolares.MainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Future;

public class RunnableCliente implements Runnable {
    public static final String TAG = "RunnableCliente";

    public static final String hostname = "192.168.25.9";
    private static final int portaServidor = 12345;

    private String[] params;

    private String returnString;

    private Future ouvirFuture;

    private static Socket socket = null;
    private static IAsyncHandler mHandler;
    private static IAsyncHandler mHandlerAnt;
    BufferedReader br;
    BufferedWriter bw;

    public RunnableCliente(IAsyncHandler mHandler) {
        if(!(mHandler instanceof LoginAct))
            if(this.mHandlerAnt == null)
                mHandlerAnt = mHandler;
            else
                mHandlerAnt = this.mHandler;

        this.mHandler = mHandler;
    }

    public RunnableCliente(IAsyncHandler mHandler, String... params) {
        if(!(mHandler instanceof LoginAct))
            if(this.mHandlerAnt == null)
                mHandlerAnt = mHandler;
            else
                mHandlerAnt = this.mHandler;

        this.mHandler = mHandler;

        this.params = params;
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
            Thread.currentThread().setName("Thread Ouvir " + MainActivity.x);
            while (mHandler == mHandlerAnt) {
                String retorno = null;
                try {
                    retorno = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(retorno != null) mHandler.postResult(retorno);
            }
            mHandlerAnt = mHandler;
        }
    };

    @Override
    public void run() {
        Thread.currentThread().setName("Thread RunnCliente " + MainActivity.x);
        try {
            if (socket == null)
                socket = new Socket(hostname, portaServidor);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            if (socket.isConnected()) {
                if (Arrays.toString(params).contains("login")) {
                    enviarMsg(" ");
                    String aux = br.readLine();
                    Log.i(TAG, "run: " + aux);
                    enviarMsg(Arrays.toString(params));
                    String res = br.readLine();
                    Log.i(TAG, "run: " + res);
                    mHandler.postResult(res);
                }else {
                    ouvirFuture = MainActivity.executorService.submit(ouvir);
                    Log.i(TAG, "run: " + Arrays.toString(params));
                    enviarMsg(Arrays.toString(params));
                }
            }else{
                mHandler.postResult("sem conexao");
            }

            return;
        } catch(IOException e){
            returnString = e.getMessage();
        }
    }

    public Future getOuvirFuture() {
        return ouvirFuture;
    }
}