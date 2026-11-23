package com.example.smartberry;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import com.google.android.material.button.MaterialButton;

public class MonitoramentoActivity extends BaseActivity {

    private MaterialButton btnTemp, btnUmidade;
    private TextView txtTituloGrafico, txtFaixaIdeal, txtAtual, txtMinima, txtMaxima;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_monitoramento);
        configurarNavbar(R.id.nav_monitoramento);

        btnTemp = findViewById(R.id.btnTemp);
        btnUmidade = findViewById(R.id.btnUmidade);
        txtTituloGrafico = findViewById(R.id.txtTituloGrafico);
        txtFaixaIdeal = findViewById(R.id.txtFaixaIdeal);
        txtAtual = findViewById(R.id.txtAtual);
        txtMinima = findViewById(R.id.txtMinima);
        txtMaxima = findViewById(R.id.txtMaxima);

        btnTemp.setOnClickListener(v -> selecionar(true));
        btnUmidade.setOnClickListener(v -> selecionar(false));

        selecionar(true); // Temperatura ativa por padrão
    }

    private void selecionar(boolean temperatura) {
        // Usa os selectors já existentes (botao_filtro_fundo / botao_filtro_texto)
        btnTemp.setSelected(temperatura);
        btnUmidade.setSelected(!temperatura);

        if (temperatura) {
            txtTituloGrafico.setText("Gráfico de Temperatura (°C)");
            txtFaixaIdeal.setText("Faixa Ideal: 20°C - 26°C");
            txtAtual.setText("24°C");
            txtMinima.setText("19°C");
            txtMaxima.setText("31°C");
        } else {
            txtTituloGrafico.setText("Gráfico de Umidade");
            txtFaixaIdeal.setText("Faixa Ideal: 50% - 70%");
            txtAtual.setText("60%");
            txtMinima.setText("50%");
            txtMaxima.setText("80%");
        }
    }
}