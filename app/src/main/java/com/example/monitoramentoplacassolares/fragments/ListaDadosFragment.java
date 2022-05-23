package com.example.monitoramentoplacassolares.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.monitoramentoplacassolares.R;
import com.example.monitoramentoplacassolares.activities.MainActivity;
import com.example.monitoramentoplacassolares.adapters.DadosDataAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ListaDadosFragment extends Fragment {
    public static final String TAG = "ListaDadosFragment";
    // TODO: Passar lista de dados para cá

    private TableRow rowCabecalho;
    private RecyclerView rvDados;
    private List<JSONObject> listaDados = Collections.synchronizedList(new ArrayList<>());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lista_dados, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rowCabecalho = view.findViewById(R.id.tblRow_Cabecalho);

        rvDados = view.findViewById(R.id.rcDados);
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        rvDados.setLayoutManager(layoutManager);
    }

    public void carregaDados(JSONArray dados) throws JSONException {
        if (dados == null) {
            Log.i(TAG, "carregaDados: dados is null");
            return;
        }

        listaDados.clear();
        for (int i = 0; i < dados.length(); i++) {
            listaDados.add(dados.getJSONObject(i));
        }
    }

    public void criaCabecalho(JSONObject dados) {
        Iterator<String> keysIt = dados.keys();
        String key;
        boolean evenView; // O ultimo a ser inserido foi o 0-ésimo (par)

        rowCabecalho.removeAllViews();

        // Inicia data separado, garantindo ser o primeiro
        addTextViewCabecalho("Data", false); // evenView == false
        evenView = true; // 2o text view

        List<String> listaTitulos = Arrays.asList(MainActivity.titulosDados);
        List<String> listaNomes = Arrays.asList(MainActivity.nomesDados);

        while (keysIt.hasNext()) {
            key = keysIt.next();
            if (key.equals("data")) continue;

            int indexTitulo = listaTitulos.indexOf(key);
            String nomeDado = listaNomes.get(indexTitulo);

            addTextViewCabecalho(nomeDado, evenView);
            evenView = !evenView;
        }
    }

    private void addTextViewCabecalho(String text, boolean evenColumn) {
        TextView tvToAdd = new TextView(getContext());

        rowCabecalho.addView(tvToAdd);

        tvToAdd.setText(text);
//        tvToAdd.setHeight(TableRow.LayoutParams.WRAP_CONTENT);

        int dps = 95;
        final float scale = getResources().getDisplayMetrics().density;
        tvToAdd.getLayoutParams().width = (int) (dps * scale + 0.5f); // pixels

        tvToAdd.setTextColor(Color.BLACK);
        if (evenColumn) {
            tvToAdd.setBackgroundResource(R.color.cinzaClaro);
        } else {
            tvToAdd.setBackgroundResource(R.color.cinzaEscuro);
        }
        tvToAdd.setGravity(Gravity.CENTER);

    }

    public void atualizaListaDados() {
        DadosDataAdapter listaAdapter = new DadosDataAdapter(getContext(), listaDados);
        rvDados.setAdapter(listaAdapter);
        rvDados.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}