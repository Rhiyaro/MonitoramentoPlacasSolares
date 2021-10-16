package com.example.monitoramentoplacassolares;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import com.example.monitoramentoplacassolares.conexao.RunnableCliente;
import com.example.monitoramentoplacassolares.configFirebase.MyFirebaseMessagingService;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.monitoramentoplacassolares.conexao.IAsyncHandler;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity implements IAsyncHandler, NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {
    public static final String TAG = "MainActivity";

    //public static String phoneToken;

    public MyFirebaseMessagingService msgServ = new MyFirebaseMessagingService();

    private Boolean sair;
    private LineGraphSeries<DataPoint> serieLumi, serieTPlaca, serieTensao, serieCorrente, seriePressao,
            serieTemp, serieUmidade, serieChuva;
    private Toolbar tb;
    private int idBtGraf;
    public static int x = 0;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Fragment[] fragments;
    private DrawerLayout drawer;

    public static ExecutorService executorServiceCached = Executors.newCachedThreadPool();

    public static RunnableCliente Cliente;

    private FragmentValoresAtuais fragValAtuais;
    private String ultimoLocal = "", ultimaPlaca = "";


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        tb = findViewById(R.id.toolbar);

        setSupportActionBar(tb);

        tabLayout = findViewById(R.id.tabLayout);

        fragments = new Fragment[2];
        fragments[0] = new FragmentValoresAtuais();
        fragments[1] = new Fragment();

        fragValAtuais = (FragmentValoresAtuais) fragments[0];

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), getResources().getStringArray(R.array.tabTitles), fragments));

        tabLayout.setupWithViewPager(viewPager);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, tb, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        JSONObject pacoteComunicacao = new JSONObject();

        try {
            pacoteComunicacao.put("acao", "comunicar");
            pacoteComunicacao.put("pedido", "ultimos dados");
            //pacoteComunicacao.put("local", "CEFET");
        } catch (JSONException e) {
            Log.e(TAG, "onCreate: ", e);
        }

        MainActivity.Cliente.setPacoteConfig(pacoteComunicacao);
        RunnableCliente.setmHandler(this);
        RunnableCliente.setmHandlerAnt(this);


        // Configura e roda com o cliente padrão, recebendo dados do "CEFET"


        //RunnableCliente runnableCliente = new RunnableCliente(MainActivity.this, "ultimos dados", "CEFET");//, ((FragmentValoresAtuais)fragments[0]).getLocalAtual().getNome()
        //RunnableCliente runnableCliente = new RunnableCliente(MainActivity.this, pacoteComunicacao, "ultimos dados", "CEFET");
        fragValAtuais.clienteFuture = executorServiceCached.submit(Cliente);
        fragValAtuais.mHandler = (IAsyncHandler) this;

        // Inicia os gráficos
        serieLumi = new LineGraphSeries<>();
        serieTPlaca = new LineGraphSeries<>();
        serieTensao = new LineGraphSeries<>();
        serieCorrente = new LineGraphSeries<>();
        seriePressao = new LineGraphSeries<>();
        serieTemp = new LineGraphSeries<>();
        serieUmidade = new LineGraphSeries<>();
        serieChuva = new LineGraphSeries<>();

        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "mps");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        sair = false;
        idBtGraf = R.id.btLuminosidade;

    }

    public void goAct(View v, Class act) {

        Intent intAct = new Intent(this, act);
        startActivity(intAct);
        this.finish();
    }

    /**
     * Irá colocar os valores do vetor 'valor' nos seus devidos textViews baseado no nome
     * existente na mesma posição em 'strValor'
     * Coloca os valores nas respectivas placas, se for o caso
     *
     * @param strValor vetor de strings com os labels dos dados
     * @param valor    vetor de strings contendo o valor dos dados
     * @param idPlaca  id da placa a qual os dados se referem
     */
    @SuppressLint("SetTextI18n")
    private void setValoresPlaca(String[] strValor, String[] valor, int idPlaca) {
        //TODO: Refazer o método para utilizar os JSON Objects -> setValoresJSON


        /*
          Reseta todos os textViews para o caso de um local ter algum tipo de dado que outro
          não tem
         */
        for (int i = 0; i < fragValAtuais.txtViewValores.length; i++) {
            for (int j = 0; j < fragValAtuais.txtViewValores[0].length; j++) {
                fragValAtuais.txtViewValores[i][j].setText("      --- ");
            }
        }

        /*
          Checa se existe somente uma linha/placa. Nesse caso, irá retirar a média de informações
          que possuem mais de uma aparição e adicionar ao primeiro caso. Também modifica as outras
          aparições de forma que não se repita o processo
         */
        int count, total, totalPlacas = fragValAtuais.getLocalAtual().getPlacas().size();
        if (totalPlacas == 1 || idPlaca == -1) {
            for (int i = 0; i < strValor.length - 1; i++) {
                if (strValor[i].equals("adicionado")) continue;
                count = 1;
                total = Integer.parseInt(valor[i]);
                for (int j = i + 1; j < strValor.length; j++) {
                    if (strValor[i].replaceAll("[0-9]*", "").matches(strValor[j].replaceAll("[0-9]*", ""))) {
                        total += Integer.parseInt(valor[j]);
                        count++;
                        strValor[j] = "adicionado";
                    }
                }
                valor[i] = "" + total / count;
            }
        }

        for (int i = 0; i < strValor.length; i++) {
            if (strValor[i].contains("luminosidade"))
                fragValAtuais.txtViewValores[0][0].setText("      " + valor[i] + " L");

            else if (strValor[i].contains("tempPlaca"))
                fragValAtuais.txtViewValores[1][0].setText("      " + valor[i] + " °C");

            else if (strValor[i].contains("temp")) {
                if (strValor[i].matches(".*\\d")) {
                    if (strValor[i].contains(fragValAtuais.getPlacaAtual().getId() + "") || idPlaca == -1) {
                        fragValAtuais.txtViewValores[1][1].setText("      " + valor[i] + " °C");
                    }
                } else {
                    fragValAtuais.txtViewValores[1][1].setText("      " + valor[i] + " °C");
                }
            } else if (strValor[i].contains("chuva"))
                fragValAtuais.txtViewValores[3][1].setText("      " + valor[i] + " Ch");

            else if (strValor[i].contains("tensao")) {
                if (strValor[i].matches(".*\\d")) {
                    if (strValor[i].contains(fragValAtuais.getPlacaAtual().getId() + "") || idPlaca == -1) {
                        fragValAtuais.txtViewValores[2][0].setText("      " + valor[i] + " V");
                    }
                } else {
                    fragValAtuais.txtViewValores[2][0].setText("      " + valor[i] + " V");
                }
            } else if (strValor[i].contains("pressao"))
                fragValAtuais.txtViewValores[0][1].setText("      " + valor[i] + " Pa");

            else if (strValor[i].contains("umidade"))
                fragValAtuais.txtViewValores[2][1].setText("      " + valor[i] + " %");

            else if (strValor[i].contains("corrente")) {
                if (strValor[i].matches(".*\\d")) {
                    if (strValor[i].contains(fragValAtuais.getPlacaAtual().getId() + "") || idPlaca == -1) {
                        fragValAtuais.txtViewValores[3][0].setText("      " + valor[i] + " A");
                    }
                } else {
                    fragValAtuais.txtViewValores[3][0].setText("      " + valor[i] + " A");
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void setValoresJSON(JSONObject dados) {
//        txtViewValores[0][0] = inf.findViewById(R.id.txtValorLuminosidade);
//        txtViewValores[1][0] = inf.findViewById(R.id.txtValorTPlaca);
//        txtViewValores[2][0] = inf.findViewById(R.id.txtTensao);
//        txtViewValores[3][0] = inf.findViewById(R.id.txtCorrente);
//        txtViewValores[0][1] = inf.findViewById(R.id.txtPressao);
//        txtViewValores[1][1] = inf.findViewById(R.id.txtTemp);
//        txtViewValores[2][1] = inf.findViewById(R.id.txtUmidade);
//        txtViewValores[3][1] = inf.findViewById(R.id.txtChuva);

        for (int i = 0; i < fragValAtuais.txtViewValores.length; i++) {
            for (int j = 0; j < fragValAtuais.txtViewValores[i].length; j++) {
                setTxtView(fragValAtuais.txtViewValores[i][j], dados, fragValAtuais.txtViewValores[i][j].getHint().toString());
            }
        }

//        setTxtView(fragValAtuais.txtViewValores[0][0], dados, "luminosidade");
//        setTxtView(fragValAtuais.txtViewValores[1][0], dados, "tempPlaca");
//        setTxtView(fragValAtuais.txtViewValores[2][0], dados, "tensao");
//        setTxtView(fragValAtuais.txtViewValores[3][0], dados, "corrente");
//        setTxtView(fragValAtuais.txtViewValores[0][1], dados, "pressao");
//        setTxtView(fragValAtuais.txtViewValores[1][1], dados, "temp");
//        setTxtView(fragValAtuais.txtViewValores[2][1], dados, "umidade");
//        setTxtView(fragValAtuais.txtViewValores[3][1], dados, "chuva");
    }

    private void setTxtView(TextView txtView, JSONObject conjunto, String dado) {
        /*
        Recupera o id da placa selecionada no momento.
         */
        int idPlaca = fragValAtuais.getPlacaAtual().getId();
        double valor = 0;
        JSONArray arrayAux;
        try {
            // se a fonte dos dados tem o tipo de dado
            if (conjunto.has(dado)) {
                // se a amostra do tipo de dados for um array significa que existe mais de uma entrada
                // se não, o dado pertence à matriz toda, isto é, a todas as placas
                arrayAux = conjunto.optJSONArray(dado);
                if (arrayAux == null) {
                    txtView.setText(conjunto.opt(dado).toString());
                } else {
                    // se a placa seleciona for a placaMédia, retira a média dos valores
                    // se não, retira o valor referente a placa específica
                    if (idPlaca == 0) {
                        for (int i = 0; i < arrayAux.length(); i++) {
                            valor += arrayAux.optDouble(i);
                        }
                        valor /= arrayAux.length();
                        txtView.setText(String.format("%s", valor));
                    } else {
                        txtView.setText(conjunto.optJSONArray(dado).get(idPlaca).toString());
                    }
                }
            } else {
                txtView.setText("---");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método que trata das respostas recebidas do servidor
     * Faz o pré-processamento da resposta para ficar nos vetores necessários; chama o método
     * 'setValoresPlaca' para atualizar a UI
     *
     * @param result resultado retornado de algum método rodando em paralelo
     */
    @Override
    public void postResult(String result) {
        Log.i(TAG, "postResult: " + result);
        String[] resultados = result.split(";");
        final String[] strValor = resultados[0].split(",");
        final String[] strValor2 = resultados[0].split(",");
        final String[] valor = resultados[1].split(",");
        final String[] valor2 = resultados[1].split(",");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setValoresPlaca(strValor, valor, fragValAtuais.getPlacaAtual().getId());
            }
        });

        /*
            Checa se o local selecionado é o mesmo do ciclo anterior. Se for, apenas adiciona os
            dataPoints e ajeita o gráfico. Se não, atualiza o localAtual e placaAtual
         */
        if (!ultimoLocal.equals("") && fragValAtuais.getLocalAtual().getCodigo().equals(ultimoLocal)) {
            fragValAtuais.getLocalAtual().adicionaDataPointsStrings(strValor2, valor2);
            ajeitaGrafico();
        } else if (!fragValAtuais.getLocalAtual().getCodigo().equals(ultimoLocal) || !fragValAtuais.getPlacaAtual().getCodigo().equals(ultimaPlaca)) {
            if (!ultimoLocal.equals("") && !ultimaPlaca.equals("")) {
                attSeriePlaca();
            }
            ultimoLocal = fragValAtuais.getLocalAtual().getCodigo();
            ultimaPlaca = fragValAtuais.getPlacaAtual().getCodigo();
        }
    }

    @Override
    public void postResult(final JSONObject result) {
        Log.i(TAG, "\npostResult: result= " + result.toString() + "\n" + fragValAtuais.getLocalAtual().getNome());
        /*
        TODO: Configurar ações utilizando o JSONObject
         */
        try {
            String pedido = result.getString("pedido"); // Recupera o pedido que foi feito para receber esta resposta

            //Log.i(TAG, "postResult: dados= "+dadosLocalAtual.toString());

            switch (pedido) {
                case "ultimos dados":
                    final JSONObject dadosLocalAtual = result.getJSONObject("dados").getJSONObject(fragValAtuais.getLocalAtual().getCodigo());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setValoresJSON(dadosLocalAtual);
                        }
                    });
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + pedido);
            }
        } catch (JSONException e) {
            Log.e(TAG, "postResult: JSONException", e);
        }

        /*
        TODO: Configurar atualização dos gráficos
         */
    }

    public void ajeitaGrafico() {
        x++;

        if (((FragmentValoresAtuais) fragments[0]).viewport != null && ((FragmentValoresAtuais) fragments[0]).switchAutoScroll.isChecked()) {
            ((FragmentValoresAtuais) fragments[0]).viewport.setMaxY(((FragmentValoresAtuais) fragments[0]).viewport.getMaxY(true) * 1.05);
            ((FragmentValoresAtuais) fragments[0]).viewport.setMinY(((FragmentValoresAtuais) fragments[0]).viewport.getMinY(true) - ((FragmentValoresAtuais) fragments[0]).viewport.getMinY(true) * 0.05);
            if (x > 20) {
                ((FragmentValoresAtuais) fragments[0]).viewport.setMinX(((FragmentValoresAtuais) fragments[0]).viewport.getMaxX(true) - 20);
                ((FragmentValoresAtuais) fragments[0]).viewport.setMaxX(((FragmentValoresAtuais) fragments[0]).viewport.getMaxX(true));
            } else {
                ((FragmentValoresAtuais) fragments[0]).viewport.setMinX(0);
                ((FragmentValoresAtuais) fragments[0]).viewport.setMaxX(20);
            }
        }
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

    @SuppressLint({"SetTextI18n", "NonConstantResourceId"})
    private void attSerie() {
        fragValAtuais.graf.removeAllSeries();

        switch (idBtGraf) {
            case R.id.btLuminosidade:
                fragValAtuais.graf.addSeries(serieLumi);
                fragValAtuais.txtYGraf.setText("Luminosidade");
                break;

            case R.id.btTPlaca:
                fragValAtuais.graf.addSeries(serieTPlaca);
                fragValAtuais.txtYGraf.setText("Temp. Placa");
                break;

            case R.id.btTensao:
                fragValAtuais.graf.addSeries(serieTensao);
                fragValAtuais.txtYGraf.setText("Tensão");
                break;

            case R.id.btCorrente:
                fragValAtuais.graf.addSeries(serieCorrente);
                fragValAtuais.txtYGraf.setText("Corrente");
                break;

            case R.id.btPressao:
                fragValAtuais.graf.addSeries(seriePressao);
                fragValAtuais.txtYGraf.setText("Pressão");
                break;

            case R.id.btTemp:
                fragValAtuais.graf.addSeries(serieTemp);
                if (fragValAtuais.getLocalAtual().getNome().contains("CEFET")) {
                    fragValAtuais.txtYGraf.setText("Temp. Caixa");
                } else if (fragValAtuais.getLocalAtual().getNome().contains("Artigo")) {
                    fragValAtuais.txtYGraf.setText("Temp. Ambiente");
                }
                break;

            case R.id.btUmidade:
                fragValAtuais.graf.addSeries(serieUmidade);
                fragValAtuais.txtYGraf.setText("Umidade");
                break;

            case R.id.btChuva:
                fragValAtuais.graf.addSeries(serieChuva);
                fragValAtuais.txtYGraf.setText("Intens. Chuva");
                break;

        }

        if (fragValAtuais.viewport != null) {
            fragValAtuais.viewport.setMaxY(fragValAtuais.viewport.getMaxY(true) * 1.2);
            fragValAtuais.viewport.setMinY(fragValAtuais.viewport.getMinY(true) - fragValAtuais.viewport.getMinY(true) * 0.2);
        }
    }

    @SuppressLint({"SetTextI18n", "NonConstantResourceId"})
    private void attSeriePlaca() {
        fragValAtuais.graf.removeAllSeries();

        switch (idBtGraf) {
            case R.id.btLuminosidade:
                if (fragValAtuais.getPlacaAtual().getSerieLumi().isEmpty()) {
                    fragValAtuais.graf.addSeries(fragValAtuais.getLocalAtual().getPlacas().get(0).getSerieLumi());
                } else {
                    ((FragmentValoresAtuais) fragments[0]).graf.addSeries(fragValAtuais.getPlacaAtual().getSerieLumi());
                }
                fragValAtuais.grafAtual = "Luminosidade";
                break;

            case R.id.btTPlaca:
                if (fragValAtuais.getPlacaAtual().getSerieTPlaca().isEmpty()) {
                    fragValAtuais.graf.addSeries(fragValAtuais.getLocalAtual().getPlacas().get(0).getSerieTPlaca());
                } else {
                    ((FragmentValoresAtuais) fragments[0]).graf.addSeries(fragValAtuais.getPlacaAtual().getSerieTPlaca());
                }
                fragValAtuais.grafAtual = "Temp. Placa";
                break;

            case R.id.btTensao:
                if (fragValAtuais.getPlacaAtual().getSerieTensao().isEmpty()) {
                    fragValAtuais.graf.addSeries(fragValAtuais.getLocalAtual().getPlacas().get(0).getSerieTensao());
                } else {
                    ((FragmentValoresAtuais) fragments[0]).graf.addSeries(fragValAtuais.getPlacaAtual().getSerieTensao());
                }
                fragValAtuais.grafAtual = "Tensão";
                break;

            case R.id.btCorrente:
                if (fragValAtuais.getPlacaAtual().getSerieCorrente().isEmpty()) {
                    fragValAtuais.graf.addSeries(fragValAtuais.getLocalAtual().getPlacas().get(0).getSerieCorrente());
                } else {
                    ((FragmentValoresAtuais) fragments[0]).graf.addSeries(fragValAtuais.getPlacaAtual().getSerieCorrente());
                }
                fragValAtuais.grafAtual = "Corrente";
                break;

            case R.id.btPressao:
                if (fragValAtuais.getPlacaAtual().getSeriePressao().isEmpty()) {
                    fragValAtuais.graf.addSeries(fragValAtuais.getLocalAtual().getPlacas().get(0).getSeriePressao());
                } else {
                    ((FragmentValoresAtuais) fragments[0]).graf.addSeries(fragValAtuais.getPlacaAtual().getSeriePressao());
                }
                fragValAtuais.grafAtual = "Pressão";
                break;

            case R.id.btTemp:
                if (fragValAtuais.getPlacaAtual().getSerieTemp().isEmpty()) {
                    fragValAtuais.graf.addSeries(fragValAtuais.getLocalAtual().getPlacas().get(0).getSerieTemp());
                } else {
                    ((FragmentValoresAtuais) fragments[0]).graf.addSeries(fragValAtuais.getPlacaAtual().getSerieTemp());
                }
                if (fragValAtuais.getLocalAtual().getNome().contains("CEFET")) {
                    fragValAtuais.grafAtual = "Temp. Caixa";
                } else if (fragValAtuais.getLocalAtual().getNome().contains("Artigo")) {
                    fragValAtuais.grafAtual = "Temp. Ambiente";
                }
                break;

            case R.id.btUmidade:
                if (fragValAtuais.getPlacaAtual().getSerieUmidade().isEmpty()) {
                    fragValAtuais.graf.addSeries(fragValAtuais.getLocalAtual().getPlacas().get(0).getSerieUmidade());
                } else {
                    ((FragmentValoresAtuais) fragments[0]).graf.addSeries(fragValAtuais.getPlacaAtual().getSerieUmidade());
                }
                fragValAtuais.grafAtual = "Umidade";
                break;

            case R.id.btChuva:
                if (fragValAtuais.getPlacaAtual().getSerieChuva().isEmpty()) {
                    fragValAtuais.graf.addSeries(fragValAtuais.getLocalAtual().getPlacas().get(0).getSerieChuva());
                } else {
                    ((FragmentValoresAtuais) fragments[0]).graf.addSeries(fragValAtuais.getPlacaAtual().getSerieChuva());
                }
                fragValAtuais.grafAtual = "Intens. Chuva";
                break;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fragValAtuais.txtYGraf.setText(fragValAtuais.grafAtual);
            }
        });

        if (((FragmentValoresAtuais) fragments[0]).viewport != null) {
            ((FragmentValoresAtuais) fragments[0]).viewport.setMaxY(((FragmentValoresAtuais) fragments[0]).viewport.getMaxY(true) * 1.2);
            ((FragmentValoresAtuais) fragments[0]).viewport.setMinY(((FragmentValoresAtuais) fragments[0]).viewport.getMinY(true) - ((FragmentValoresAtuais) fragments[0]).viewport.getMinY(true) * 0.2);
        }
    }

    public void escolheGraf(View view) {
        idBtGraf = view.getId();
        //attSerie();
        attSeriePlaca();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_bd) {
            goAct(findViewById(id), DadosAct.class);

        } else if (id == R.id.nav_graficos) {
            goAct(findViewById(id), GraficosAct.class);

        } else if (id == R.id.nav_salvar) {

        }

        //DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
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
