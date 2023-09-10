package com.example.monitoramentoplacassolares.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.monitoramentoplacassolares.R;
import com.example.monitoramentoplacassolares.excecoes.HttpLoginException;
import com.example.monitoramentoplacassolares.httpcomm.MpsHttpClient;
import com.example.monitoramentoplacassolares.httpcomm.MpsHttpServerInfo;

public class LoginAct extends AppCompatActivity {
    private static final String TAG = "LoginAct";

    public static final String LOGIN_OK = "sucesso";
    public static final String SENHA_INV = "senha-invalida";
    public static final String LOGIN_INEX = "login-inexistente";
    public static final String ERRO = "erro";

    private EditText edtTxtLogin, edtTxtSenha;

    public LoginAct() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_login);

        edtTxtLogin = findViewById(R.id.edtTxtLoginLogin);
        edtTxtSenha = findViewById(R.id.edtTxtSenhaLogin);

        carregaLoginSenha();
    }

    private void carregaLoginSenha() {
        SharedPreferences configs = PreferenceManager.getDefaultSharedPreferences(this);
        String login = configs.getString("login", "");
        String senha = configs.getString("senha", "");
        edtTxtLogin.setText(login);
        edtTxtSenha.setText(senha);
    }

    public void goMain() {
        final Context thisContext = this;
        runOnUiThread(() -> {
            Intent intAct = new Intent(thisContext, MainActivity.class);
            startActivity(intAct);
        });
        this.finish();
    }

    public void btLogarAction(View view) {
        salvarLogin();
        if (!checaLoginValido()) {
            return;
        }
        try {
            String resultado = logarHttp();
            checaResultadoLogin(resultado);
        } catch (HttpLoginException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void salvarLogin() {
        SharedPreferences configs = PreferenceManager.getDefaultSharedPreferences(this);
        String login = edtTxtLogin.getText().toString();
        String senha = edtTxtSenha.getText().toString();
        configs.edit().putString("login", login).apply();
        configs.edit().putString("senha", senha).apply();
    }

    private boolean checaLoginValido() {
        if (edtTxtLogin.getText().length() < 3 || edtTxtSenha.getText().length() < 3) {
            Toast.makeText(LoginAct.this,
                    "Login e/ou senha inseridos inválidos!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String logarHttp() throws HttpLoginException {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this );
        String ip = sharedPreferences.getString("ip", "192.168.0.8");
        int port = Integer.parseInt(sharedPreferences.getString("port", "8080"));
        MpsHttpClient.criaInstancia(new MpsHttpClient(ip, port, this));

        String login = edtTxtLogin.getText().toString();
        String senha = edtTxtSenha.getText().toString();

        return MpsHttpClient.instacia().logarCliente(login, senha);
    }

    private void checaResultadoLogin(String resultado) throws HttpLoginException {
        // FIXME: Added for compatibility
        String teste_resultado = resultado;
        if (teste_resultado.contains("token")) {
            teste_resultado = LOGIN_OK;
            // {"token":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsb2dpbjEiLCJpYXQiOjE2ODg5NTY5NDIsImV4cCI6MTY4OTA0MzM0Mn0.6AgLDPX2Kzy9sQ6IhCtGs_gIBHV8_-wxP2h3Mv3CwNU"}
            MpsHttpServerInfo.bearerToken = resultado.split(":")[1];
            MpsHttpServerInfo.bearerToken = MpsHttpServerInfo.bearerToken.split("\"")[1];
        }
        switch (teste_resultado) {
            case LOGIN_OK:
                goMain();
                break;
            case LOGIN_INEX:
                throw new HttpLoginException("Login inexistente");
            case SENHA_INV:
                throw new HttpLoginException("Senha inválida");
            default:
                throw new HttpLoginException("Erro de Login: Resposta Inesperada");
        }
    }

    public void irCadastrar(View view) {
        Intent intAct = new Intent(this, CadastroAct.class);
        startActivity(intAct);
    }

    public void irConfigs(View view) {
        Intent intAct = new Intent(this, ConfigAct.class);
        startActivity(intAct);
    }
}
