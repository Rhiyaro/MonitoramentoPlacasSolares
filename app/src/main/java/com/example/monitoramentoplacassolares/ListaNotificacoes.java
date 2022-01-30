package com.example.monitoramentoplacassolares;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

public class ListaNotificacoes extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private NavigationDrawer navDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_lista_notificacoes);

        navDrawer = new NavigationDrawer(this);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        boolean mesmo = false;

        if (id == R.id.nav_notificacoes) {
            mesmo = true;
        }

        return navDrawer.navigate(id, mesmo);
    }
}