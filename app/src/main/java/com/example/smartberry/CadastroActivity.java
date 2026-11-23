package com.example.smartberry;

import com.example.smartberry.AuthManager;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CadastroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // UI components
        android.widget.EditText txtNome = findViewById(R.id.txtNome);
        android.widget.EditText txtEmail = findViewById(R.id.txtEmail);
        android.widget.EditText txtTelefone = findViewById(R.id.txtTelefone);
        android.widget.EditText txtSenha = findViewById(R.id.txtSenha);
        android.widget.EditText txtConfirmarSenha = findViewById(R.id.txtConfirmarSenha);
        android.widget.Button btnProximaEtapa = findViewById(R.id.btnProximaEtapa);
        android.widget.Button btnVoltarLogin = findViewById(R.id.btnVoltarLogin);

        btnProximaEtapa.setOnClickListener(v -> {
            String nome = txtNome.getText().toString().trim();
            String email = txtEmail.getText().toString().trim();
            String telefone = txtTelefone.getText().toString().trim();
            String senha = txtSenha.getText().toString();
            String confirmar = txtConfirmarSenha.getText().toString();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmar.isEmpty()) {
                android.widget.Toast.makeText(this, "Preencha todos os campos obrigatórios", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            if (!senha.equals(confirmar)) {
                android.widget.Toast.makeText(this, "Senhas não coincidem", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            boolean success = AuthManager.registerUser(this, email, senha, nome, telefone);
            if (success) {
                // go to greenhouse registration
                startActivity(new android.content.Intent(this, CadastroEstufaActivity.class));
                finish();
            } else {
                android.widget.Toast.makeText(this, "E‑mail já cadastrado ou dados inválidos", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        btnVoltarLogin.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, LoginActivity.class));
            finish();
        });
    }
}