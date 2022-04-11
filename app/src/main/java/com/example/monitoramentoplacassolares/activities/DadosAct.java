package com.example.monitoramentoplacassolares.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.monitoramentoplacassolares.R;
import com.example.monitoramentoplacassolares.adapters.AntigoDadosAdapter;
import com.example.monitoramentoplacassolares.adapters.DadosDataAdapter;
import com.example.monitoramentoplacassolares.adapters.ListaNotificacaoAdapter;
import com.example.monitoramentoplacassolares.excecoes.HttpRequestException;
import com.example.monitoramentoplacassolares.httpcomm.MpsHttpClient;
import com.example.monitoramentoplacassolares.httpcomm.MpsHttpServerInfo;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Response;

public class DadosAct extends AppCompatActivity implements AdapterView.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "DadosAct";

    private Spinner spMes, spDia;
    //TODO: Adaptar para HTTP
    private RecyclerView rcDados;
    private final String Dados = null;
    private boolean sair;

    private Calendar calendar;

    private TextView tvDataHoraInicio;
    private TextView tvDataHoraFim;

    private int ano, mes, dia;
    private int hora, minuto;

    private List<JSONObject> listaDados = Collections.synchronizedList(new ArrayList<>());
    private DadosAct objetoPrincipal;
    private JSONObject dadoCabecalho = new JSONObject();
    private final Object esperaResposta = new Object();

    private NavigationDrawer navDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dados);
        objetoPrincipal = this;

