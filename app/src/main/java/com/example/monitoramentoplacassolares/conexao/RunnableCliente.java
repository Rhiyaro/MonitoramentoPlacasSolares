package com.example.monitoramentoplacassolares.conexao;

import android.util.Log;

import com.example.monitoramentoplacassolares.LoginAct;
import com.example.monitoramentoplacassolares.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Future;

public class RunnableCliente implements Runnable {
    public static final String TAG = "RunnableCliente";

    //    private static final String hostname = "172.16.116.172";
//    private static final String hostname = "192.168.1.110";
    public static final String hostname = "192.168.25.9";
    private static final int portaServidor = 12345;

    private String[] params;
    private JSONObject pacoteConfig;

    private String returnString;
    private String retorno;
    private JSONObject pacoteRetorno;

    private Future comunicarFuture;
    private boolean continuarComunicando;

    private ObjectOutputStream objOut;
    private ObjectInputStream objIn;

    private static Socket socket = null;
    public static IAsyncHandler mHandler;
    private static IAsyncHandler mHandlerAnt;
    BufferedReader br;
    BufferedWriter bw;

    public RunnableCliente(IAsyncHandler mHandler) {
        if (!(mHandler instanceof LoginAct))
            if (mHandlerAnt == null)
                mHandlerAnt = mHandler;
            else
                mHandlerAnt = RunnableCliente.mHandler;

        RunnableCliente.mHandler = mHandler;
    }

    public RunnableCliente(IAsyncHandler mHandler, String... params) {
        if (!(mHandler instanceof LoginAct)) {
            if (mHandlerAnt == null) {
                mHandlerAnt = mHandler;
            } else {
                mHandlerAnt = RunnableCliente.mHandler;
            }
        }
        RunnableCliente.mHandler = mHandler;

        this.params = params;
    }

    public RunnableCliente(IAsyncHandler mHandler, JSONObject pacoteConfig) {
        if (!(mHandler instanceof LoginAct)) {
            if (mHandlerAnt == null) {
                mHandlerAnt = mHandler;
            } else {
                mHandlerAnt = RunnableCliente.mHandler;
            }
        }
        RunnableCliente.mHandler = mHandler;

        this.pacoteConfig = pacoteConfig;
    }

    public RunnableCliente(IAsyncHandler mHandler, JSONObject pacoteConfig, String... params) {
        if (!(mHandler instanceof LoginAct)) {
            if (mHandlerAnt == null) {
                mHandlerAnt = mHandler;
            } else {
                mHandlerAnt = RunnableCliente.mHandler;
            }
        }
        RunnableCliente.mHandler = mHandler;

        this.pacoteConfig = pacoteConfig;
        this.params = params;
    }

    public void enviarMsg(String msg) {
        try {
            bw.write(msg+"\n");
            bw.newLine();
            bw.flush();
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    Runnable ouvir = new Runnable() {
        public void run() {
            while (mHandler == mHandlerAnt) {
                retorno = null;
                try {
                    retorno = br.readLine();

                    if (retorno != null) mHandler.postResult(retorno);
                } catch (IOException e) {
                    Log.e(TAG, "ouvirException: ", e);
                }

            }
            mHandlerAnt = mHandler;
        }
    };

    Runnable comunicar = new Runnable() {
        public void run() {
            while (continuarComunicando) {
                pacoteRetorno = null;
                try {
                    pacoteRetorno = new JSONObject((String)objIn.readObject());
                    if (pacoteRetorno != null) {
                        mHandler.postResult(pacoteRetorno);
                    }
                    objOut.writeObject(pacoteConfig.toString());
                } catch (IOException | ClassNotFoundException | JSONException e) {
                    Log.e(TAG, "comunicarException: ", e);
                }
            }
        }
    };

    @Override
    public void run() {
        //Thread.currentThread().setName("Thread RunnCliente " + MainActivity.x);
        /*
        TODO:   Pensar em (talvez) trocar a forma de comunicação entre as threads
                Talvez retirar o Handler criado e utilizar Handlers do java em si
         */
        try {
            if (socket == null) socket = new Socket(hostname, portaServidor);
            //TODO: Checar se já é seguro retirar esses readers e writers da classe
            //br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            if(objIn == null) objIn = new ObjectInputStream(socket.getInputStream());
            if(objOut == null)objOut = new ObjectOutputStream(socket.getOutputStream());

            /*
            Resposta recebida pelo servidor
             */
            JSONObject resposta = new JSONObject();

            if (socket.isConnected()) {
                /*
                Age de acordo com o campo "acao" do pacote
                 */
                Log.i(TAG, "run: "+pacoteConfig.toString());
                switch (pacoteConfig.getString("acao")) {
                    case "logar":
                    case "cadastrar":
                        objOut.writeObject(pacoteConfig.toString());
                        resposta = new JSONObject((String)objIn.readObject());
                        break;
                    case "comunicar":
                        continuarComunicando = true;
                        objOut.writeObject(pacoteConfig.toString());
                        comunicarFuture = MainActivity.executorServiceCached.submit(comunicar);
                        break;
                    default:
                        returnString = "ação desconhecida";
                        resposta.put("resultado", "desconhecido");
                }
            } else {
                resposta.put("resultado", "sem conexao");
            }

            if(!pacoteConfig.getString("acao").equals("comunicar")) mHandler.postResult(resposta);
        } catch (IOException | JSONException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void cancelarComunicacao(){
        continuarComunicando = false;
    }

    public void setPacoteConfig(JSONObject pacoteConfig) {
        this.pacoteConfig = pacoteConfig;
    }

    public static void setmHandler(IAsyncHandler mHandler) {
        RunnableCliente.mHandler = mHandler;
    }

    public static void setmHandlerAnt(IAsyncHandler mHandlerAnt) {
        RunnableCliente.mHandlerAnt = mHandlerAnt;
    }

    public static IAsyncHandler getmHandler() {
        return mHandler;
    }

    public Future getComunicarFuture() {
        return comunicarFuture;
    }
}