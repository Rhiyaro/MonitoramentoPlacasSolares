package com.example.monitoramentoplacassolares.activities;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.monitoramentoplacassolares.R;
import com.example.monitoramentoplacassolares.conexao.Cliente;
import com.example.monitoramentoplacassolares.conexao.IAsyncHandler;
import com.example.monitoramentoplacassolares.conexao.RunnableCliente;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Normalizer;

public class CadastroAct extends AppCompatActivity implements IAsyncHandler {

    //TODO: Adaptar para HTTP
    private Cliente con;
    private EditText edtTxtLogin, edtTxtSenha, edtTxtSenha2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_cadastro);

        edtTxtLogin = findViewById(R.id.edtTxtLoginCadastro);
        edtTxtSenha = findViewById(R.id.edtTxtConfirmaSenhaCadastro);
        edtTxtSenha2 = findViewById(R.id.edtTxtSenhaCadastro);
    }

    public void btAvancar(View v) {

        String login = Normalizer.normalize(String.valueOf(edtTxtLogin.getText()), Normalizer.Form.NFD);
        String senha = Normalizer.normalize(String.valueOf(edtTxtSenha.getText()), Normalizer.Form.NFD);

        JSONObject pacoteCadastro = new JSONObject();

        try {

            if (!login.matches("[a-zA-Z0-9]{4,20}") || !senha.matches("[a-zA-Z0-9]{4,20}")) {

                Toast.makeText(CadastroAct.this, "Login e/ou senha inválidos", Toast.LENGTH_SHORT).show();

            } else if (!senha.equals(String.valueOf(edtTxtSenha2.getText()))) {

                Toast.makeText(CadastroAct.this, "Senhas não coicidem", Toast.LENGTH_SHORT).show();

            } else {
            /*
            con = new Cliente(CadastroAct.this);
            con.execute("irCadastrar;login," + login + ";senha," + senha);
             */
                pacoteCadastro.put("acao", "irCadastrar");
                pacoteCadastro.put("login", login);
                pacoteCadastro.put("senha", senha);

                RunnableCliente runnableCliente = new RunnableCliente(CadastroAct.this, pacoteCadastro);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void btRetornar(View v) {
        finish();
    }

    @Override
    public void postResult(String result) {
        if (result.contains("login ja existe")) {
            Toast.makeText(CadastroAct.this, "Login já em uso!", Toast.LENGTH_SHORT).show();
        } else if (result.contains("cadastrado com sucesso")) {
            Toast.makeText(CadastroAct.this, "Cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
            Intent intAct = new Intent(this, LoginAct.class);
            startActivity(intAct);
            this.finish();
        }
    }

    @Override
    public void postResult(JSONObject result) {
        try {
            switch (result.getString("resultado")) {
                case "login existente":
                    Toast.makeText(CadastroAct.this, "Login já registrado", Toast.LENGTH_SHORT).show();
                    break;
                case "sucesso":
                    Toast.makeText(CadastroAct.this, "Cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    Intent intAct = new Intent(this, LoginAct.class);
                    startActivity(intAct);
                    this.finish();
                    break;
                case "sem conexao":
                    Toast.makeText(CadastroAct.this, "Sem conexão com o servidor", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(CadastroAct.this, "Ocorreu um erro inesperado", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
