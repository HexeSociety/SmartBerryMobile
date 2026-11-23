package com.example.smartberry;

import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import com.example.smartberry.network.SmartBerryRepository;
import org.json.JSONObject;
import org.json.JSONArray;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.activity.EdgeToEdge;

import com.google.android.material.button.MaterialButton;

public class MonitoramentoActivity extends BaseActivity {

    private JSONObject summary = new JSONObject();
    private JSONArray graph = new JSONArray();
    private boolean temperature = true;
    private TextView subtitle, microclimate;
    private View chart;
    private long graphEnd;
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

        chart = findViewById(R.id.chartContainer);
        subtitle = findViewById(R.id.txtMonitoramentoSubtitle);
        microclimate = findViewById(R.id.txtStatusMicroclima);
        subtitle.setText("Carregando leituras…");
        selecionar(true);
        consultar(repository::resumo, data -> {
            summary = data;
            subtitle.setText("Histórico detalhado das últimas 24h");
            selecionar(temperature);
        }, error -> subtitle.setText(SmartBerryRepository.mensagem(error)));
        consultar(repository::grafico24h, data -> {
            graphEnd = System.currentTimeMillis();
            atualizarEixo();
            graph = data;
            selecionar(temperature);
        }, error -> microclimate.setText(SmartBerryRepository.mensagem(error)));
    }

    private void atualizarEixo() {
        View root = findViewById(android.R.id.content);
        String[] rotulos = {"06:00", "12:00", "18:00", "00:00"};
        SimpleDateFormat hora = new SimpleDateFormat("HH:mm", Locale.getDefault());
        TextView[] labels = new TextView[rotulos.length];
        for (int i = 0; i < labels.length; i++) labels[i] = textoExistente(root, rotulos[i]);
        for (int i = 0; i < labels.length; i++) {
            if (labels[i] != null) labels[i].setText(hora.format(new Date(graphEnd - (24 - i * 6) * 3_600_000L)));
        }
    }

    private void selecionar(boolean temperatura) {
        temperature = temperatura;
        btnTemp.setSelected(temperatura);
        btnUmidade.setSelected(!temperatura);

        String field = temperatura ? "temperatura" : "umidade";
        String unit = temperatura ? "°C" : "%";
        txtTituloGrafico.setText(temperatura ? "Gráfico de Temperatura (°C)" : "Gráfico de Umidade");
        txtFaixaIdeal.setText(temperatura ? "Faixa Ideal: 20°C - 26°C" : "Faixa Ideal: 50% - 70%");
        txtAtual.setText(SmartBerryRepository.medida(summary, field + "Atual", unit));
        txtMinima.setText(SmartBerryRepository.medida(summary, field + "Minima24h", unit));
        txtMaxima.setText(SmartBerryRepository.medida(summary, field + "Maxima24h", unit));
        microclimate.setText(summary.optString("ultimaLeitura", "").isEmpty()
                ? "Aguardando leituras" : summary.optBoolean("online") ? "ESP32 online" : "Última leitura disponível");
        chart.setBackground(new ChartDrawable(graph, temperatura, !temperatura,
                graphEnd - 86_400_000L, graphEnd));
    }
}
