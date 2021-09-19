package com.example.monitoramentoplacassolares.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
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
import com.example.monitoramentoplacassolares.locais.localMonitoramento;
import com.example.monitoramentoplacassolares.locais.placaMonitoramento;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;

import java.util.ArrayList;
import java.util.concurrent.Future;

public class FragmentValoresAtuais extends Fragment {

    public TextView[][] txtViewValores = new TextView[4][2];
    public TextView txtYGraf;
    public Switch switchAutoScroll;
    public Button[][] btStrDados = new Button[4][2];
    public GraphView graf;
    public Viewport viewport;

    private Spinner spLocal, spPlaca;

    private localMonitoramento localAtual;
    private placaMonitoramento placaAtual;
    public String grafAtual = "";

    public IAsyncHandler mHandler;
    public Future clienteFuture, ouvirFuture;

    public static ArrayList<localMonitoramento> locais = new ArrayList<localMonitoramento>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View inf = inflater.inflate(R.layout.fragment_valores_atuais, container, false);

        //Adiciona a lista de locais aos spinners de seleção
        spLocal = inf.findViewById(R.id.seletorLocal);
        spPlaca = inf.findViewById(R.id.seletorPlaca);

        if(locais.isEmpty()){
            locais.add(new localMonitoramento("CEFET-NI/RJ", "192.168.25.9", 12345, new placaMonitoramento("Placa Principal", 1)));
            locais.add(new localMonitoramento("Artigo", "192.168.25.9", 12345, new placaMonitoramento("Linha 1", 1), new placaMonitoramento("Linha 2", 2)));
        }
        localAtual = locais.get(0);
        placaAtual = localAtual.getPlacas().get(0);

        ArrayAdapter<String> locaisAdapter = new ArrayAdapter(this.getContext(), android.R.layout.simple_spinner_item, locais);
        locaisAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLocal.setAdapter(locaisAdapter);
        spLocal.setOnItemSelectedListener(selecaoLocal);

        ArrayAdapter<String> placasAdapter = new ArrayAdapter(this.getContext(), android.R.layout.simple_spinner_item, localAtual.getPlacas());
        placasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPlaca.setAdapter(placasAdapter);

        spPlaca.setOnItemSelectedListener(selecaoPlaca);

        //Fim da adição

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
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            //Toast.makeText(adapterView.getContext(), adapterView.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();

            localMonitoramento localSelecionado = FragmentValoresAtuais.locais.get(0);
//            for(localMonitoramento lcl : FragmentValoresAtuais.locais){
//                if(lcl.getNome().equals(adapterView.getItemAtPosition(i))){
//                    localSelecionado = lcl;
//                    break;
//                }
//            }
            Object aux = adapterView.getItemAtPosition(i);
            for (int j = 0; j < locais.size(); j++) {
                if(locais.get(j).equals(adapterView.getItemAtPosition(i))){
                    localSelecionado = locais.get(j);
                }
            }

            if(localSelecionado != localAtual){
                localAtual = localSelecionado;
                ArrayAdapter<String> placasAdapter = new ArrayAdapter(adapterView.getContext(), android.R.layout.simple_spinner_item, localSelecionado.getPlacas());
                placasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spPlaca.setAdapter(placasAdapter);

                spPlaca.setOnItemSelectedListener(selecaoPlaca);

                novoLocal(localAtual, mHandler);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    public AdapterView.OnItemSelectedListener selecaoPlaca = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            //Toast.makeText(adapterView.getContext(), adapterView.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
            placaMonitoramento placaSelecionada = localAtual.getPlacas().get(0);
//            for(placaMonitoramento plc : localAtual.getPlacas()){
//                if(plc.equals(adapterView.getItemAtPosition(i))){
//                    placaSelecionada = plc;
//                    break;
//                }
//            }
            Object aux = adapterView.getItemAtPosition(i);
            for (int j = 0; j < localAtual.getPlacas().size(); j++) {
                if(localAtual.getPlacas().get(j).equals(adapterView.getItemAtPosition(i))){
                    placaSelecionada = localAtual.getPlacas().get(j);
                }
            }

            if(!placaSelecionada.equals(placaAtual)){
                placaAtual = placaSelecionada;
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    public void novoLocal(localMonitoramento novoLocal, IAsyncHandler mHandler){
        clienteFuture.cancel(true);
//        ouvirFuture.cancel(true);

        RunnableCliente runnableCliente = new RunnableCliente(mHandler, "ultimos dados", novoLocal.getNome());
        ouvirFuture = runnableCliente.getOuvirFuture();
        clienteFuture = MainActivity.executorServiceCached.submit(runnableCliente);
    }

    public localMonitoramento getLocalAtual() {
        return localAtual;
    }

    public placaMonitoramento getPlacaAtual() {
        return placaAtual;
    }
}