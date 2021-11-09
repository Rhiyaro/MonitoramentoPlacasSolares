package com.example.monitoramentoplacassolares;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.util.LogPrinter;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.monitoramentoplacassolares.conexao.Cliente;
import com.example.monitoramentoplacassolares.conexao.IAsyncHandler;
import com.example.monitoramentoplacassolares.conexao.RunnableCliente;
import com.example.monitoramentoplacassolares.conexao.TarefaMensagem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Future;

public class LoginAct extends AppCompatActivity implements IAsyncHandler {
    private static final String TAG = "LoginAct";

    private Cliente con;
    private Future clienteFuture;
    private EditText edtTxtLogin, edtTxtSenha;

    private boolean tentandoLogar = false;

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
        /*
        TODO: Refatorar todo o código para funcionar com o CallableClient e Futures -> Checar
                se ainda será interessante
         */
        if(edtTxtLogin.getText().length() < 3 || edtTxtSenha.getText().length() < 3){
            tentandoLogar = false;
            Toast.makeText(LoginAct.this, "Login e/ou senha inseridos inválidos!", Toast.LENGTH_SHORT).show();
        } else {
            if(!tentandoLogar){
                Log.i(TAG, "logar: logando");
                tentandoLogar = true;

                JSONObject pacoteLogin = new JSONObject();

                try {
                    pacoteLogin.put("acao", "logar");
                    pacoteLogin.put("login", edtTxtLogin.getText());
                    pacoteLogin.put("senha", edtTxtSenha.getText());

                    MainActivity.Cliente = new RunnableCliente();
                    //MainActivity.Cliente.iniciaCliente();
                    //MainActivity.executorServiceCached.submit(MainActivity.Cliente.iniciar);

                    TarefaMensagem tarefaLogin = new TarefaMensagem( this, pacoteLogin);

                    MainActivity.Cliente.novaTarefa(tarefaLogin);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


//        if(con != null && con.cancel(false)) {
        /*if(clienteFuture != null && clienteFuture.cancel(false)){
//            con = new Cliente(LoginAct.this);
//            con.execute("logar;login," + edtTxtLogin.getText() + ";senha," + edtTxtSenha.getText());

            clienteFuture = MainActivity.executorService.submit(
                    new RunnableCliente(LoginAct.this,
                            "logar;login," + edtTxtLogin.getText() + ";senha," + edtTxtSenha.getText()));

//            clienteFuture = MainActivity.executorService.submit(
//                    new CallableCliente(LoginAct.this,
//                            "logar;login," + edtTxtLogin.getText() + ";senha," + edtTxtSenha.getText()));

        }else{
//            con = new Cliente(LoginAct.this);
//            con.execute("logar;login," + edtTxtLogin.getText() + ";senha," + edtTxtSenha.getText());

            clienteFuture = MainActivity.executorService.submit(
                    new RunnableCliente(LoginAct.this,
                            "logar;login," + edtTxtLogin.getText() + ";senha," + edtTxtSenha.getText()));
//            clienteFuture = MainActivity.executorService.submit(
//                    new CallableCliente(LoginAct.this,
//                            "logar;login," + edtTxtLogin.getText() + ";senha," + edtTxtSenha.getText()));
        }*/

//        String resultado;
//        try {
////            if(clienteFuture.isDone()){
//                posResultado((String)clienteFuture.get(5000, TimeUnit.MILLISECONDS));
////            }
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (TimeoutException e) {
//            e.printStackTrace();
//        }

//        MainActivity.getAppToken();


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
