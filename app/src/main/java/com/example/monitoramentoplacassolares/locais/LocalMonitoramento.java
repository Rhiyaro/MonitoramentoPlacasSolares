package com.example.monitoramentoplacassolares.locais;

import android.content.Context;

import com.example.monitoramentoplacassolares.activities.MainActivity;
import com.example.monitoramentoplacassolares.adapters.PlacaAdapter;
import com.jjoe64.graphview.series.DataPoint;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LocalMonitoramento {
    public static final String TAG = "LocalMonitoramento";

    private final String nome;
    private final String codigo;
    private final String grandezas;
    private final List<PlacaMonitoramento> placas = new ArrayList<PlacaMonitoramento>();
    private PlacaMonitoramento placaMedia;

    private PlacaAdapter placasAdapter;

    public LocalMonitoramento(String nome, String codigo, String grandezas, int numPlacas) {
        this.nome = nome;
        this.codigo = codigo;
        this.grandezas = grandezas;

        criaPlacas(numPlacas);
    }

    private void criaPlacas(int numPlacas) {
        if (numPlacas > 1) {
            this.placaMedia = new PlacaMonitoramento("Média", "media", 0);
            this.placas.add(placaMedia);

            for (int i = 1; i <= numPlacas; i++) {
                this.placas.add(new PlacaMonitoramento("Placa " + i, "linha" + i, i));
            }
        } else if (numPlacas == 1) {
            this.placaMedia = new PlacaMonitoramento("Placa Principal", "media", 0);
            this.placas.add(placaMedia);
        }
    }

    public void criaAdapter(Context context) {
        placasAdapter = new PlacaAdapter(context, placas);
    }

    /**
     * Adiciona os dados às séries de cada placa do local
     *
     * @param dados JSONObject contendo os dados a serem adicionados
     */
    public void adicionaDataPoints(JSONObject dados) {
        String chave;
        JSONArray arrayAux;
        double media = 0, valor;

        /*
        Retira um Iterator contendo as chaves e itera sobre os itens enquanto houver
         */
        Iterator<String> chaves = dados.keys();
        while (chaves.hasNext()) {
            media = 0;
            chave = chaves.next();

            /*
            Checa se o dado é um JSONArray, significando que continha mais de um valor pelo tipo
            de dado (e.g temp1 e temp2)
             */
            arrayAux = dados.optJSONArray(chave);
            if (arrayAux == null) { // Se não for, adiciona o dado à placa 'Média' -> "dado pertence a todas as placas" (à matriz)
                placaMedia.adicionaPonto(chave, dados.optDouble(chave, 0.0));
            } else { // Se for, retira a média dos valores para adicionar na placa 'Média' e adiciona cada valor à sua placa
                for (int i = 0; i < arrayAux.length(); i++) {
                    valor = arrayAux.optDouble(i, 0.0);
                    media += valor;
                    if (placas.size() > 1) {
                        placas.get(i + 1).adicionaPonto(chave, valor);
                    }
                }
                media /= arrayAux.length();
                placaMedia.adicionaPonto(chave, media);
            }
        }
    }

    public void adicionaDataPointsStrings(String[] strValores, String[] valores) {

        int count, total, totalPlacas = placas.size(), idPlaca;
        String dado;
        //Checa se existe somente uma linha/placa
        if (totalPlacas == 1) {
            /*
                No caso de apenas uma linha/placa, irá retirar a média de informações que possuem
                mais de uma aparição e modificar as outras aparições de forma que não se repita o processo
                Por fim, adiciona os pontos à série da linha/placa
             */
            for (int i = 0; i < strValores.length; i++) {
                if (strValores[i].equals("adicionado")) continue;
                count = 1;
                total = Integer.parseInt(valores[i]);
                if (strValores[i].matches(".*\\d")) { //if (strValor sem tudo menos números != "")
                    for (int j = i + 1; j < strValores.length; j++) {
                        if (strValores[i].replaceAll("[0-9]*", "").matches(strValores[j].replaceAll("[0-9]*", ""))) {
                            total += Integer.parseInt(valores[j]);
                            count++;
                            strValores[j] = "adicionado";
                        }
                    }
                    //valores[i] = "" + total/count;
                    adicionaPoint(strValores[i], total / count, 0);
                } else {
                    //valores[i] = "" + total/count;
                    adicionaPoint(strValores[i], total, 0);
                }
            }
        } else {
            /*
                Se houver mais de uma linha/placa, adiciona as informações que possuem um número à linha/placa específica
                e retirar à média para adicionar à "Média". As informações que não possuem um número também são
                adicionadas à linha "Média"
             */
            for (int i = 0; i < strValores.length; i++) {
                String teste = valores[i].replaceAll("\\D*", "");
                if (strValores[i].equals("adicionado") || valores[i].replaceAll("\\D*", "").equals(""))
                    continue;
                count = 1;
                total = Integer.parseInt(valores[i]); // Valor inicial = primeira aparição de uma informação
                //Pré define a placa como "Média"
                idPlaca = 0;
                if (strValores[i].matches(".*\\d")) {
                    /*
                        Caso a informação seja de uma linha específica, troca o id pelo da linha
                        Então passa por toda a string adicionando as informações e juntando ao "total" da média
                        Por fim, adiciona à linha "Média"
                     */
                    dado = strValores[i].replaceAll("[0-9]*", "");

                    idPlaca = Integer.parseInt("0" + strValores[i].split("\\D+")[1]); // Retira o número da informação, correspondente ao id da linha/placa
                    adicionaPoint(dado, total, idPlaca);

                    for (int j = i + 1; j < strValores.length; j++) {
                        if (dado.matches(strValores[j].replaceAll("[0-9]*", ""))) {
                            adicionaPoint(dado, Integer.parseInt(valores[j]), Integer.parseInt("0" + strValores[j].split("\\D+")[1]));
                            total += Integer.parseInt(valores[j]);
                            count++;
                            strValores[j] = "adicionado";
                        }
                    }
                    adicionaPoint(dado, total / count, 0);
                } else {
                    adicionaPoint(strValores[i], total, idPlaca);
                }
            }
        }
    }

    public void adicionaPoint(String serie, int valor, int placaId) {
        if (serie.contains("luminosidade")) {
            placas.get(placaId).getSerieLumi().appendData(new DataPoint(MainActivity.grafX, valor), false, 100);
        } else if (serie.contains("tempPlaca")) {
            placas.get(placaId).getSerieTPlaca().appendData(new DataPoint(MainActivity.grafX, valor), false, 100);
        } else if (serie.contains("temp")) {
            placas.get(placaId).getSerieTemp().appendData(new DataPoint(MainActivity.grafX, valor), false, 100);
        } else if (serie.contains("chuva")) {
            placas.get(placaId).getSerieChuva().appendData(new DataPoint(MainActivity.grafX, valor), false, 100);
        } else if (serie.contains("tensao")) {
            placas.get(placaId).getSerieTensao().appendData(new DataPoint(MainActivity.grafX, valor), false, 100);
        } else if (serie.contains("pressao")) {
            placas.get(placaId).getSeriePressao().appendData(new DataPoint(MainActivity.grafX, valor), false, 100);
        } else if (serie.contains("umidade")) {
            placas.get(placaId).getSerieUmidade().appendData(new DataPoint(MainActivity.grafX, valor), false, 100);
        } else if (serie.contains("corrente")) {
            placas.get(placaId).getSerieCorrente().appendData(new DataPoint(MainActivity.grafX, valor), false, 100);
        }
    }

    @NotNull
    @Override
    public String toString() {
        return nome;
    }

    public String getNome() {
        return nome;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getGrandezas() {
        return grandezas;
    }

    public List<PlacaMonitoramento> getPlacas() {
        return placas;
    }

    public PlacaMonitoramento getPlacaMedia() {
        return placaMedia;
    }

    public PlacaAdapter getPlacasAdapter() {
        return placasAdapter;
    }
}
