package com.example.monitoramentoplacassolares.conexao;

import android.util.Log;

import com.example.monitoramentoplacassolares.LoginAct;
import com.example.monitoramentoplacassolares.MainActivity;
import com.example.monitoramentoplacassolares.locais.LocalMonitoramento;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

public class RunnableCliente implements Runnable {
    public static final String TAG = "RunnableCliente";

    //    private static final String hostname = "172.16.116.172";
//    private static final String hostname = "192.168.1.110";
    public static final String hostname = "192.168.25.9";
    private static final int portaServidor = 12345;

    private String[] params;
    private JSONObject pacoteConfig;

    private Future clienteFuture;
    private boolean continuarComunicando;

    private Socket socket = null;
    private ObjectOutputStream objOut;
    private ObjectInputStream objIn;

    public static IAsyncHandler mHandler;
    private static IAsyncHandler mHandlerAnt;

    private GerenciadorDados gerenciadorDados;
    private List<TarefaCliente> tarefas = Collections.synchronizedList(new ArrayList<TarefaCliente>());

    private ArrayList<LocalMonitoramento> locais = new ArrayList<LocalMonitoramento>();

    public RunnableCliente() {

    }

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

    public void iniciaCliente() {
        Log.i(TAG, "iniciaCliente: \niniciando cliente");
        try {
            if (socket == null || !socket.isConnected()) {
                socket = new Socket();
                SocketAddress socketAddress = new InetSocketAddress(hostname, portaServidor);
                socket.connect(socketAddress, 1000);
                Log.i(TAG, "iniciaCliente: socket conectado " + socket.isConnected());
            }
            if (socket.isConnected()) {
                if (objIn == null) {
                    objIn = new ObjectInputStream(socket.getInputStream());
                    Log.i(TAG, "iniciaCliente: " + objIn.toString());
                }
                if (objOut == null) {
                    objOut = new ObjectOutputStream(socket.getOutputStream());
                    Log.i(TAG, "iniciaCliente: " + objOut.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Runnable iniciar = new Runnable() {
        @Override
        public void run() {
            iniciaCliente();
        }
    };

    public void novaTarefa(TarefaCliente novaTarefa) {
        Log.i(TAG, "novaTarefa: \npacotePedido: " + novaTarefa.getPacotePedido().toString());

        tarefas.add(novaTarefa);

        if (tarefas.size() == 1 && (clienteFuture == null || clienteFuture.isCancelled())) {
            Log.i(TAG, "novaTarefa: \niniciando cliente");
            clienteFuture = MainActivity.executorServiceCached.submit(this);
        }
    }

    private void proximaTarefa() {
        TarefaCliente tarefaAtual = tarefas.get(0);

        tarefas.remove(0);

        if (tarefaAtual instanceof TarefaComunicar && !tarefas.contains(tarefaAtual)) {
            novaTarefa(tarefaAtual);
        }

        if (tarefas.isEmpty()) {
            clienteFuture.cancel(false);
        }
    }

    private synchronized void executaTarefa(TarefaCliente tarefa) {
        Log.i(TAG, "executaTarefa:)");
        Log.i(TAG, "executaTarefa: " + tarefa.toString() + "--\npacote: " + tarefa.getPacotePedido().toString());

        tarefa.configuraConexao(socket, objIn, objOut);

        tarefa.executar();
    }

    @Override
    public void run() {
        Log.i(TAG, "run: \n cliente run");
        iniciaCliente();


        while (!clienteFuture.isCancelled()) {
            if (!tarefas.isEmpty()) {
                executaTarefa(tarefas.get(0));

                proximaTarefa();
            }
        }

    }

    public void cancelarComunicacao() {
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

    public GerenciadorDados getGerenciadorDados() {
        return gerenciadorDados;
    }

    public void setGerenciadorDados(GerenciadorDados gerenciadorDados) {
        this.gerenciadorDados = gerenciadorDados;
    }

    public ArrayList<LocalMonitoramento> getLocais() {
        return locais;
    }

    public void setLocais(ArrayList<LocalMonitoramento> locais) {
        this.locais = locais;
    }

    public void setClienteFuture(Future clienteFuture) {
        this.clienteFuture = clienteFuture;
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectOutputStream getObjOut() {
        return objOut;
    }

    public ObjectInputStream getObjIn() {
        return objIn;
    }
}