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
import java.util.concurrent.Callable;

public class CallableCliente implements Callable<String> {
    public static final String TAG = "CallableCliente";

//    private static final String hostname = "172.16.116.172";
//    private static final String hostname = "192.168.1.110";
    public static final String hostname = "192.168.25.2";
    private static final int portaServidor = 12345;

    private String[] params;

    private String returnString;

    private static Socket socket = null;
    private static IAsyncHandler mHandler;
    private static IAsyncHandler mHandlerAnt;
    BufferedReader br;
    BufferedWriter bw;

    public CallableCliente(IAsyncHandler mHandler) {
        if(!(mHandler instanceof LoginAct))
            if(this.mHandlerAnt == null)
                mHandlerAnt = mHandler;
            else
                mHandlerAnt = this.mHandler;

        this.mHandler = mHandler;
    }

    public CallableCliente(IAsyncHandler mHandler, String... params) {
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
    public String call() {
        try {
            if (socket == null)
                socket = new Socket(hostname, portaServidor);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            if (socket.isConnected()) {
                if (Arrays.toString(params).contains("login")) {
                    enviarMsg(" ");
                    String aux = br.readLine();
                    Log.i(TAG, "call: " + aux);
//                    System.out.println(aux);
                    enviarMsg(Arrays.toString(params));
                    String res = br.readLine();
                    Log.i(TAG, "call: " + res);
//                    System.out.println(res);
//                    mHandler.postResult(res);
                    return res;
                }else {
                    MainActivity.executorService.submit(ouvir);
                    Log.i(TAG, "call: " + Arrays.toString(params));
                    enviarMsg(Arrays.toString(params));
                }
            }else{
//                mHandler.postResult("sem conexao");
                return "sem conexao";
            }

            return null;
        } catch(IOException e){
//            returnString = e.getMessage();
            return e.getMessage();
        }
    }
}