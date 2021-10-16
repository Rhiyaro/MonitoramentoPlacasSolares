package com.example.monitoramentoplacassolares.locais;

import com.example.monitoramentoplacassolares.MainActivity;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class placaMonitoramento {
    private String nome;
    private String codigo;
    int id;

    private String strValores, valores;


    private List<LineGraphSeries<DataPoint>> series = new ArrayList<>();

    private LineGraphSeries<DataPoint> serieLumi = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> serieTPlaca = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> serieTensao = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> serieCorrente = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> seriePressao = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> serieTemp = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> serieUmidade = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> serieChuva = new LineGraphSeries<>();

    public placaMonitoramento(String nome, String codigo,int id){
        this.nome = nome;
        this.codigo = codigo;
        this.id = id;
        inicializaTitulosSeries();
    }

    private void inicializaTitulosSeries(){
        serieLumi.setTitle("luminosidade");
        series.add(serieLumi);

        serieTPlaca.setTitle("tempPlaca");
        series.add(serieTPlaca);

        serieTensao.setTitle("tensao");
        series.add(serieTensao);

        serieCorrente.setTitle("corrente");
        series.add(serieCorrente);

        seriePressao.setTitle("pressao");
        series.add(seriePressao);

        serieTemp.setTitle("temp");
        series.add(serieTemp);

        serieUmidade.setTitle("umidade");
        series.add(serieUmidade);

        serieChuva.setTitle("chuva");
        series.add(serieChuva);
    }

    public void adicionaPonto(String serie, double valor){
        Iterator<LineGraphSeries<DataPoint>> it = this.series.iterator();
        String titulo;
        LineGraphSeries<DataPoint> alvo;

        while(it.hasNext()){
            alvo = it.next();
            titulo = alvo.getTitle();
            if(serie.equals(titulo)){
                alvo.appendData(new DataPoint(MainActivity.x, valor), false, 100);
            }
        }
    }

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
}
