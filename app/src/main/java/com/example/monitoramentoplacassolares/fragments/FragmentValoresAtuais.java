package com.example.monitoramentoplacassolares.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.example.monitoramentoplacassolares.MainActivity;
import com.example.monitoramentoplacassolares.R;
import com.example.monitoramentoplacassolares.conexao.IAsyncHandler;
import com.example.monitoramentoplacassolares.conexao.RunnableCliente;
import com.example.monitoramentoplacassolares.conexao.TarefaMensagem;
import com.example.monitoramentoplacassolares.locais.LocalMonitoramento;
import com.example.monitoramentoplacassolares.locais.PlacaMonitoramento;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Future;

public class FragmentValoresAtuais extends Fragment implements IAsyncHandler {
    public static final String TAG = "FragmentValoresAtuais";

    private FragmentValoresAtuais objetoPrincipal;
    private Activity actAux = new Activity();

    public TextView[][] txtViewValores = new TextView[4][2];
    public TextView txtYGraf;
    public Switch switchAutoScroll;
    public Button[][] btStrDados = new Button[4][2];
    public GraphView graf;
    public Viewport viewport;

    //TODO: Trocar por RecyclerView
    private Spinner spLocal, spPlaca;

    private LocalMonitoramento localAtual;
    private PlacaMonitoramento placaAtual;
    public String grafAtual = "";

    private String ultimoLocal = "";
    private String ultimaPlaca = "";

    public IAsyncHandler mHandler;
    public Future clienteFuture, comunicarFuture;

    public ArrayList<LocalMonitoramento> locais = new ArrayList<LocalMonitoramento>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View inf = inflater.inflate(R.layout.fragment_valores_atuais, container, false);
        objetoPrincipal = this;

        //Adiciona a lista de locais aos spinners de seleção
        spLocal = inf.findViewById(R.id.seletorLocal);
        spPlaca = inf.findViewById(R.id.seletorPlaca);

        try {
            atualizaLocais();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        txtViewValores[0][0] = inf.findViewById(R.id.txtValorLuminosidade);
        txtViewValores[1][0] = inf.findViewById(R.id.txtValorTPlaca);
        txtViewValores[2][0] = inf.findViewById(R.id.txtTensao);
        txtViewValores[3][0] = inf.findViewById(R.id.txtCorrente);
        txtViewValores[0][1] = inf.findViewById(R.id.txtPressao);
        txtViewValores[1][1] = inf.findViewById(R.id.txtTemp);
        txtViewValores[2][1] = inf.findViewById(R.id.txtUmidade);
        txtViewValores[3][1] = inf.findViewById(R.id.txtChuva);

        txtYGraf = inf.findViewById(R.id.txtYGraf);

        btStrDados[0][0] = inf.findViewById(R.id.btLuminosidade);
        btStrDados[1][0] = inf.findViewById(R.id.btTPlaca);
        btStrDados[2][0] = inf.findViewById(R.id.btTensao);
        btStrDados[3][0] = inf.findViewById(R.id.btCorrente);
        btStrDados[0][1] = inf.findViewById(R.id.btPressao);
        btStrDados[1][1] = inf.findViewById(R.id.btTemp);
        btStrDados[2][1] = inf.findViewById(R.id.btUmidade);
        btStrDados[3][1] = inf.findViewById(R.id.btChuva);

        switchAutoScroll = inf.findViewById(R.id.switchautoscroll);

        graf = inf.findViewById(R.id.graficoTempoReal);

        viewport = graf.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(200);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(20);
        viewport.setScrollable(true);
        viewport.setScalable(true);
        viewport.setScalableY(true);
        viewport.setScrollableY(true);

        return inf;
    }

