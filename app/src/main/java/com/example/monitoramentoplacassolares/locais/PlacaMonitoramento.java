package com.example.monitoramentoplacassolares.locais;

import androidx.annotation.NonNull;

import com.example.monitoramentoplacassolares.activities.MainActivity;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlacaMonitoramento {
    private final String nome;
    private final String codigo;
    int id;

    private final List<LineGraphSeries<DataPoint>> series = new ArrayList<>();
    private final List<String> titulosSeries = new ArrayList<>();

    private final LineGraphSeries<DataPoint> serieLumi = new LineGraphSeries<>();
    private final LineGraphSeries<DataPoint> serieTPlaca = new LineGraphSeries<>();
    private final LineGraphSeries<DataPoint> serieTensao = new LineGraphSeries<>();
    private final LineGraphSeries<DataPoint> serieCorrente = new LineGraphSeries<>();
    private final LineGraphSeries<DataPoint> seriePressao = new LineGraphSeries<>();
    private final LineGraphSeries<DataPoint> serieTemp = new LineGraphSeries<>();
    private final LineGraphSeries<DataPoint> serieUmidade = new LineGraphSeries<>();
    private final LineGraphSeries<DataPoint> serieChuva = new LineGraphSeries<>();

    public PlacaMonitoramento(String nome, String codigo, int id){
        this.nome = nome;
        this.codigo = codigo;
        this.id = id;
        inicializaTitulosSeries();
    }

    private void inicializaTitulosSeries(){
        serieLumi.setTitle("luminosidade");
        series.add(serieLumi);
        titulosSeries.add("Luminosidade");

        serieTPlaca.setTitle("tempPlaca");
        series.add(serieTPlaca);
        titulosSeries.add("Temp. Placa");

        serieTensao.setTitle("tensao");
        series.add(serieTensao);
        titulosSeries.add("Tensão");

        serieCorrente.setTitle("corrente");
        series.add(serieCorrente);
        titulosSeries.add("Corrente");

        seriePressao.setTitle("pressao");
        series.add(seriePressao);
        titulosSeries.add("Pressão");

        serieTemp.setTitle("temp");
        series.add(serieTemp);
        titulosSeries.add("Temperatura");

        serieUmidade.setTitle("umidade");
        series.add(serieUmidade);
        titulosSeries.add("Umidade");

        serieChuva.setTitle("chuva");
        series.add(serieChuva);
        titulosSeries.add("Chuva");
    }

    public void adicionaPonto(String serie, double valor){
        Iterator<LineGraphSeries<DataPoint>> it = this.series.iterator();
        String titulo;
        LineGraphSeries<DataPoint> alvo;

        while(it.hasNext()){
            alvo = it.next();
            titulo = alvo.getTitle();
            if(serie.equals(titulo)){
                alvo.appendData(new DataPoint(MainActivity.grafX, valor), false, 100);
            }
        }
    }

    public LineGraphSeries<DataPoint> getSeriesByTitle(String title){
        Iterator<LineGraphSeries<DataPoint>> it = this.series.iterator();
        LineGraphSeries<DataPoint> alvo;

        while(it.hasNext()){
            alvo = it.next();
            if(alvo.getTitle().equals(title)){
                return alvo;
            }
        }

        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return nome;
    }

    public String getNome() {
        return nome;
    }

    public int getId() {
        return id;
    }

    public LineGraphSeries<DataPoint> getSerieLumi() {
        return serieLumi;
    }

    public LineGraphSeries<DataPoint> getSerieTPlaca() {
        return serieTPlaca;
    }

    public LineGraphSeries<DataPoint> getSerieTensao() {
        return serieTensao;
    }

    public LineGraphSeries<DataPoint> getSerieCorrente() {
        return serieCorrente;
    }

    public LineGraphSeries<DataPoint> getSeriePressao() {
        return seriePressao;
    }

    public LineGraphSeries<DataPoint> getSerieTemp() {
        return serieTemp;
    }

    public LineGraphSeries<DataPoint> getSerieUmidade() {
        return serieUmidade;
    }

    public LineGraphSeries<DataPoint> getSerieChuva() {
        return serieChuva;
    }

    public String getCodigo() {
        return codigo;
    }

    public List<LineGraphSeries<DataPoint>> getSeries() {
        return series;
    }

    public List<String> getTitulosSeries() {
        return titulosSeries;
    }
}
