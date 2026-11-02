package com.example.smartberry;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PerfilActivity extends AppCompatActivity {

    private Switch switchNotificacoes;
    private Button btnSair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        switchNotificacoes = findViewById(R.id.switchNotificacoes);
        btnSair = findViewById(R.id.btnSair);

        switchNotificacoes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Notificações ativadas",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notificações desativadas",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnSair.setOnClickListener(v -> {
            Toast.makeText(this, "Saindo da conta...",
                    Toast.LENGTH_SHORT).show();

            finish();
        });
    }
}