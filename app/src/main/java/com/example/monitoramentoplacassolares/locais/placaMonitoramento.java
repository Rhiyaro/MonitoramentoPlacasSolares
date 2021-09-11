package com.example.monitoramentoplacassolares.locais;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class placaMonitoramento {
    String nome;
    int id;

    private String strValores, valores;

    private LineGraphSeries<DataPoint> serieLumi = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> serieTPlaca = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> serieTensao = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> serieCorrente = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> seriePressao = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> serieTemp = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> serieUmidade = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> serieChuva = new LineGraphSeries<DataPoint>();

    public placaMonitoramento(String nome, int id){
        this.nome = nome;
        this.id = id;
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

    public void setSerieLumi(LineGraphSeries<DataPoint> serieLumi) {
        this.serieLumi = serieLumi;
    }

    public LineGraphSeries<DataPoint> getSerieTPlaca() {
        return serieTPlaca;
    }

    public void setSerieTPlaca(LineGraphSeries<DataPoint> serieTPlaca) {
        this.serieTPlaca = serieTPlaca;
    }

    public LineGraphSeries<DataPoint> getSerieTensao() {
        return serieTensao;
    }

    public void setSerieTensao(LineGraphSeries<DataPoint> serieTensao) {
        this.serieTensao = serieTensao;
    }

    public LineGraphSeries<DataPoint> getSerieCorrente() {
        return serieCorrente;
    }

    public void setSerieCorrente(LineGraphSeries<DataPoint> serieCorrente) {
        this.serieCorrente = serieCorrente;
    }

    public LineGraphSeries<DataPoint> getSeriePressao() {
        return seriePressao;
    }

    public void setSeriePressao(LineGraphSeries<DataPoint> seriePressao) {
        this.seriePressao = seriePressao;
    }

    public LineGraphSeries<DataPoint> getSerieTemp() {
        return serieTemp;
    }

    public void setSerieTemp(LineGraphSeries<DataPoint> serieTemp) {
        this.serieTemp = serieTemp;
    }

    public LineGraphSeries<DataPoint> getSerieUmidade() {
        return serieUmidade;
    }

    public void setSerieUmidade(LineGraphSeries<DataPoint> serieUmidade) {
        this.serieUmidade = serieUmidade;
    }

    public LineGraphSeries<DataPoint> getSerieChuva() {
        return serieChuva;
    }

    public void setSerieChuva(LineGraphSeries<DataPoint> serieChuva) {
        this.serieChuva = serieChuva;
    }
}
