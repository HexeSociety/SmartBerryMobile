package com.example.smartberry;

import com.example.smartberry.AuthManager;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.smartberry.AuthManager;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // UI components
        android.widget.EditText txtEmail = findViewById(R.id.txtEmail);
        android.widget.EditText txtSenha = findViewById(R.id.txtSenha);
        android.widget.Button btnLogin = findViewById(R.id.btnLogin);
        android.widget.TextView txtCadastro = findViewById(R.id.txtCadastro);
        android.view.View btnGoogle = findViewById(R.id.btnLoginGoogle);

        btnLogin.setOnClickListener(v -> {
            String email = txtEmail.getText().toString();
            String senha = txtSenha.getText().toString();
            if (AuthManager.login(this, email, senha)) {
                startActivity(new android.content.Intent(this, MainActivity.class));
                finish();
            } else {
                android.widget.Toast.makeText(this, "Login ou senha incorretos", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        txtCadastro.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, CadastroActivity.class));
        });

        btnGoogle.setOnClickListener(v -> {
            android.widget.Toast.makeText(this, "Login com Google ainda não está disponível", android.widget.Toast.LENGTH_SHORT).show();
        });
    }


    
}