package com.example.monitoramentoplacassolares.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.monitoramentoplacassolares.R;
import com.example.monitoramentoplacassolares.conexao.RunnableCliente;
import com.example.monitoramentoplacassolares.configFirebase.MyFirebaseMessagingService;
import com.example.monitoramentoplacassolares.excecoes.HttpRequestException;
import com.example.monitoramentoplacassolares.httpcomm.MpsHttpClient;
import com.example.monitoramentoplacassolares.httpcomm.MpsHttpServerInfo;
import com.example.monitoramentoplacassolares.locais.LocalMonitoramento;
import com.example.monitoramentoplacassolares.locais.PlacaMonitoramento;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.monitoramentoplacassolares.adapters.MyFragmentPagerAdapter;
import com.example.monitoramentoplacassolares.fragments.FragmentValoresAtuais;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Response;
import okhttp3.ResponseBody;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {
    public static final String TAG = "MainActivity";

    //TODO: Limpar códigos de teste

    public MyFirebaseMessagingService msgServ = new MyFirebaseMessagingService();

    private NavigationDrawer navDrawer;

    private Boolean sair;
    private int idBtGraf;
    public static int grafX = 0;

    public static ExecutorService executorServiceCached = Executors.newCachedThreadPool();

    private final ScheduledExecutorService executorComunicacao = Executors.newSingleThreadScheduledExecutor();

    public static RunnableCliente runnableCliente;

    public FragmentValoresAtuais fragValAtuais;
    public String ultimoLocal = "", ultimaPlaca = "";
    public String grafAtual;
    private View viewGrafAtual;

    public static String[] titulosDados;
    public static String[] nomesDados;

    private Toolbar tb;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Fragment[] fragments;
    private DrawerLayout drawer;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        tb = findViewById(R.id.toolbarMainAct);
        setSupportActionBar(tb);

        titulosDados = getResources().getStringArray(R.array.tituloColunasDados);
        nomesDados = getResources().getStringArray(R.array.nomeColunasDados);

        //navDrawer = new NavigationDrawer(this);

        tabLayout = findViewById(R.id.mainActTabLayout);

        fragments = new Fragment[2];
        fragments[0] = new FragmentValoresAtuais();
        //fragments[1] = new Fragment(); //Retirado para retirar a aba "Produção"

        fragValAtuais = (FragmentValoresAtuais) fragments[0];

        viewPager = findViewById(R.id.mainActViewPager);
        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(),
                getResources().getStringArray(R.array.tabTitles),
                fragments));

        tabLayout.setupWithViewPager(viewPager);

        // Init Toolbar e Drawer


        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, tb, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // end Init

        executorComunicacao.scheduleAtFixedRate(
                this::comunicacaoHttp, 0,
                MpsHttpClient.REQUEST_TIMEOUT_SECONDS,
                TimeUnit.SECONDS);

        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "mps");
        if (!folder.exists()) {
            boolean mk = folder.mkdirs();
            if (mk)
                Log.i(TAG, "onCreate: folder.mkdirs() returned true");
        }

        sair = false;
        idBtGraf = R.id.btLuminosidade;
        viewGrafAtual = findViewById(R.id.btLuminosidade);
    }

    private void comunicacaoHttp() {
        try (Response comunicacaoResponse = MpsHttpClient.instacia().doGet(MpsHttpServerInfo.PATH_ULT_DADOS)) {
            ResponseBody responseBody = comunicacaoResponse.body();
            String responseBodyStr;
            if (responseBody != null) {
                responseBodyStr = responseBody.string();
                JSONObject respostaJson = new JSONObject(responseBodyStr);
                gerenciaUltimosDados(respostaJson);
            }
        } catch (HttpRequestException | IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void gerenciaUltimosDados(JSONObject resposta) throws JSONException {
        JSONObject dados = resposta.getJSONObject("dados");

        if (fragValAtuais.getLocalAtual() == null) {
            return;
        }

        JSONObject dadosLocalAtual = dados.getJSONObject(fragValAtuais.getLocalAtual().getNome());

        setValoresJSON(dadosLocalAtual);

        // TODO: Melhorar controle dos gráficos -> otimizar métodos
        boolean mudou = false;
        if (!fragValAtuais.getLocalAtual().getCodigo().equals(ultimoLocal) ||
                !fragValAtuais.getPlacaAtual().getCodigo().equals(ultimaPlaca)) {
            if (!ultimoLocal.equals("") && !ultimaPlaca.equals("")) {
                mudou = true;
            }
            ultimoLocal = fragValAtuais.getLocalAtual().getCodigo();
            ultimaPlaca = fragValAtuais.getPlacaAtual().getCodigo();
        }
        adicionaDataPoints(dados);
        grafX++;
        if (fragValAtuais.switchAutoScroll.isChecked()) ajeitaGrafico();
        if (mudou) runOnUiThread(() -> escolheGraf(viewGrafAtual));
    }

    /**
     * Seta os valores da tela principal para os últimos dados recebidos
     *
     * @param dados objeto contendo os dados que devem ser mostrados
     */
    @SuppressLint("SetTextI18n")
    public void setValoresJSON(JSONObject dados) {
        for (int i = 0; i < fragValAtuais.txtViewValores.length; i++) {
            for (int j = 0; j < fragValAtuais.txtViewValores[i].length; j++) {
                setTxtView(fragValAtuais.txtViewValores[i][j], dados,
                        fragValAtuais.txtViewValores[i][j].getHint().toString());
            }
        }
    }

    /**
     * Seta o valor em seu devido TxtView
     *
     * @param txtView  Qual TxtView irá mostrar o valor
     * @param conjunto Objeto contendo os dados
     * @param dado     Qual dado deve ser buscado no conjunto
     */
    private void setTxtView(final TextView txtView, JSONObject conjunto, String dado) {
        /*
        Recupera o id da placa selecionada no momento.
         */
        int idPlaca = fragValAtuais.getPlacaAtual().getId();
        double valor = 0;
        final String VALOR_A_SETAR;
        JSONArray arrayAux;
        try {
            // se a fonte dos dados tem o tipo de dado, i.e. se o local selecionado tem o
            // tipo de dado (e.g checa se o local coleta dados de tensão)
            if (conjunto.has(dado)) {
                // se a amostra do tipo de dados for um array significa que existe mais de uma entrada
                // se não, o dado pertence à matriz toda, isto é, a todas as placas
                arrayAux = conjunto.optJSONArray(dado);
                if (arrayAux == null) {
                    VALOR_A_SETAR = conjunto.optString(dado, "SEM-DADO");
                } else {
                    // se a placa seleciona for a placaMédia, retira a média dos valores
                    // se não, retira o valor referente a placa específica
                    if (idPlaca == 0) {
                        for (int i = 0; i < arrayAux.length(); i++) {
                            valor += arrayAux.optDouble(i);
                        }
                        valor /= arrayAux.length();
                        VALOR_A_SETAR = String.format(new Locale("en", "US"),
                                "%.2f", valor);
                    } else {
                        VALOR_A_SETAR = arrayAux.getString(idPlaca - 1);
                    }
                }
            } else { // caso o local não possua o tipo de dado, seta como "nulo"
                VALOR_A_SETAR = "---";
            }
            runOnUiThread(() -> txtView.setText(VALOR_A_SETAR));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void adicionaDataPoints(JSONObject dados) {
        Iterator<LocalMonitoramento> locaisIt = fragValAtuais.ListaLocais.iterator();
        JSONObject dadosLocal;
        LocalMonitoramento localAux;

        while (locaisIt.hasNext()) {
            localAux = locaisIt.next();
            try {
                dadosLocal = dados.getJSONObject(localAux.getNome());
                localAux.adicionaDataPoints(dadosLocal);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void ajeitaGrafico() {

        if (fragValAtuais.viewport != null) {
            fragValAtuais.viewport.setMaxY(fragValAtuais.viewport.getMaxY(true) * 1.01);
            fragValAtuais.viewport.setMinY(fragValAtuais.viewport.getMinY(true) -
                    fragValAtuais.viewport.getMinY(true) * 0.01);
            if (grafX > 20) {
                fragValAtuais.viewport.setMinX(fragValAtuais.viewport.getMaxX(true) - 20);
                fragValAtuais.viewport.setMaxX(fragValAtuais.viewport.getMaxX(true));
            } else {
                fragValAtuais.viewport.setMinX(0);
                fragValAtuais.viewport.setMaxX(20);
            }
        }
    }

    public void escolheGraf(View view) {
        if (view == null) {
            view = findViewById(R.id.btLuminosidade);
        }
        idBtGraf = view.getId();
        viewGrafAtual = view;
        String btHint = ((Button) view).getHint().toString();
//        Log.i(TAG, "escolheGraf: " + btHint);
//        attSeriePlaca();
        atualizaGrafico(btHint);
        ajeitaGrafico();
    }

    private void atualizaGrafico(String serieEscolhida) {

        fragValAtuais.graf.removeAllSeries();

        PlacaMonitoramento placaAtual = fragValAtuais.getPlacaAtual();

//        List<LineGraphSeries<DataPoint>> graphSeries = placaAtual.getLineGraphSeries();
//        LineGraphSeries<DataPoint> lineGraph;
//
//        for (int i = 0; i < graphSeries.size(); i++) {
//            lineGraph = graphSeries.get(i);
//            if (lineGraph.getTitle().equals(serieEscolhida)) {
//                fragValAtuais.grafAtual = placaAtual.getTitulosSeries().get(i);
//
//                if (lineGraph.isEmpty()) {
//                    runOnUiThread(() -> fragValAtuais.graf.addSeries(
//                            fragValAtuais.getLocalAtual().getPlacaMedia().getLineGraphSeriesByTitle(serieEscolhida)));
//                } else {
//                    runOnUiThread(() -> fragValAtuais.graf.addSeries(
//                            fragValAtuais.getPlacaAtual().getLineGraphSeriesByTitle(serieEscolhida)));
//                }
//
//                break;
//            }
//        }

        List<PlacaMonitoramento.SeriePlaca> series = placaAtual.getSeries();
        PlacaMonitoramento.SeriePlaca seriePlaca;

        for (int i = 0; i < series.size(); i++) {
            seriePlaca = series.get(i);
            if (seriePlaca.getTitulo().equals(serieEscolhida)) {
                grafAtual = seriePlaca.getNome();

                if (seriePlaca.getSerie().isEmpty()){
                    PlacaMonitoramento.SeriePlaca seriePlacaMedia = fragValAtuais.getLocalAtual().getPlacaMedia().getSerieByTitle(serieEscolhida);
                    runOnUiThread(() -> fragValAtuais.graf.addSeries(seriePlacaMedia.getSerie()));
                } else {
                    PlacaMonitoramento.SeriePlaca finalSeriePlaca = seriePlaca;
                    runOnUiThread(() -> fragValAtuais.graf.addSeries(finalSeriePlaca.getSerie()));
                }
            }
        }

        runOnUiThread(() -> fragValAtuais.txtYGraf.setText(grafAtual));
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Log.i(TAG, "onNavigationItemSelected: Botão Home");
        } else if (id == R.id.nav_bd) {
            goAct(findViewById(id), DadosAct.class);

        } else if (id == R.id.nav_graficos) {
            //goAct(findViewById(id), GraficosAct.class);
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

    @SuppressWarnings("rawtypes")
    public void goAct(View v, Class act) {
        Intent intAct = new Intent(this, act);
        intAct.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intAct);
//        this.finish();
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

    /*public void gera(View gerador){
        ((FragmentValoresAtuais) fragments[0]).graf.takeSnapshotAndShare(this, "exampleGraph", "GraphViewSnapshot");
//        int permissionCheck= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, permissionCheck);
//        ((FragmentValoresAtuais) fragments[0]).graf.buildDrawingCache();
//        Bitmap bitmap = ((FragmentValoresAtuais) fragments[0]).graf.getDrawingCache();
//        String filename = "pippo.png";
//        File sd = Environment.getExternalStorageDirectory();
//        File dest = new File(sd, filename);
//        try {
//            FileOutputStream out = new FileOutputStream(dest);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
//            out.flush();
//            out.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    public static void getAppToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        MainActivity.phoneToken = token;

                        // Log and toast
                        String msg = "token: " + token;//getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, msg);
//                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }*/

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Toast.makeText(adapterView.getContext(), adapterView.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


}