//        navDrawer = new NavigationDrawer(this);
//        MenuItem salvar = findViewById(R.id.nav_salvar);
//        salvar.setEnabled(true);

        sair = false;

        // Init Toolbar e Drawer
        Toolbar tb = findViewById(R.id.toolbarDadosAct);
        setSupportActionBar(tb);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, tb, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // end

        // Inicia TxTs Seleção Data
        calendar = Calendar.getInstance();

        tvDataHoraInicio = findViewById(R.id.txtSelecionaDataHoraInicioDados);
        tvDataHoraInicio.setOnClickListener(v -> mostrarSelecaoData(tvDataHoraInicio));

        tvDataHoraFim = findViewById(R.id.txtSelecionaDataHoraTerminoDados);
        tvDataHoraFim.setOnClickListener(v -> mostrarSelecaoData(tvDataHoraFim));
        // end

        rcDados = findViewById(R.id.rcDados);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcDados.setLayoutManager(layoutManager);

        AntigoDadosAdapter mAdapter = new AntigoDadosAdapter(Dados);
        rcDados.setAdapter(mAdapter);
    }

    private void mostrarSelecaoData(TextView tvDataHora) {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            ano = year;
            calendar.set(Calendar.MONTH, month);
            mes = month + 1;
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            dia = dayOfMonth;

            mostrarSelecaoHora(tvDataHora);
        };

        new DatePickerDialog(DadosAct.this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void mostrarSelecaoHora(TextView tvDataHora) {
        TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            hora = hourOfDay;
            calendar.set(Calendar.MINUTE, minute);
            minuto = minute;

            tvDataHora.setText(String.format(new Locale("pt", "BR"),
                    "%d-%02d-%02d %02d:%02d:00",
                    ano, mes, dia, hora, minuto));
        };

        new TimePickerDialog(DadosAct.this,
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true)
                .show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //int Dia = spDia.getSelectedItemPosition();
        String Dia = (String) spDia.getSelectedItem();
        String Mes = (String) spMes.getSelectedItem();

        TextView txtData = findViewById(R.id.textView);
        txtData.setText(String.format("d: %s  m: %s  a: 2019", Dia, Mes));

        //Toast.makeText(this, parent.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
    }

    private void salvarDados() {

        String pathName = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "mps" + File.separator + "dados";

        File f = new File(pathName);
        CSVWriter writer;

        if (!f.exists()) {
            f.mkdirs();
        }

        // File exist
        writer = null;
        if (f.exists() && !f.isDirectory()) {
            FileWriter mFileWriter = null;
            try {
                mFileWriter = new FileWriter(pathName + File.separator + spDia.getSelectedItem() + spMes.getSelectedItem() + ".csv");
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer = new CSVWriter(mFileWriter);
        } else {
            try {
                writer = new CSVWriter(new FileWriter(pathName + File.separator + spDia.getSelectedItem() + spMes.getSelectedItem() + ".csv"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String[] data = Dados.split(";");

        for (int i = 0; i < data.length; i++) {
            writer.writeNext(data[i].split(";"), false);
        }


        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(DadosAct.this, pathName + '/' + spDia.getSelectedItem() + spMes.getSelectedItem() + ".csv", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (sair) {
                super.finish();
            } else
                Toast.makeText(this, "Pressione novamente para sair.", Toast.LENGTH_SHORT).show();
            sair = true;
        }
    }

    public void btRequestAction(View v) {
        //TODO: Debuggar

        MainActivity.executorServiceCached.submit(() -> {
            pedeDados();
            runOnUiThread(this::atualizaListaDados);
            runOnUiThread(() -> criaCabecalho(dadoCabecalho));
        });

        /*String data;
        data = spDia.getSelectedItem() + "-";
        data += spMes.getSelectedItem();

        //Cliente task = new Cliente(DadosAct.this);
        //task.execute("DADOS DATA " + data);
        JSONObject pacotePedido = new JSONObject();
        try {
            pacotePedido.put("acao", "dados data");
            pacotePedido.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

//        MainActivity.Cliente.setPacoteConfig(pacotePedido);
//        MainActivity.executorServiceCached.submit(MainActivity.Cliente);
//        TarefaMensagem tarefaMensagem = new TarefaMensagem(this, pacotePedido);
//        MainActivity.runnableCliente.novaTarefa(tarefaMensagem);
    }

    private void pedeDados() {
        //TODO: Adicionar seleção de local
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("local", "VR");
        String dataInicio = tvDataHoraInicio.getText().toString().replace(" ", "+");
        queryParams.put("inicio", dataInicio);
        String dataFim = tvDataHoraFim.getText().toString().replace(" ", "+");
        queryParams.put("fim", dataFim);

        try (Response dadosResponse =
                     MpsHttpClient.instacia().doGet(MpsHttpServerInfo.PATH_DADOS_DATA, queryParams)) {
            String responseBodyStr = dadosResponse.body().string();

            int statusCode = dadosResponse.code();
            if (statusCode == MpsHttpClient.HTTP_OK_RESPONSE) {
                JSONArray dadosJson = new JSONArray(responseBodyStr);

                if (dadosJson.length() == 0) {
                    runOnUiThread(() -> Toast.makeText(this,
                            "Sem dados entre essas datas", Toast.LENGTH_SHORT).show());
                    return;
                }

                dadoCabecalho = dadosJson.getJSONObject(0);
                //runOnUiThread(() -> criaCabecalho(dadoCabecalho));
                carregaDados(dadosJson);
            } else {
                Log.i(TAG, "pedeDados: Erro: Código de resposta inesperado: "
                        + statusCode + "\nCorpo mensagem: " + responseBodyStr);
            }
        } catch (HttpRequestException | IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void criaCabecalho(JSONObject dados) {
        Iterator<String> keysIt = dados.keys();
        String key;
        boolean evenView = false; // O ultimo a ser inserido foi o 0-ésimo (par)

        TableRow cabecalho = findViewById(R.id.tblRow_Cabecalho);
        cabecalho.removeAllViews();

        // Inicia data separado, garantindo ser o primeiro
        addTextViewCabecalho("Data", evenView);
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
        TableRow cabecalho = findViewById(R.id.tblRow_Cabecalho);

        TextView tvToAdd = new TextView(this);

        cabecalho.addView(tvToAdd);

        tvToAdd.setText(text);
//        tvToAdd.setHeight(TableRow.LayoutParams.WRAP_CONTENT);

        int dps = 95;
        final float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (dps * scale + 0.5f);
        tvToAdd.getLayoutParams().width = pixels;

        tvToAdd.setTextColor(Color.BLACK);
        if (evenColumn) {
            tvToAdd.setBackgroundResource(R.color.cinzaClaro);
        } else {
            tvToAdd.setBackgroundResource(R.color.cinzaEscuro);
        }
        tvToAdd.setGravity(Gravity.CENTER);

    }

    private void carregaDados(JSONArray dados) throws JSONException {
        if (dados == null) {
            Log.i(TAG, "carregaDados: dados is null");
            return;
        }

        listaDados.clear();
        for (int i = 0; i < dados.length(); i++) {
            listaDados.add(dados.getJSONObject(i));
        }
    }

    private void atualizaListaDados() {
        DadosDataAdapter listaAdapter = new DadosDataAdapter(objetoPrincipal, listaDados);
        rcDados.setAdapter(listaAdapter);
        rcDados.setLayoutManager(new LinearLayoutManager(objetoPrincipal));
    }

    public void goAct(View v, Class act) {
        Intent intAct = new Intent(this, act);
        startActivity(intAct);
        this.finish();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
//        boolean mesmo = false;
//
//        if (id == R.id.nav_bd) {
//            mesmo = true;
//        } else if (id == R.id.nav_salvar){
//            salvarDados();
//        }
//
//        return navDrawer.navigate(id, mesmo);

        if (id == R.id.nav_home) {
            goAct(findViewById(id), MainActivity.class);

        } else if (id == R.id.nav_bd) {
            Log.i(TAG, "onNavigationItemSelected: Botão Dados");

        } else if (id == R.id.nav_graficos) {
            goAct(findViewById(id), GraficosAct.class);

        } else if (id == R.id.nav_notificacoes) {
            goAct(findViewById(id), ListaNotificacoes.class);
        } else if (id == R.id.nav_salvar) {
            Log.i(TAG, "onNavigationItemSelected: Botão Salvar");
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}