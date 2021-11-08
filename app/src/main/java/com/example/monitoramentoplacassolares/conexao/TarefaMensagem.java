package com.example.monitoramentoplacassolares.conexao;

import com.example.monitoramentoplacassolares.MainActivity;

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

    public void configuraComunicacao(Socket socket, ObjectInputStream objIn, ObjectOutputStream objOut){
        this.socket = socket;
        this.objIn = objIn;
        this.objOut = objOut;
    }

    @Override
    public void executar() {
        MainActivity.executorServiceCached.submit(this);
    }

    @Override
    public void run() {
        JSONObject resposta = new JSONObject();
        try {
            if (socket.isConnected()) {
                objOut.writeObject(getPacotePedido().toString());

                resposta = new JSONObject((String) objIn.readObject());

                getResultHandler().postResult(resposta);
            }
        } catch (ConnectException | SocketTimeoutException e) {
            try {
                resposta.put("resultado", "sem conexao");
                getResultHandler().postResult(resposta);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        } catch (IOException | ClassNotFoundException | JSONException e) {
            e.printStackTrace();
        }
    }
}
