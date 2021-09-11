package com.example.monitoramentoplacassolares;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.monitoramentoplacassolares.conexao.CallableCliente;
import com.example.monitoramentoplacassolares.conexao.Cliente;
import com.example.monitoramentoplacassolares.conexao.IAsyncHandler;
import com.example.monitoramentoplacassolares.conexao.RunnableCliente;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LoginAct extends AppCompatActivity implements IAsyncHandler {
    private static final String TAG = "LoginAct";
    private Cliente con;
    private Future clienteFuture;
    private EditText edtTxtLogin, edtTxtSenha;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_login);

        edtTxtLogin = findViewById(R.id.edtTxtLogin);
        edtTxtSenha = findViewById(R.id.edtTxtSenha);




    }

    public void goMain(View view){
        Intent intAct = new Intent(this, MainActivity.class);
        startActivity(intAct);
    }

    public void logar(View view){
        //TODO: Refatorar todo o código para funcionar com o CallableClient e Futures

        RunnableCliente runnCliente = new RunnableCliente(LoginAct.this,
                "logar;login," + edtTxtLogin.getText() + ";senha," + edtTxtSenha.getText());

        clienteFuture = MainActivity.executorService.submit(runnCliente);

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

    public void cadastrar(View view){
        Intent intAct = new Intent(this, CadastroAct.class);
        startActivity(intAct);

    }

    @Override
    public void postResult(String result) {
        System.out.println("00  " + result);
        if(result.toLowerCase().contains("sucesso")){
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

            Intent intAct = new Intent(this, MainActivity.class);
            startActivity(intAct);
            this.finish();
        }else if (result.contains("sem conexao")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LoginAct.this, "Sem conexao com o servidor!", Toast.LENGTH_SHORT).show();
                }
            });
        }else if (result.toLowerCase().contains("login nao encontrado")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LoginAct.this, "Login e/ou senha inválidos!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void posResultado(String resultado){
        Log.i(TAG, "posResultado: " + resultado);
        if(resultado.toLowerCase().contains("sucesso")){
            FirebaseMessaging.getInstance().subscribeToTopic("avisos")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = "sucesso inscrição tópico";//getString(R.string.msg_subscribed);
                            if (!task.isSuccessful()) {
                                msg = "falha inscrição tópico";//getString(R.string.msg_subscribe_failed);
                            }
                            Log.i(TAG, "onComplete: " + msg);
                            //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });

            Intent intAct = new Intent(this, MainActivity.class);
            startActivity(intAct);
            this.finish();
        }else if (resultado.contains("sem conexao")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LoginAct.this, "Sem conexao com o servidor!", Toast.LENGTH_SHORT).show();
                }
            });
        }else if (resultado.toLowerCase().contains("login nao encontrado")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LoginAct.this, "Login e/ou senha inválidos!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
