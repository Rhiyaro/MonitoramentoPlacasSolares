package com.example.monitoramentoplacassolares.conexao;

import android.util.Log;

import com.example.monitoramentoplacassolares.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TarefaComunicar extends TarefaCliente {
    public static final String TAG = "TarefaComunicar";

    public static boolean primeiraChamada = true;

    private boolean continuarComunicando;
    private Socket socket;
    private ObjectOutputStream objOut;
    private ObjectInputStream objIn;

    public TarefaComunicar(IAsyncHandler resultHandler, JSONObject pacotePedido) {
        super(resultHandler, pacotePedido);
    }

    public TarefaComunicar(IAsyncHandler askerHandler, IAsyncHandler resultHandler, JSONObject pacotePedido) {
        super(askerHandler, resultHandler, pacotePedido);
    }

    public void configuraComunicacao(Socket socket, ObjectInputStream objIn, ObjectOutputStream objOut) {
        this.socket = socket;
        this.objIn = objIn;
        this.objOut = objOut;
    }

    public void executar() {
        continuarComunicando = true;
        MainActivity.executorServiceCached.submit(this);
    }

    @Override
    public void run() {
        Log.i(TAG, ".\ncomunicar: start");

        JSONObject pacoteRetorno = new JSONObject();
        try {
            if (socket.isConnected()) {
                while (continuarComunicando) {
                    objOut.writeObject(this.getPacotePedido().toString());

                    pacoteRetorno = new JSONObject((String) objIn.readObject());

                    if (!primeiraChamada) {
                        this.getResultHandler().postResult(pacoteRetorno);
                    }
                    primeiraChamada = false;

                    Thread.sleep(3000);
                }
            }
        } catch (ConnectException | SocketTimeoutException e) {
            try {
                pacoteRetorno.put("resultado", "sem conexao");
                getResultHandler().postResult(pacoteRetorno);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        } catch (IOException | ClassNotFoundException | JSONException | InterruptedException e) {
            Log.e(TAG, "comunicarException: ", e);
        }
    }
}
