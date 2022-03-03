package com.example.monitoramentoplacassolares.conexao;

import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.TextView;

import com.example.monitoramentoplacassolares.activities.MainActivity;
import com.example.monitoramentoplacassolares.fragments.FragmentValoresAtuais;
import com.example.monitoramentoplacassolares.locais.LocalMonitoramento;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class GerenciadorDados {
    public static final String TAG = "GerenciadorDados";

    private final MainActivity mainact;
    FragmentValoresAtuais fragValAtuais;

    public GerenciadorDados(MainActivity mainact, FragmentValoresAtuais fragValAtuais){
        this.mainact = mainact;
        this.fragValAtuais = fragValAtuais;
    }

    /**
     * Gerencia a resposta obtido do servidor ao fazer um pedido
     * @param resposta Objeto contendo a resposta
     */
    public void gerenciarResposta(JSONObject resposta){
        Log.i(TAG, "->\ngerenciaResposta: result= " + resposta.toString() + "\n" + fragValAtuais.getLocalAtual().getNome());
        try {
            String pedido = resposta.getString("pedido"); // Recupera o pedido que foi feito para receber esta resposta

            switch (pedido) {
                case "ultimos dados":
                    final JSONObject dados = resposta.getJSONObject("dados");
                    final JSONObject dadosLocalAtual = dados.getJSONObject(fragValAtuais.getLocalAtual().getCodigo());
                    mainact.setValoresJSON(dadosLocalAtual);

                    MainActivity.executorServiceCached.submit(new Runnable() {
                        @Override
                        public void run() {
                            String ultimoLocal = fragValAtuais.getUltimoLocal();
                            String ultimaPlaca = fragValAtuais.getUltimaPlaca();
                            if (!ultimoLocal.equals("") && fragValAtuais.getLocalAtual().getCodigo().equals(ultimoLocal)) {
                                adicionaDataPoints(dados);
                            } else if (!fragValAtuais.getLocalAtual().getCodigo().equals(ultimoLocal) || !fragValAtuais.getPlacaAtual().getCodigo().equals(ultimaPlaca)) {
                                if (!ultimoLocal.equals("") && !ultimaPlaca.equals("")) {
                                    mainact.attSeriePlaca();
                                }
                                fragValAtuais.setUltimoLocal(fragValAtuais.getLocalAtual().getCodigo());
                                fragValAtuais.setUltimaPlaca(fragValAtuais.getPlacaAtual().getCodigo());
                            }
                            mainact.ajeitaGrafico();
                        }
                    });
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + pedido);
            }
        } catch (JSONException e) {
            Log.e(TAG, "postResult: JSONException", e);
        }
    }

    /**
     * Seta os valores da tela principal para os últimos dados recebidos
     * @param dados objeto contendo os dados que devem ser mostrados
     */
    @SuppressLint("SetTextI18n")
    public void setValoresJSON(JSONObject dados) {
        for (int i = 0; i < fragValAtuais.txtViewValores.length; i++) {
            for (int j = 0; j < fragValAtuais.txtViewValores[i].length; j++) {
                setTxtView(fragValAtuais.txtViewValores[i][j], dados, fragValAtuais.txtViewValores[i][j].getHint().toString());
            }
        }
    }

    /**
     * Seta o valor em seu devido TxtView
     * @param txtView Qual TxtView irá mostrar o valor
     * @param conjunto Objeto contendo os dados
     * @param dado Qual dado deve ser buscado no conjunto
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
            // se a fonte dos dados tem o tipo de dado, i.e. se o local selecionado tem o tipo de dado
            if (conjunto.has(dado)) {
                // se a amostra do tipo de dados for um array significa que existe mais de uma entrada
                // se não, o dado pertence à matriz toda, isto é, a todas as placas
                arrayAux = conjunto.optJSONArray(dado);
                if (arrayAux == null) {
                    VALOR_A_SETAR = conjunto.opt(dado).toString();
                } else {
                    // se a placa seleciona for a placaMédia, retira a média dos valores
                    // se não, retira o valor referente a placa específica
                    if (idPlaca == 0) {
                        for (int i = 0; i < arrayAux.length(); i++) {
                            valor += arrayAux.optDouble(i);
                        }
                        valor /= arrayAux.length();
                        VALOR_A_SETAR = String.format("%s", valor);
                    } else {
                        VALOR_A_SETAR = conjunto.optJSONArray(dado).get(idPlaca-1).toString();
                    }
                }
            } else { // caso o local não possua o tipo de dado, seta como "nulo"
                VALOR_A_SETAR = "---";
            }
            // atualiza os textViews pela uiThread
            mainact.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtView.setText(VALOR_A_SETAR);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adiciona os DataPoints nas séries das placas dos locais
     * @param dados Dados que serão colocaos nas séries
     */
    private void adicionaDataPoints(JSONObject dados){
        Iterator<LocalMonitoramento> locaisIt = fragValAtuais.locais.iterator();
        JSONObject dadosLocal;
        LocalMonitoramento localAux;

        while(locaisIt.hasNext()){
            localAux = locaisIt.next();
            dadosLocal = dados.optJSONObject(localAux.getCodigo());
            if(dadosLocal != null){
                localAux.adicionaDataPoints(dadosLocal);
            }
        }
    }
}
