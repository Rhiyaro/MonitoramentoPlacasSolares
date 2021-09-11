package com.example.monitoramentoplacassolares;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.monitoramentoplacassolares.conexao.Cliente;
import com.example.monitoramentoplacassolares.conexao.IAsyncHandler;

import java.text.Normalizer;

public class CadastroAct extends AppCompatActivity implements IAsyncHandler {

    private Cliente con;
    private EditText edtTxtLogin, edtTxtSenha, edtTxtSenha2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_cadastro);

        edtTxtLogin = findViewById(R.id.edtTxtLogin);
        edtTxtSenha = findViewById(R.id.edtTxtSenha);
        edtTxtSenha2 = findViewById(R.id.edtTxtSenha2);

    }

    public void btAvancar(View v) {

        String login = Normalizer.normalize(String.valueOf(edtTxtLogin.getText()), Normalizer.Form.NFD);
        String senha = Normalizer.normalize(String.valueOf(edtTxtSenha.getText()), Normalizer.Form.NFD);

        if( login.length() < 3 || login.contains("´") || login.contains("`") || login.contains("~")
        || login.contains("^") || login.contains("[") || login.contains("{") || login.contains("}")
        || login.contains("]") || login.contains("º") || login.contains(".") || login.contains(",")
        || login.contains(">") || login.contains("<") || login.contains(":") || login.contains(";")
        || login.contains("/") || login.contains("|") || login.contains("\\") || login.contains("*")
        || login.contains("!") || login.contains("@") || login.contains("#") || login.contains("$")
        || login.contains("\'") || login.contains("\"") || login.contains("+") || login.contains("-")
        || login.contains("_") || login.contains("(") || login.contains(")") || login.contains(" ")
        || login.contains("=") || login.contains("&") || !login.replaceAll("[^\\p{ASCII}]", "").equals(login)){

            Toast.makeText(CadastroAct.this, "Login inaceitável!", Toast.LENGTH_SHORT).show();

        }else if(senha.length() < 5){

            Toast.makeText(CadastroAct.this, "Senha muito curta!", Toast.LENGTH_SHORT).show();

        }else if(!senha.replaceAll("[^\\p{ASCII}]", "").equals(senha)){
            Toast.makeText(CadastroAct.this, "Senha: caractéres inválidos!", Toast.LENGTH_SHORT).show();
        }

        else if(!senha.equals(String.valueOf(edtTxtSenha2.getText()))) {

            Toast.makeText(CadastroAct.this, "Senhas não coicidem!", Toast.LENGTH_SHORT).show();

        }else{
            con = new Cliente(CadastroAct.this);
            con.execute("cadastrar;login," + login + ";senha," + senha);
        }

    }

    @Override
    public void postResult(String result) {
        if(result.contains("login ja existe")){
            Toast.makeText(CadastroAct.this, "Login já em uso!", Toast.LENGTH_SHORT).show();
        }else if(result.contains("cadastrado com sucesso")){
            Toast.makeText(CadastroAct.this, "Cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
            Intent intAct = new Intent(this, LoginAct.class);
            startActivity(intAct);
            this.finish();
        }
    }
}