    public AdapterView.OnItemSelectedListener selecaoLocal = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(final AdapterView<?> adapterView, View view, final int i, long l) {

            LocalMonitoramento localSelecionado = locais.get(0);
            Object aux = adapterView.getItemAtPosition(i);
            for (int j = 0; j < locais.size(); j++) {
                if (locais.get(j).equals(adapterView.getItemAtPosition(i))) {
                    localSelecionado = locais.get(j);
                }
            }

            if (localSelecionado != localAtual) {
                localAtual = localSelecionado;
                ArrayAdapter<String> placasAdapter = new ArrayAdapter(adapterView.getContext(), android.R.layout.simple_spinner_item, localSelecionado.getPlacas());
                placasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spPlaca.setAdapter(placasAdapter);

                spPlaca.setOnItemSelectedListener(selecaoPlaca);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    public AdapterView.OnItemSelectedListener selecaoPlaca = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(final AdapterView<?> adapterView, View view, final int i, long l) {
            //TODO: Atualizar seleção de placa do dropList

            PlacaMonitoramento placaSelecionada = localAtual.getPlacas().get(0);
            for (int j = 0; j < localAtual.getPlacas().size(); j++) {
                if (localAtual.getPlacas().get(j).equals(adapterView.getItemAtPosition(i))) {
                    placaSelecionada = localAtual.getPlacas().get(j);
                }
            }
            if (!placaSelecionada.equals(placaAtual)) {
                placaAtual = placaSelecionada;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private void atualizaLocais() throws JSONException {
//        if(locais.isEmpty()){
//            locais.add(new LocalMonitoramento("CEFET-RJ/NI", "cefet","192.168.25.9", 12345, new PlacaMonitoramento("Placa Principal", "main",0)));
//            locais.add(new LocalMonitoramento("Artigo", "artigo","192.168.25.9", 12345, new PlacaMonitoramento("Linha 1", "linha1",1), new PlacaMonitoramento("Linha 2", "linha2",2)));
//        }
        JSONObject pedido = new JSONObject();
        pedido.put("acao", "locais");
        pedido.put("pedido", "locais");

        TarefaMensagem tarefaMsg = new TarefaMensagem(objetoPrincipal, pedido);
        tarefaMsg.configuraConexao(MainActivity.Cliente.getSocket(),
                MainActivity.Cliente.getObjIn(),
                MainActivity.Cliente.getObjOut());

        MainActivity.Cliente.novaTarefa(tarefaMsg);
    }

    private void atualizaAdapters() {
        ArrayAdapter<String> locaisAdapter = new ArrayAdapter(objetoPrincipal.getContext(), android.R.layout.simple_spinner_item, locais);
        locaisAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLocal.setAdapter(locaisAdapter);
        spLocal.setOnItemSelectedListener(selecaoLocal);

        ArrayAdapter<String> placasAdapter = new ArrayAdapter(objetoPrincipal.getContext(), android.R.layout.simple_spinner_item, localAtual.getPlacas());
        placasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPlaca.setAdapter(placasAdapter);

        spPlaca.setOnItemSelectedListener(selecaoPlaca);
    }

    public void novoLocal(LocalMonitoramento novoLocal) {
        //TODO: Atualizar controle de local e placa atual

    }

    public LocalMonitoramento getLocalAtual() {
        return localAtual;
    }

    public PlacaMonitoramento getPlacaAtual() {
        return placaAtual;
    }

    public String getUltimoLocal() {
        return ultimoLocal;
    }

    public void setUltimoLocal(String ultimoLocal) {
        this.ultimoLocal = ultimoLocal;
    }

    public String getUltimaPlaca() {
        return ultimaPlaca;
    }

    public void setUltimaPlaca(String ultimaPlaca) {
        this.ultimaPlaca = ultimaPlaca;
    }

    @Override
    public void postResult(String result) {

    }

    @Override
    public void postResult(JSONObject result) {
        Log.i(TAG, "postResult: " + result.toString());
        String pedido = result.optString("pedido");

        switch (pedido) {
            case "locais":
                JSONArray jsArrLocais = result.optJSONArray("locais");
                if (jsArrLocais == null) {
                    Log.i(TAG, "postResult: jsArrLocais is null");
                    break;
                }

                for (int i = 0; i < jsArrLocais.length(); i++) {
                    JSONObject jsLocal = jsArrLocais.optJSONObject(i);
                    String nomeLocal = jsLocal.optString("local");
                    String codigoLocal = jsLocal.optString("local").toLowerCase();
                    int numPlacas = jsLocal.optInt("matrizes");

                    LocalMonitoramento local = new LocalMonitoramento(nomeLocal, codigoLocal,
                            RunnableCliente.host_atual, RunnableCliente.porta_atual, numPlacas);

                    locais.add(local);
                }

                localAtual = locais.get(0);
                placaAtual = localAtual.getPlacas().get(0);

                actAux.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        atualizaAdapters();
                    }
                });

                break;
        }
    }
}