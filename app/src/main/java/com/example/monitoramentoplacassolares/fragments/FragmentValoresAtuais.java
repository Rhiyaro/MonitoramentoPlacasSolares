package com.example.monitoramentoplacassolares.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.example.monitoramentoplacassolares.activities.MainActivity;
import com.example.monitoramentoplacassolares.R;
import com.example.monitoramentoplacassolares.adapters.LocalAdapter;
import com.example.monitoramentoplacassolares.adapters.PlacaAdapter;
import com.example.monitoramentoplacassolares.excecoes.HttpRequestException;
import com.example.monitoramentoplacassolares.httpcomm.MpsHttpClient;
import com.example.monitoramentoplacassolares.httpcomm.MpsHttpServerInfo;
import com.example.monitoramentoplacassolares.locais.LocalMonitoramento;
import com.example.monitoramentoplacassolares.locais.PlacaMonitoramento;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Response;
import okhttp3.ResponseBody;

public class FragmentValoresAtuais extends Fragment {
    public static final String TAG = "FragmentValoresAtuais";

    private FragmentValoresAtuais objetoPrincipal;
    private final Activity actAux = new Activity();

    public TextView[][] txtViewValores = new TextView[4][2];
    private TextView txtMedidaCorrente;

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

    public ArrayList<LocalMonitoramento> locais = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View inf = inflater.inflate(R.layout.fragment_valores_atuais, container, false);
        objetoPrincipal = this;

        //Adiciona a lista de locais aos spinners de seleção
        spLocal = inf.findViewById(R.id.seletorLocal);
        spPlaca = inf.findViewById(R.id.seletorPlaca);

        MainActivity.executorServiceCached.submit(this::atualizaLocaisHttp);

        // Txts Valores
        txtViewValores[0][0] = inf.findViewById(R.id.txtValorLuminosidade);
        txtViewValores[1][0] = inf.findViewById(R.id.txtValorTPlaca);
        txtViewValores[2][0] = inf.findViewById(R.id.txtTensao);
        txtViewValores[3][0] = inf.findViewById(R.id.txtCorrente);
        txtViewValores[0][1] = inf.findViewById(R.id.txtPressao);
        txtViewValores[1][1] = inf.findViewById(R.id.txtTemp);
        txtViewValores[2][1] = inf.findViewById(R.id.txtUmidade);
        txtViewValores[3][1] = inf.findViewById(R.id.txtChuva);

        // Txts Medidas
        txtMedidaCorrente = inf.findViewById(R.id.txtMedidaCorrente);

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



    private void atualizaGrandezas() {
        String[] configsGrandezas = localAtual.getGrandezas().split(";");
        if (configsGrandezas.length > 0) {
            for (String config : configsGrandezas) {
                if (config.equals("")) continue;
                String grandeza = config.split("=")[0];
                String unidade = config.split("=")[1];
                mudaTxtGrandeza(grandeza, unidade);
            }
        }
    }

    private void mudaTxtGrandeza(String grandeza, String unidade) {
        if ("I".equals(grandeza)) {
            txtMedidaCorrente.setText(unidade);
        }
    }

    private void atualizaLocaisHttp() {
        try (Response locaisResponse = MpsHttpClient.instacia().doGet(MpsHttpServerInfo.PATH_LOCAIS)) {
            ResponseBody responseBody = locaisResponse.body();
            String responseBodyStr = responseBody != null ? responseBody.string() : "vazio";
            int statusCode = locaisResponse.code();
            if (statusCode == MpsHttpClient.HTTP_OK_RESPONSE && !responseBodyStr.equals("vazio")) {
                JSONObject locaisJson = new JSONObject(responseBodyStr);
                carregaLocais(locaisJson);
                actAux.runOnUiThread(this::atualizaListaLocais);
            } else {
                Log.i(TAG, "atualizaLocaisHttp: Erro: Código de resposta inesperado: " + statusCode);
            }
        } catch (HttpRequestException | IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void atualizaListaLocais() {
        atualizaAdapters();
    }

    private void atualizaAdapters() {
//        ArrayAdapter<String> locaisAdapter = new ArrayAdapter(objetoPrincipal.getContext(), android.R.layout.simple_spinner_item, locais);
//        locaisAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spLocal.setAdapter(locaisAdapter);
//        spLocal.setOnItemSelectedListener(selecaoLocal);
//
//        ArrayAdapter<String> placasAdapter = new ArrayAdapter(objetoPrincipal.getContext(), android.R.layout.simple_spinner_item, localAtual.getPlacas());
//        placasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spPlaca.setAdapter(placasAdapter);
//        spPlaca.setOnItemSelectedListener(selecaoPlaca);

        LocalAdapter localAdapter = new LocalAdapter(objetoPrincipal.getContext(), locais);
        spLocal.setAdapter(localAdapter);
        spLocal.setOnItemSelectedListener(selecaoLocal);

        PlacaAdapter placaAdapter = new PlacaAdapter(objetoPrincipal.getContext(), localAtual.getPlacas());
        spPlaca.setAdapter(placaAdapter);
        spPlaca.setOnItemSelectedListener(selecaoPlaca);
    }

    private final AdapterView.OnItemSelectedListener selecaoLocal = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            LocalMonitoramento localSelecionado = (LocalMonitoramento) parent.getItemAtPosition(position);
            if (!localSelecionado.equals(localAtual)){
                localAtual = localSelecionado;
                atualizaGrandezas();

                placaAtual = localSelecionado.getPlacas().get(0);

                PlacaAdapter placaAdapter = new PlacaAdapter(objetoPrincipal.getContext(), localAtual.getPlacas());
                spPlaca.setAdapter(placaAdapter);
                spPlaca.setOnItemSelectedListener(selecaoPlaca);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private final AdapterView.OnItemSelectedListener selecaoPlaca = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            PlacaMonitoramento placaSelecionada = (PlacaMonitoramento) parent.getItemAtPosition(position);
            if (!placaSelecionada.equals(placaAtual)) {
                placaAtual = placaSelecionada;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    public LocalMonitoramento getLocalAtual() {
        return localAtual;
    }

    public PlacaMonitoramento getPlacaAtual() {
        return placaAtual;
    }

    private void carregaLocais(JSONObject locaisJson) throws JSONException {
        JSONArray jsArrLocais = locaisJson.getJSONArray("locais");

        for (int i = 0; i < jsArrLocais.length(); i++) {
            JSONObject jsLocal = jsArrLocais.optJSONObject(i);
            String nomeLocal = jsLocal.optString("local");
            String codigoLocal = nomeLocal.toLowerCase();
            String grandezas = jsLocal.optString("grandezas", "");
            int numPlacas = jsLocal.optInt("matrizes");

            LocalMonitoramento local = new LocalMonitoramento(nomeLocal, codigoLocal,
                    grandezas, numPlacas);

            locais.add(local);
        }

        localAtual = locais.get(0);
        placaAtual = localAtual.getPlacas().get(0);
    }
}