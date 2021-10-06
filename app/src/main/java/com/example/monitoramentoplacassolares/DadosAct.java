package com.example.monitoramentoplacassolares;

import android.content.Intent;

import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.monitoramentoplacassolares.Conexao.Cliente;
import com.example.monitoramentoplacassolares.adapters.DadosAdapter;
import com.example.monitoramentoplacassolares.conexao.Cliente;
import com.example.monitoramentoplacassolares.conexao.IAsyncHandler;
import com.opencsv.CSVWriter;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DadosAct extends AppCompatActivity implements AdapterView.OnItemSelectedListener, IAsyncHandler, NavigationView.OnNavigationItemSelectedListener {

    private Spinner spMes, spDia;
    private Toolbar tb;
    private RecyclerView rcDados;
    private String  Dados = null;
    private boolean sair;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dados);

        sair = false;

        tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, tb, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        spDia = (Spinner) findViewById(R.id.spDia);
        ArrayAdapter<CharSequence> adapterspDia = ArrayAdapter.createFromResource(this,
                R.array.strSpDia, android.R.layout.simple_spinner_item);
        adapterspDia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDia.setAdapter(adapterspDia);
        spDia.setOnItemSelectedListener(this);

        spMes = (Spinner) findViewById(R.id.spMes);
        ArrayAdapter<CharSequence> adapterspMes = ArrayAdapter.createFromResource(this,
                R.array.strSpMes, android.R.layout.simple_spinner_item);
        adapterspMes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMes.setAdapter(adapterspMes);
        spMes.setOnItemSelectedListener(this);

        rcDados = (RecyclerView) findViewById(R.id.rcDados);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcDados.setLayoutManager(layoutManager);

        DadosAdapter mAdapter = new DadosAdapter(Dados);
        rcDados.setAdapter(mAdapter);




    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //int Dia = spDia.getSelectedItemPosition();
        String Dia = (String) spDia.getSelectedItem();
        String Mes = (String) spMes.getSelectedItem();

        TextView txtData = (TextView) findViewById(R.id.textView);
        txtData.setText("d: " + Dia + "  m: " + Mes + "  a: 2019");

        Toast.makeText(this, parent.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
    }

    private void salvarDados(){

        String pathName = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator + "mps" + File.separator + "dados";

        File f = new File(pathName);
        CSVWriter writer;

        if (!f.exists()) {
            f.mkdirs();
        }

        // File exist
        writer = null;
        if(f.exists()&&!f.isDirectory()) {
            FileWriter mFileWriter = null;
            try {
                mFileWriter = new FileWriter(pathName + File.separator + (String) spDia.getSelectedItem() + (String) spMes.getSelectedItem() + ".csv");
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer = new CSVWriter(mFileWriter);
        }else {
            try {
                writer = new CSVWriter(new FileWriter(pathName + File.separator + (String) spDia.getSelectedItem() + (String) spMes.getSelectedItem() + ".csv"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String[] data = Dados.split(";");

        for (int i = 0; i < data.length; i++) {
            writer.writeNext(data[i].split(";"), false);
        }


        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(DadosAct.this, pathName + '/' +(String) spDia.getSelectedItem() + (String) spMes.getSelectedItem() + ".csv", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(sair) {
                super.finish();
            }else
                Toast.makeText(this, "Pressione novamente para sair.", Toast.LENGTH_SHORT).show();
            sair = true;
        }
    }

    @Override
    public void postResult(String result) {
        Dados = result;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DadosAdapter mAdapter = new DadosAdapter(Dados);
                rcDados.setAdapter(mAdapter);
            }
        });

    }

    @Override
    public void postResult(JSONObject result){

    }

    public void request(View v){
        String data;
        data  =  spDia.getSelectedItem() + "-";
        data +=  spMes.getSelectedItem();

        Cliente task = new Cliente(DadosAct.this);
        task.execute("DADOS DATA " + data);
    }

    public void goAct(View v, Class act){
        Intent intAct = new Intent(this, act);
        startActivity(intAct);
        this.finish();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_home) {
            goAct(findViewById(id), MainActivity.class);

        } else if (id == R.id.nav_graficos) {
            goAct(findViewById(id), GraficosAct.class);

        }else if (id == R.id.nav_salvar) {
            salvarDados();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}