package com.example.monitoramentoplacassolares.locais;

import android.util.Log;

import com.example.monitoramentoplacassolares.MainActivity;
import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class localMonitoramento {
    public static final String TAG = "localMonitoramento";

    /*TODO: Reformular classe para ser criada a partir do objeto lido do
            banco de dados
      TODO: Remontar métodos para trabalhar com JSON Object
     */

    private String nome;
    private String ip;
    private int port;
    private List<placaMonitoramento> placas = new ArrayList<placaMonitoramento>();

    public localMonitoramento(String nome, String ip, int port) {
        this.nome = nome;
        this.ip = ip;
        this.port = port;

        for(int i = 1; i<=10; i++){
            this.placas.add(new placaMonitoramento("Placa " + i, i));
        }
    }

    public localMonitoramento(String nome, String ip, int port, placaMonitoramento... placas) {
        this.nome = nome;
        this.ip = ip;
        this.port = port;
        if(placas.length > 1) this.placas.add(new placaMonitoramento("Média", -1));
        //this.placas = Arrays.asList(placas);
        Collections.addAll(this.placas, placas);
        Log.i(TAG, "constructor 2: " + this.placas.toString());
    }

    public localMonitoramento(String nome, String ip, int port, List<placaMonitoramento> placas) {
        this.nome = nome;
        this.ip = ip;
        this.port = port;
        if(placas.size() > 1) this.placas.add(new placaMonitoramento("Média", -1));
        this.placas = placas;
    }

    public void adicionaDataPoints(String[] strValores, String[] valores){

        /*
            Checa se existe somente uma linha/placa. Nesse caso, irá retirar a média de informações que possuem
            mais de uma aparição e adicionar ao primeiro caso. Também modifica as outras aparições de forma
            que não se repita o processo
         */
        int count, total, totalPlacas = placas.size();
        if(totalPlacas == 1){
            for (int i = 0; i<strValores.length-1; i++) {
                if(strValores[i].equals("adicionado")) continue;
                count = 1;
                total = Integer.parseInt(valores[i]);
                for (int j = i+1; j<strValores.length; j++) {
                    if(strValores[i].replaceAll("[0-9]*", "").matches(strValores[j].replaceAll("[0-9]*", ""))){
                        total += Integer.parseInt(valores[j]);
                        count++;
                        strValores[j] = "adicionado";
                    }
                }
                valores[i] = "" + total/count;
            }
        }

        for (int i = 0; i<strValores.length; i++) {
            /*
                Checa se a informação tem algum número, indicando que é de uma linha/placa especifica
                Caso tenha, irá adicionar a informação à placa correspondente, se não, adiciona à todas
                e diretamente à Média
             */
            int idPlacaAdd = totalPlacas == 1 ? 1 : -1;
            if(strValores[i].matches(".*\\d")){
                /*
                    Retira o número do meio da string, que irá corresponder ao ID
                    Adiciona-se o 0 no inicio para dar o split em qualquer caractere não dígito
                    Assim, a posição [1] do resultado é o número desejado, o ID e a posição na lista
                 */
                idPlacaAdd = Integer.parseInt("0" + strValores[i].split("\\D+")[1]);
            }

            placaMonitoramento placaAAdd = placas.get(idPlacaAdd);

            if (strValores[i].contains("luminosidade")){
                    placaAAdd.getSerieLumi().appendData(new DataPoint(MainActivity.x, Integer.parseInt(valores[i])), false, 100);

            }

            else if (strValores[i].contains("tempPlaca")){
                    placaAAdd.getSerieTPlaca().appendData(new DataPoint(MainActivity.x, Integer.parseInt(valores[i])), false, 100);

            }

            else if (strValores[i].contains("temp")){
                    placaAAdd.getSerieTemp().appendData(new DataPoint(MainActivity.x, Integer.parseInt(valores[i])), false, 100);

            }

            else if (strValores[i].contains("chuva")){
                    placaAAdd.getSerieChuva().appendData(new DataPoint(MainActivity.x, Integer.parseInt(valores[i])), false, 100);

            }

            else if (strValores[i].contains("tensao")){
                    placaAAdd.getSerieTensao().appendData(new DataPoint(MainActivity.x, Integer.parseInt(valores[i])), false, 100);

            }
            else if (strValores[i].contains("pressao")) {
                    placaAAdd.getSeriePressao().appendData(new DataPoint(MainActivity.x, Integer.parseInt(valores[i])), false, 100);

            }

            else if (strValores[i].contains("umidade")) {
                    placaAAdd.getSerieUmidade().appendData(new DataPoint(MainActivity.x, Integer.parseInt(valores[i])), false, 100);

            }

            else if (strValores[i].contains("corrente")){
                    placaAAdd.getSerieCorrente().appendData(new DataPoint(MainActivity.x, Integer.parseInt(valores[i])), false, 100);

            }
        }
    }

    public void adicionaDataPointsTeste(String[] strValores, String[] valores){

        int count, total, totalPlacas = placas.size(), idPlaca;
        String dado;
        //Checa se existe somente uma linha/placa
        if(totalPlacas == 1){
            /*
                No caso de apenas uma linha/placa, irá retirar a média de informações que possuem
                mais de uma aparição e modificar as outras aparições de forma que não se repita o processo
                Por fim, adiciona os pontos à série da linha/placa
             */
            for (int i = 0; i<strValores.length; i++) {
                if(strValores[i].equals("adicionado")) continue;
                count = 1;
                total = Integer.parseInt(valores[i]);
                if(strValores[i].matches(".*\\d")) { //if (strValor sem tudo menos números != "")
                    for (int j = i+1; j<strValores.length; j++) {
                        if(strValores[i].replaceAll("[0-9]*", "").matches(strValores[j].replaceAll("[0-9]*", ""))){
                            total += Integer.parseInt(valores[j]);
                            count++;
                            strValores[j] = "adicionado";
                        }
                    }
                    //valores[i] = "" + total/count;
                    adicionaPoint(strValores[i], total/count, 0);
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
            for (int i = 0; i<strValores.length; i++) {
                String teste = valores[i].replaceAll("\\D*", "");
                if(strValores[i].equals("adicionado") || valores[i].replaceAll("\\D*", "").equals("")) continue;
                count = 1;
                total = Integer.parseInt(valores[i]); // Valor inicial = primeira aparição de uma informação
                //Pré define a placa como "Média"
                idPlaca = 0;
                if(strValores[i].matches(".*\\d")) {
                    /*
                        Caso a informação seja de uma linha específica, troca o id pelo da linha
                        Então passa por toda a string adicionando as informações e juntando ao "total" da média
                        Por fim, adiciona à linha "Média"
                     */
                    dado = strValores[i].replaceAll("[0-9]*", "");

                    idPlaca = Integer.parseInt("0" + strValores[i].split("\\D+")[1]); // Retira o número da informação, correspondente ao id da linha/placa
                    adicionaPoint(dado, total, idPlaca);

                    for (int j = i+1; j<strValores.length; j++) {
                        if(dado.matches(strValores[j].replaceAll("[0-9]*", ""))){
                            adicionaPoint(dado, Integer.parseInt(valores[j]), Integer.parseInt("0" + strValores[j].split("\\D+")[1]));
                            total += Integer.parseInt(valores[j]);
                            count++;
                            strValores[j] = "adicionado";
                        }
                    }
                    adicionaPoint(dado, total/count, 0);
                } else {
                    adicionaPoint(strValores[i], total, idPlaca);
                }
            }
        }
    }

    public void adicionaPoint(String serie, int valor, int placaId){
        if (serie.contains("luminosidade")){
            placas.get(placaId).getSerieLumi().appendData(new DataPoint(MainActivity.x, valor), false, 100);

        }

        else if (serie.contains("tempPlaca")){
            placas.get(placaId).getSerieTPlaca().appendData(new DataPoint(MainActivity.x, valor), false, 100);

        }

        else if (serie.contains("temp")){
            placas.get(placaId).getSerieTemp().appendData(new DataPoint(MainActivity.x, valor), false, 100);

        }

        else if (serie.contains("chuva")){
            placas.get(placaId).getSerieChuva().appendData(new DataPoint(MainActivity.x, valor), false, 100);

        }

        else if (serie.contains("tensao")){
            placas.get(placaId).getSerieTensao().appendData(new DataPoint(MainActivity.x, valor), false, 100);

        }
        else if (serie.contains("pressao")) {
            placas.get(placaId).getSeriePressao().appendData(new DataPoint(MainActivity.x, valor), false, 100);

        }

        else if (serie.contains("umidade")) {
            placas.get(placaId).getSerieUmidade().appendData(new DataPoint(MainActivity.x, valor), false, 100);

        }

        else if (serie.contains("corrente")){
            placas.get(placaId).getSerieCorrente().appendData(new DataPoint(MainActivity.x, valor), false, 100);

        }
    }

    @Override
    public String toString() {
        return nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<placaMonitoramento> getPlacas() {
        return placas;
    }

    public void setPlacas(List<placaMonitoramento> placas) {
        this.placas = placas;
    }
}
