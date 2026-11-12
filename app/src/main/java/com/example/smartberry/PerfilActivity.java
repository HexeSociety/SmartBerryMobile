package com.example.smartberry;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;


public class PerfilActivity extends BaseActivity {

    private Switch switchNotificacoes;
    private Button btnSair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);
        configurarNavbar(R.id.nav_perfil);

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