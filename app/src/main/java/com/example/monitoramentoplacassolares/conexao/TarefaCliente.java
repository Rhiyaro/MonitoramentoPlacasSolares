package com.example.monitoramentoplacassolares.conexao;

import org.json.JSONObject;

public abstract class TarefaCliente implements Runnable{
    public static final String TAG = "TarefaCliente";

    protected String tipoTarefa;

    private IAsyncHandler askerHandler, resultHandler;
    private JSONObject pacotePedido;

    public TarefaCliente() {
    }

    public TarefaCliente(IAsyncHandler resultHandler, JSONObject pacotePedido) {
        this.resultHandler = resultHandler;
        this.pacotePedido = pacotePedido;
    }

    public TarefaCliente(IAsyncHandler askerHandler, IAsyncHandler resultHandler, JSONObject pacotePedido) {
        this.askerHandler = askerHandler;
        this.resultHandler = resultHandler;
        this.pacotePedido = pacotePedido;
    }

    public abstract void executar();

    public IAsyncHandler getAskerHandler() {
        return askerHandler;
    }

    public IAsyncHandler getResultHandler() {
        return resultHandler;
    }

    public JSONObject getPacotePedido() {
        return pacotePedido;
    }
}
