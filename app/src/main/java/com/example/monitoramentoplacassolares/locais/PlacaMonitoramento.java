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

    private final List<LineGraphSeries<DataPoint>> lineGraphSeries = new ArrayList<>();
    private final List<String> titulosSeries = new ArrayList<>();

    private List<SeriePlaca> series = new ArrayList<>();

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

        for (int i = 0; i < MainActivity.titulosDados.length; i++) {
            SeriePlaca serie = new SeriePlaca(MainActivity.titulosDados[i], MainActivity.nomesDados[i]);
            series.add(serie);
        }

        serieLumi.setTitle("luminosidade");
        lineGraphSeries.add(serieLumi);
        titulosSeries.add("Luminosidade");

        serieTPlaca.setTitle("tempPlaca");
        lineGraphSeries.add(serieTPlaca);
        titulosSeries.add("Temp. Placa");

        serieTensao.setTitle("tensao");
        lineGraphSeries.add(serieTensao);
        titulosSeries.add("Tensão");

        serieCorrente.setTitle("corrente");
        lineGraphSeries.add(serieCorrente);
        titulosSeries.add("Corrente");

        seriePressao.setTitle("pressao");
        lineGraphSeries.add(seriePressao);
        titulosSeries.add("Pressão");

        serieTemp.setTitle("temp");
        lineGraphSeries.add(serieTemp);
        titulosSeries.add("Temperatura");

        serieUmidade.setTitle("umidade");
        lineGraphSeries.add(serieUmidade);
        titulosSeries.add("Umidade");

        serieChuva.setTitle("chuva");
        lineGraphSeries.add(serieChuva);
        titulosSeries.add("Chuva");
    }

    public void adicionaPonto(String serie, double valor){
//        Iterator<LineGraphSeries<DataPoint>> it = lineGraphSeries.iterator();
//        String titulo;
//        LineGraphSeries<DataPoint> alvo;
//
//        while(it.hasNext()){
//            alvo = it.next();
//            titulo = alvo.getTitle();
//            if(serie.equals(titulo)){
//                alvo.appendData(new DataPoint(MainActivity.grafX, valor), false, 100);
//            }
//        }

        Iterator<SeriePlaca> iterator = series.iterator();
        SeriePlaca serieAlvo;

        while(iterator.hasNext()){
            serieAlvo = iterator.next();
            if(serie.equals(serieAlvo.getTitulo())){
                serieAlvo.getSerie().appendData(new DataPoint(MainActivity.grafX, valor), false, 100);
            }
        }
    }

    public LineGraphSeries<DataPoint> getLineGraphSeriesByTitle(String title){
        Iterator<LineGraphSeries<DataPoint>> it = this.lineGraphSeries.iterator();
        LineGraphSeries<DataPoint> alvo;

        while(it.hasNext()){
            alvo = it.next();
            if(alvo.getTitle().equals(title)){
                return alvo;
            }
        }

        return null;
    }

    public SeriePlaca getSerieByTitle (String title) {
        Iterator<SeriePlaca> iterator = series.iterator();
        SeriePlaca alvo;

        while (iterator.hasNext()){
            alvo = iterator.next();
            if (alvo.getTitulo().equals(title)){
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

    public List<LineGraphSeries<DataPoint>> getLineGraphSeries() {
        return lineGraphSeries;
    }

    public List<String> getTitulosSeries() {
        return titulosSeries;
    }

    public List<SeriePlaca> getSeries() {
        return series;
    }

    public class SeriePlaca {
        private String titulo;
        private String nome;

        private LineGraphSeries<DataPoint> serie;
        //TODO: Modificar placa para usar classe SeriePlaca
        public SeriePlaca(String titulo, String nome) {
            this.titulo = titulo;
            this.nome = nome;

            serie = new LineGraphSeries<>();
            serie.setTitle(titulo);
        }

        public String getTitulo() {
            return titulo;
        }

        public String getNome() {
            return nome;
        }

        public LineGraphSeries<DataPoint> getSerie() {
            return serie;
        }
    }
}
