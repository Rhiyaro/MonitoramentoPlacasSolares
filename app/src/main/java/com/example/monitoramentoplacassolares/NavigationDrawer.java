package com.example.monitoramentoplacassolares;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.monitoramentoplacassolares.DadosAct;
import com.example.monitoramentoplacassolares.GraficosAct;
import com.example.monitoramentoplacassolares.ListaNotificacoes;
import com.example.monitoramentoplacassolares.R;

public class NavigationDrawer extends AppCompatActivity {

    private Activity caller;

    public NavigationDrawer(Activity caller) {
        this.caller = caller;
    }

    public void goAct(View v, Class act) {
        Intent intAct = new Intent(caller, act);
        caller.startActivity(intAct);
        caller.finish();
    }

    public boolean navigate(int id, boolean mesmo){

        if(!mesmo){
            MenuItem salvar = findViewById(R.id.nav_salvar);
            salvar.setEnabled(false);

            if (id == R.id.nav_home) {
                goAct(findViewById(id), MainActivity.class);

            } else if (id == R.id.nav_bd) {
                goAct(findViewById(id), DadosAct.class);

            } else if (id == R.id.nav_graficos) {
                goAct(findViewById(id), GraficosAct.class);

            } else if (id == R.id.nav_notificacoes){
                goAct(findViewById(id), ListaNotificacoes.class);
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void setCaller(Activity caller) {
        this.caller = caller;
    }
}
