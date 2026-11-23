package com.example.smartberry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class CadastroActivity extends AppCompatActivity {
    private EditText nome, email, telefone, senha, confirmarSenha;
    private View proxima, voltar;

    private final ActivityResultLauncher<Intent> cadastroEstufa = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    criarConta(result.getData());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);
        Conta.ajustarInsets(this);
        nome = findViewById(R.id.txtNome);
        email = findViewById(R.id.txtEmail);
        telefone = findViewById(R.id.txtTelefone);
        senha = findViewById(R.id.txtSenha);
        confirmarSenha = findViewById(R.id.txtConfirmarSenha);
        senha.setSaveEnabled(false);
        confirmarSenha.setSaveEnabled(false);
        proxima = findViewById(R.id.btnProximaEtapa);
        voltar = findViewById(R.id.btnVoltarLogin);
        proxima.setOnClickListener(v -> {
            if (validar()) cadastroEstufa.launch(new Intent(this, CadastroEstufaActivity.class));
        });
        voltar.setOnClickListener(v -> finish());
    }

    private boolean validar() {
        if (Conta.valor(nome).isEmpty()) {
            nome.setError("Informe seu nome");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(Conta.valor(email)).matches()) {
            email.setError("Informe um e-mail válido");
            return false;
        }
        if (senha.length() < 6) {
            senha.setError("Use pelo menos seis caracteres");
            return false;
        }
        if (!senha.getText().toString().contentEquals(confirmarSenha.getText())) {
            confirmarSenha.setError("As senhas precisam ser iguais");
            return false;
        }
        return true;
    }

    private void criarConta(Intent estufa) {
        if (!validar()) return;
        FirebaseAuth auth = Conta.autenticacao();
        if (auth == null) {
            Conta.mensagem(this, "Configure o Firebase para cadastrar usuários.");
            return;
        }
        String nomeConta = Conta.valor(nome);
        String telefoneConta = Conta.valor(telefone);
        proxima.setEnabled(false);
        voltar.setEnabled(false);
        auth.createUserWithEmailAndPassword(Conta.valor(email), senha.getText().toString())
                .addOnCompleteListener(resultado -> {
                    FirebaseUser user = resultado.isSuccessful() ? resultado.getResult().getUser() : null;
                    if (user == null) {
                        if (!isFinishing() && !isDestroyed()) {
                            proxima.setEnabled(true);
                            voltar.setEnabled(true);
                            Conta.mensagem(this, Conta.erro(resultado.getException()));
                        }
                        return;
                    }
                    Conta.dadosLocais(this, user).edit()
                            .putString("nome", nomeConta)
                            .putString("telefone", telefoneConta)
                            .putString("estufa", estufa.getStringExtra("estufa"))
                            .putString("cultivo", estufa.getStringExtra("cultivo"))
                            .putString("area", estufa.getStringExtra("area"))
                            .putString("localizacao", estufa.getStringExtra("localizacao"))
                            .apply();
                    user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(nomeConta).build())
                            .addOnCompleteListener(atualizacao -> {
                                if (isDestroyed() || isFinishing()) return;
                                if (!atualizacao.isSuccessful()) {
                                    Conta.mensagem(this, "Conta criada. O nome foi salvo neste aparelho.");
                                }
                                senha.getText().clear();
                                confirmarSenha.getText().clear();
                                Conta.iniciarSessao(this, user, false);
                                Conta.abrir(this, DashboardActivity.class);
                            });
                });
    }
}
