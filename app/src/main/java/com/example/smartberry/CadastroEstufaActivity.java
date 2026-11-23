
package com.example.smartberry;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class CadastroEstufaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_estufa);

        // Componentes da tela
        EditText txtNomeEstufa = findViewById(R.id.txtNomeEstufa);
        EditText txtTipoCultivo = findViewById(R.id.txtTipoCultivo);
        EditText txtArea = findViewById(R.id.txtArea);
        EditText txtLocalizacao = findViewById(R.id.txtLocalizacao);

        Button btnFinalizarCadastro = findViewById(R.id.btnFinalizarCadastro);
        Button btnVoltarEtapa = findViewById(R.id.btnVoltarEtapa);

        // Finalizar cadastro da estufa
        btnFinalizarCadastro.setOnClickListener(v -> {

            String nome = txtNomeEstufa.getText().toString().trim();
            String tipo = txtTipoCultivo.getText().toString().trim();
            String area = txtArea.getText().toString().trim();
            String local = txtLocalizacao.getText().toString().trim();

            if (nome.isEmpty() || tipo.isEmpty() || area.isEmpty() || local.isEmpty()) {
                Toast.makeText(
                        this,
                        "Preencha todos os campos da estufa",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            boolean saved = AuthManager.registerGreenhouse(
                    this, nome, tipo, area, local
            );

            if (saved) {
                startActivity(new android.content.Intent(
                        this, MainActivity.class
                ));
                finish();
            } else {
                Toast.makeText(
                        this,
                        "Erro ao salvar dados da estufa",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Voltar para o cadastro do usuário
        btnVoltarEtapa.setOnClickListener(v -> {
            startActivity(new android.content.Intent(
                    this, CadastroActivity.class
            ));
            finish();
        });
    }
}
