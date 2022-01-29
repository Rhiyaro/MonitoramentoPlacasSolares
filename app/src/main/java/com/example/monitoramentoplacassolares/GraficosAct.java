package com.example.monitoramentoplacassolares;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.monitoramentoplacassolares.conexao.Cliente;
import com.example.monitoramentoplacassolares.conexao.IAsyncHandler;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class GraficosAct extends AppCompatActivity implements IAsyncHandler, NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {
    public static final String TAG = "GraficosAct";

    private GraphView graf;
    private LineGraphSeries<DataPoint> series;
    private Viewport viewport;
    boolean fim;
    private Spinner spMes, spDia, spDado;
    private Toolbar tb;
    private String Dados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate: \ngrafs");

        setContentView(R.layout.act_graficos);

        tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, tb, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        spDia = findViewById(R.id.spDia);
        ArrayAdapter<CharSequence> adapterspDia = ArrayAdapter.createFromResource(this,
                R.array.strSpDia, android.R.layout.simple_spinner_item);
        adapterspDia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDia.setAdapter(adapterspDia);
        spDia.setOnItemSelectedListener(this);

        spMes = findViewById(R.id.spMes);
        ArrayAdapter<CharSequence> adapterspMes = ArrayAdapter.createFromResource(this,
                R.array.strSpMes, android.R.layout.simple_spinner_item);
        adapterspMes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMes.setAdapter(adapterspMes);
        spMes.setOnItemSelectedListener(this);

        spDado = findViewById(R.id.spDado);
        ArrayAdapter<CharSequence> adapterspDado = ArrayAdapter.createFromResource(this,
                R.array.strSpDados, android.R.layout.simple_spinner_item);
        adapterspMes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDado.setAdapter(adapterspDado);
        spDado.setOnItemSelectedListener(this);

        fim = false;
        graf = findViewById(R.id.graf);

        viewport = graf.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setScrollable(true);
        viewport.setScalable(true);
        viewport.setScalableY(true);
        viewport.setScrollableY(true);

    }

    public void request(View v){
        String data;
        data  =  spDia.getSelectedItem() + "-";
        data +=  spMes.getSelectedItem();

        Cliente task = new Cliente(this);
        task.execute("Dados DATA " + data);
    }

    private void attGrafico() {
        if (Dados != null) {
            int x = 0, ymax = 0, ymin = 0;
            graf.removeAllSeries();
            series = null;
            series = new LineGraphSeries<DataPoint>();
            String[] vetDados  = Dados.split(";");
            String[][] dados = new String[vetDados.length][vetDados[0].split(",").length];
            if(vetDados.length > 1) {
                int[] y = new int[vetDados.length-1];
                for (int i = 1; i < vetDados.length; i++) {
                    String[] aux = vetDados[i].split(",");
                    for (int j = 0; j < vetDados[0].split(",").length; j++) {
                        dados[i][j] = aux[j];
                        if (spDado.getSelectedItem().equals(getResources().getStringArray(R.array.strSpDados)[0]))
                            if (j == 1)
                                y[i-1] = Integer.parseInt(aux[j]);

                        if (spDado.getSelectedItem().equals(getResources().getStringArray(R.array.strSpDados)[1]))
                            if (j == 2)
                                y[i-1] = Integer.parseInt(aux[j]);

                        if (spDado.getSelectedItem().equals(getResources().getStringArray(R.array.strSpDados)[2]))
                            if (j == 4)
                                y[i-1] = Integer.parseInt(aux[j]);

                        if (spDado.getSelectedItem().equals(getResources().getStringArray(R.array.strSpDados)[3]))
                            if (j == 5)
                                y[i-1] = Integer.parseInt(aux[j]);

                        if (spDado.getSelectedItem().equals(getResources().getStringArray(R.array.strSpDados)[4]))
                            if (j == 6)
                                y[i-1] = Integer.parseInt(aux[j]);

                        if (spDado.getSelectedItem().equals(getResources().getStringArray(R.array.strSpDados)[5]))
                            if (j == 7)
                                y[i-1] = Integer.parseInt(aux[j]);

                    }
                    if(y[i-1]>ymax)
                        ymax = y[i-1];
                    if(y[i-1]<ymin)
                        ymin = y[i-1];
                    series.appendData(new DataPoint(x++, y[i-1]), true, 1450);

                }
                graf.addSeries(series);
                viewport.setMaxY(ymax);
                viewport.setMaxX(x);
                viewport.scrollToEnd();
            }
        }
    }

    public void goAct(View v, Class act){
        fim = true;
        Intent intAct = new Intent(this, act);
        startActivity(intAct);
        this.finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_home) {
            goAct(findViewById(id), MainActivity.class);

        } else if (id == R.id.nav_bd) {
            goAct(findViewById(id), DadosAct.class);
        } else if (id == R.id.nav_salvar) {
            salvarGraf();
        } else if (id == R.id.nav_notificacoes){

        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void salvarGraf(){

        Bitmap bitmap = graf.takeSnapshot();
        String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions((Activity) GraficosAct.this, PERMISSIONS, 112);

        String fileName = spDia.getSelectedItem() + (String)spMes.getSelectedItem() + ".jpg";

        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "mps" + File.separator + "graficos");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

        File file = new File(folder + File.separator + fileName);

        FileOutputStream fileOutputStream = null;
        try {
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bytes.toByteArray());

            Toast.makeText(GraficosAct.this,
                    file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fileOutputStream != null){
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void postResult(String result) {
        Dados = result;
        attGrafico();
    }

    @Override
    public void postResult(JSONObject result) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == spDado.getId())
            attGrafico();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}