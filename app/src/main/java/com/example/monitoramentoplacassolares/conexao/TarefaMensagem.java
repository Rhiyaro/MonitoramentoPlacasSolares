package com.example.monitoramentoplacassolares.conexao;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TarefaMensagem extends TarefaCliente {
    public static final String TAG = "TarefaMensagem";

    private Socket socket;
    private ObjectOutputStream objOut;
    private ObjectInputStream objIn;

    public TarefaMensagem(IAsyncHandler resultHandler, JSONObject pacotePedido) {
        super(resultHandler, pacotePedido);
    }

    public TarefaMensagem(IAsyncHandler askerHandler, IAsyncHandler resultHandler, JSONObject pacotePedido) {
        super(askerHandler, resultHandler, pacotePedido);
    }

    @Override
    public void configuraConexao(Socket socket, ObjectInputStream objIn, ObjectOutputStream objOut) {
        this.socket = socket;
        this.objIn = objIn;
        this.objOut = objOut;
    }

    @Override
    public void executar() {
        //MainActivity.executorServiceCached.submit(this);
        run();
    }

    @Override
    public void run() {
        Log.i(TAG, "run start: \n"+getPacotePedido().toString());
        if(socket == null){
            Log.i(TAG, "run: socket is null");
        } else {
            Log.i(TAG, "run: socket is NOT null");
            Log.i(TAG, "run: "+socket.isConnected());
        }
        JSONObject resposta = new JSONObject();
        try {
            if (socket.isConnected()) {
                Log.i(TAG, "run: \ncorpo metodo run");
                objOut.writeObject(getPacotePedido().toString());

                resposta = new JSONObject((String) objIn.readObject());

                getResultHandler().postResult(resposta);
            }
        } catch (ConnectException | SocketTimeoutException e) {
            Log.e(TAG, "run exeception: ", e);
            try {
                resposta.put("resultado", "sem conexao");
                getResultHandler().postResult(resposta);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        } catch (IOException | ClassNotFoundException | JSONException e) {
            Log.e(TAG, "run exeception: ", e);
            e.printStackTrace();
        }
    }
}
