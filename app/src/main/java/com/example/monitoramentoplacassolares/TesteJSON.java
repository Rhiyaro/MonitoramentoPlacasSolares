package com.example.monitoramentoplacassolares;

import android.util.Log;

import org.json.JSONObject;
import  org.json.JSONException;

public class TesteJSON {
    public static final String TAG = "TesteJSON";
    JSONObject jsonobject = new JSONObject();

    public void testeJson(){
        try{
            this.jsonobject.put("chave_teste", "valor_teste");
        }catch (JSONException je){
            Log.e(TAG, "testeJson: "+je.getMessage(), je);
        }
    }
}
