package com.example.smartberry;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;

public class CadastroEstufaActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_estufa);
        Conta.ajustarInsets(this);

        EditText estufa = findViewById(R.id.txtNomeEstufa);
        EditText cultivo = findViewById(R.id.txtTipoCultivo);
        EditText area = findViewById(R.id.txtArea);
        EditText localizacao = findViewById(R.id.txtLocalizacao);
        findViewById(R.id.btnVoltarEtapa).setOnClickListener(v -> finish());
        findViewById(R.id.btnFinalizarCadastro).setOnClickListener(v -> {
            if (Conta.valor(estufa).isEmpty()) {
                estufa.setError("Informe o nome da estufa");
                return;
            }
            if (!areaValida(area)) return;
            Intent dados = new Intent();
            dados.putExtra("estufa", Conta.valor(estufa));
            dados.putExtra("cultivo", Conta.valor(cultivo));
            dados.putExtra("area", Conta.valor(area));
            dados.putExtra("localizacao", Conta.valor(localizacao));
            setResult(RESULT_OK, dados);
            finish();
        });
    }

    private boolean areaValida(EditText area) {
        String valor = Conta.valor(area);
        if (valor.isEmpty()) return true;
        try {
            if (new BigDecimal(valor.replace(',', '.')).signum() > 0) return true;
        } catch (NumberFormatException ignored) {
            // O campo precisa de um valor numérico positivo.
        }
        area.setError("Informe uma área maior que zero");
        return false;
    }
}
