package com.example.monitoramentoplacassolares.conexao;

import org.json.JSONObject;

public interface IAsyncHandler {
    void postResult(String result);

    void postResult(JSONObject result);
}
