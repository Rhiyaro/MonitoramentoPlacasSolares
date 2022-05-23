package com.example.monitoramentoplacassolares.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.monitoramentoplacassolares.R;
import com.example.monitoramentoplacassolares.adapters.AntigoDadosAdapter;
import com.example.monitoramentoplacassolares.adapters.DadosDataAdapter;
import com.example.monitoramentoplacassolares.adapters.LocalAdapter;
import com.example.monitoramentoplacassolares.adapters.MyFragmentStateAdapter;
import com.example.monitoramentoplacassolares.excecoes.HttpRequestException;
import com.example.monitoramentoplacassolares.fragments.FragmentValoresAtuais;
import com.example.monitoramentoplacassolares.fragments.ListaDadosFragment;
import com.example.monitoramentoplacassolares.httpcomm.MpsHttpClient;
import com.example.monitoramentoplacassolares.httpcomm.MpsHttpServerInfo;
import com.example.monitoramentoplacassolares.locais.LocalMonitoramento;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import okhttp3.ResponseBody;

public class DadosAct extends AppCompatActivity implements AdapterView.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "DadosAct";

    //TODO: Limpar códigos antigos

    private Spinner spMes, spDia;

    private RecyclerView rcDados;
    private final String Dados = null;
    private boolean sair;

    private Calendar calendar;

    private DrawerLayout drawer;

    private TextView tvDataHoraInicio;
    private TextView tvDataHoraFim;

    private Spinner spLocal;
    private LocalMonitoramento localEscolhido;

    private int ano, mes, dia;
    private int hora, minuto;

    private boolean visuGrafico = false;

    private List<JSONObject> listaDados = Collections.synchronizedList(new ArrayList<>());
    private DadosAct objetoPrincipal;
    private JSONObject dadoCabecalho = new JSONObject();
    private final Object esperaResposta = new Object();

    private ListaDadosFragment listaDadosFragment;

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

        drawer = findViewById(R.id.drawer_layout);
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

        // Inicia seletor local
        spLocal = findViewById(R.id.seletorLocalDados);

        LocalAdapter localAdapter = new LocalAdapter(this, FragmentValoresAtuais.ListaLocais);
        spLocal.setAdapter(localAdapter);
        spLocal.setOnItemSelectedListener(selecaoLocal);
        // end

//        rcDados = findViewById(R.id.rcDados);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        rcDados.setLayoutManager(layoutManager);
        listaDadosFragment = new ListaDadosFragment();

        List<Fragment> fragmentsToAdd = new ArrayList<>();
        fragmentsToAdd.add(listaDadosFragment);

        ViewPager2 viewPager = findViewById(R.id.vwPagerDados);
        viewPager.setAdapter(new MyFragmentStateAdapter(getSupportFragmentManager(),
                getLifecycle(), fragmentsToAdd));
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

    private final AdapterView.OnItemSelectedListener selecaoLocal = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            LocalMonitoramento localSelecionado = (LocalMonitoramento) parent.getItemAtPosition(position);
            if (!localSelecionado.equals(localEscolhido)){
                localEscolhido = localSelecionado;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

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
        MainActivity.executorServiceCached.submit(() -> {
            pedeDados();
            runOnUiThread(() -> {
                listaDadosFragment.atualizaListaDados();
                listaDadosFragment.criaCabecalho(dadoCabecalho);
            });
        });
    }

    private void pedeDados() {
        //TODO: Checar seleção de local

        //Adiciona os campos da requisição
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("local", localEscolhido.getCodigo());
        String dataInicio = tvDataHoraInicio.getText().toString().replace(" ", "+");
        queryParams.put("inicio", dataInicio);
        String dataFim = tvDataHoraFim.getText().toString().replace(" ", "+");
        queryParams.put("fim", dataFim);

        try (Response dadosResponse =
                     MpsHttpClient.instacia().doGet(MpsHttpServerInfo.PATH_DADOS_DATA, queryParams)) {
            ResponseBody responseBody = dadosResponse.body();
            String responseBodyStr = responseBody != null ? responseBody.string() : "vazio";

            if (responseBodyStr.equals("vazio")) {
                throw new HttpRequestException("Corpo de resposta vazio");
            }

            int statusCode = dadosResponse.code();
            if (statusCode == MpsHttpClient.HTTP_OK_RESPONSE) {
                JSONArray dadosJson = new JSONArray(responseBodyStr);

                if (dadosJson.length() == 0) {
                    runOnUiThread(() -> Toast.makeText(this,
                            "Sem dados entre essas datas", Toast.LENGTH_SHORT).show());
                    return;
                }

                dadoCabecalho = dadosJson.getJSONObject(0);

                listaDadosFragment.carregaDados(dadosJson);
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
        tvToAdd.getLayoutParams().width = (int) (dps * scale + 0.5f); // pixels

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
        intAct.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intAct);
//        this.finish();
    }

    @SuppressLint("SetTextI18n")
    public void btMudaVisualizacao(View v) {
        //TODO: Implementar troca para gráfico (duas abas e seleção de qual dado) com ViewPager
        Button btMudaVisu = findViewById(R.id.btMudaVisualizacaoDados);
        LinearLayout grafHead = findViewById(R.id.linLayoutGrafDadosHead);
        visuGrafico = !visuGrafico;
        if (visuGrafico) {
            btMudaVisu.setText("Lista");
            grafHead.setVisibility(View.VISIBLE);
        } else {
            btMudaVisu.setText("Gráfico");
            grafHead.setVisibility(View.GONE);
        }
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
//            goAct(findViewById(id), GraficosAct.class);
            Toast.makeText(this, "Não implementado", Toast.LENGTH_SHORT).show();

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