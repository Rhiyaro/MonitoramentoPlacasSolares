package com.example.monitoramentoplacassolares.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.monitoramentoplacassolares.R;
import com.example.monitoramentoplacassolares.adapters.ListaNotificacaoAdapter;
import com.example.monitoramentoplacassolares.conexao.IAsyncHandler;
import com.example.monitoramentoplacassolares.conexao.TarefaMensagem;
import com.example.monitoramentoplacassolares.excecoes.HttpRequestException;
import com.example.monitoramentoplacassolares.httpcomm.MpsHttpClient;
import com.example.monitoramentoplacassolares.httpcomm.MpsHttpServerInfo;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Response;

public class ListaNotificacoes extends AppCompatActivity implements IAsyncHandler, NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "ListaNotificacoes";

    //TODO: Adaptar para HTTP -> Limpar restos de comunicação por Socket
    //TODO: Implementar filtro
    private NavigationDrawer navDrawer;
    private DrawerLayout drawer;
    private Toolbar tb;

    private RecyclerView listaNotificacaoRV;
    private Button bt_refresh;

    private List<JSONObject> notificacoes = Collections.synchronizedList(new ArrayList<>());
    private final Object esperaResposta = new Object();
    private ListaNotificacoes objetoPrincipal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_lista_notificacoes);
        objetoPrincipal = this;

        //navDrawer = new NavigationDrawer(this);

        tb = findViewById(R.id.toolbarDadosAct);
        setSupportActionBar(tb);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, tb, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        bt_refresh = findViewById(R.id.bt_refresh);

        listaNotificacaoRV = findViewById(R.id.lista_notif_rv);
        atualizaLista();

    }

    public void btAtualizaLista(View view) {
//        MainActivity.executorServiceCached.submit(() -> {
//            try {
//                synchronized (esperaResposta) {
//                    atualizaNotificacoes();
//                    esperaResposta.wait();
//                }
//            } catch (JSONException | InterruptedException e) {
//                e.printStackTrace();
//            }
//            Log.i(TAG, "AtualizaLista: " + notificacoes.toString());
//
//            runOnUiThread(this::atualizaLista);
//        });
        MainActivity.executorServiceCached.submit(() -> {
            try {
                atualizaNotificacoes();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "AtualizaLista: " + notificacoes.toString());

            runOnUiThread(this::atualizaLista);
        });
    }

    private void atualizaNotificacoes() throws JSONException {
//        JSONObject pedido = new JSONObject();
//        pedido.put("acao", "notificacoes");
//        pedido.put("pedido", "lista notificacoes");
//
//        TarefaMensagem tarefaMsg = new TarefaMensagem(objetoPrincipal, pedido);
//        tarefaMsg.configuraConexao(MainActivity.runnableCliente.getSocket(),
//                MainActivity.runnableCliente.getObjIn(),
//                MainActivity.runnableCliente.getObjOut());
//
//        MainActivity.runnableCliente.novaTarefa(tarefaMsg);

        try (Response notifsResponse = MpsHttpClient.instacia().doGet(MpsHttpServerInfo.PATH_NOTIFICACOES)) {
            String responseBodyStr = notifsResponse.body().string();
            Log.i(TAG, "atualizaNotificacoes: \n"+responseBodyStr);
            int statusCode = notifsResponse.code();
            if (statusCode == MpsHttpClient.HTTP_OK_RESPONSE) {
                JSONObject notifsJson = new JSONObject(responseBodyStr);
                carregaNotificacoes(notifsJson);
            } else {
                Log.i(TAG, "atualizaLocaisHttp: Erro: Código de resposta inesperado: " + statusCode);
            }
        } catch (HttpRequestException | IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void carregaNotificacoes(JSONObject notifsJson) throws JSONException {
        JSONArray notifs = notifsJson.optJSONArray("lista-notificacoes");

        if (notifs == null) {
            Log.i(TAG, "carregaNotificacoes: notifs is null");
            return;
        }

        notificacoes.clear();
        for (int i = 0; i < notifs.length(); i++) {
            notificacoes.add(notifs.getJSONObject(i));
        }
    }

    private void atualizaLista() {
        ListaNotificacaoAdapter listaAdapter = new ListaNotificacaoAdapter(objetoPrincipal, notificacoes);
        listaNotificacaoRV.setAdapter(listaAdapter);
        listaNotificacaoRV.setLayoutManager(new LinearLayoutManager(objetoPrincipal));
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
//        boolean mesmo = false;
//
//        if (id == R.id.nav_notificacoes) {
//            mesmo = true;
//        }
//
//        return navDrawer.navigate(id, mesmo);

        if (id == R.id.nav_home) {
            goAct(findViewById(id), MainActivity.class);

        } else if (id == R.id.nav_bd) {
            goAct(findViewById(id), DadosAct.class);

        } else if (id == R.id.nav_graficos) {
            goAct(findViewById(id), GraficosAct.class);

        } else if (id == R.id.nav_notificacoes) {
            //goAct(findViewById(id), ListaNotificacoes.class);
            Log.i(TAG, "onNavigationItemSelected: Botão Notificações");
        } else if (id == R.id.nav_salvar) {
            Log.i(TAG, "onNavigationItemSelected: Botão Salvar");
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void goAct(View v, Class act) {

        Intent intAct = new Intent(this, act);
        startActivity(intAct);
        this.finish();
    }

    @Override
    public void postResult(String result) {
        Log.i(TAG, "postResult: String param");
    }

    @Override
    public void postResult(JSONObject result) {

        Log.i(TAG, "postResult: result = " + result.toString());

        String pedido = result.optString("pedido");
        JSONArray notifs = result.optJSONArray("lista notificacoes");

        switch (pedido) {
            case "lista notificacoes":
                synchronized (esperaResposta) {
                    try {
                        notificacoes.clear();
                        for (int i = 0; i < notifs.length(); i++) {
                            notificacoes.add(notifs.getJSONObject(i));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    esperaResposta.notifyAll();
                }
                break;
            default:
                Log.w(TAG, "postResult: error");
        }
    }

}