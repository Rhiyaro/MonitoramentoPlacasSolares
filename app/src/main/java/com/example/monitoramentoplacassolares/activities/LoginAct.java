package com.example.monitoramentoplacassolares.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.monitoramentoplacassolares.R;
import com.example.monitoramentoplacassolares.conexao.Cliente;
import com.example.monitoramentoplacassolares.conexao.IAsyncHandler;
import com.example.monitoramentoplacassolares.conexao.RunnableCliente;
import com.example.monitoramentoplacassolares.conexao.TarefaMensagem;
import com.example.monitoramentoplacassolares.excecoes.HttpLoginException;
import com.example.monitoramentoplacassolares.httpcomm.MpsHttpClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.Request;

public class LoginAct extends AppCompatActivity implements IAsyncHandler {
    private static final String TAG = "LoginAct";

    public static final String LOGIN_OK = "sucesso";
    public static final String SENHA_INV = "senha-invalida";
    public static final String LOGIN_INEX = "login-inexistente";
    public static final String ERRO = "erro";

    private Cliente con;
    private Future clienteFuture;
    private EditText edtTxtLogin, edtTxtSenha;

    private boolean tentandoLogar = false;

    public LoginAct() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_login);

        edtTxtLogin = findViewById(R.id.edtTxtLogin);
        edtTxtSenha = findViewById(R.id.edtTxtSenha);


    }

    public void goMain() {
        final Context thisContext = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intAct = new Intent(thisContext, MainActivity.class);
                startActivity(intAct);
            }
        });
        this.finish();
    }

    public void logar(View view) {
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

    private boolean checaLoginValido() {
        if (edtTxtLogin.getText().length() < 3 || edtTxtSenha.getText().length() < 3) {
            tentandoLogar = false;
            Toast.makeText(LoginAct.this, "Login e/ou senha inseridos inválidos!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String logarHttp() throws HttpLoginException {
        //TODO: Implementar leitura de propriedades
        MpsHttpClient.criaInstancia(new MpsHttpClient("192.168.25.9", 8080, this));

        String login = edtTxtLogin.getText().toString();
        String senha = edtTxtSenha.getText().toString();

        return MpsHttpClient.instacia().logarCliente(login, senha);
    }

    private void logarSocket() {
        if (!tentandoLogar) {
            Log.i(TAG, "logar: logando");
            tentandoLogar = true;

            JSONObject pacoteLogin = new JSONObject();

            try {
                pacoteLogin.put("acao", "logar");
                pacoteLogin.put("login", edtTxtLogin.getText());
                pacoteLogin.put("senha", edtTxtSenha.getText());

                MainActivity.runnableCliente = new RunnableCliente();
                //MainActivity.Cliente.iniciaCliente();
                //MainActivity.executorServiceCached.submit(MainActivity.Cliente.iniciar);

                TarefaMensagem tarefaLogin = new TarefaMensagem(this, pacoteLogin);

                MainActivity.runnableCliente.novaTarefa(tarefaLogin);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void checaResultadoLogin(String resultado) throws HttpLoginException {
        switch (resultado) {
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

    public void cadastrar(View view) {
        Intent intAct = new Intent(this, CadastroAct.class);
        startActivity(intAct);

    }

    @Override
    public void postResult(String result) {
        if (result.toLowerCase().contains("sucesso")) {
            FirebaseMessaging.getInstance().subscribeToTopic("avisos")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = "sucesso inscrição tópico";//getString(R.string.msg_subscribed);
                            if (!task.isSuccessful()) {
                                msg = "falha inscrição tópico";//getString(R.string.msg_subscribe_failed);
                            }
                            Log.i(TAG, msg);
                            //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });

            goMain();
        } else if (result.contains("sem conexao")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LoginAct.this, "Sem conexao com o servidor!", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (result.toLowerCase().contains("login nao encontrado")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LoginAct.this, "Login e/ou senha inválidos!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void postResult(JSONObject result) {
        final Context thisContext = this;
        try {
            switch (result.getString("resultado")) {
                case "sucesso":
                    /*
                    Inscrição no tópico do Firebase para recebimento das mensagens para notificação
                    TODO: Mover para outra parte, executando apenas uma vez por aparelho
                     */
                    /*FirebaseMessaging.getInstance().subscribeToTopic("avisos")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    String msg = "sucesso inscrição tópico";//getString(R.string.msg_subscribed);
                                    if (!task.isSuccessful()) {
                                        msg = "falha inscrição tópico";//getString(R.string.msg_subscribe_failed);
                                    }
                                    Log.i(TAG, msg);
                                    //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                }
                            });
                    */
                    goMain();
                    break;
                case "sem conexao":
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(thisContext, "Sem conexão com o servidor!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    tentandoLogar = false;
                    break;
                case "login inexistente":
                case "senha invalida":
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(thisContext, "Login e/ou senha inválidos!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    tentandoLogar = false;
                    break;
                default:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(thisContext, "Ocorreu um erro", Toast.LENGTH_SHORT).show();
                        }
                    });
                    tentandoLogar = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
