package com.example.smartberry;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import com.example.smartberry.network.SmartBerryRepository;

import org.json.JSONObject;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardActivity extends BaseActivity {

    private final Handler atualizacao = new Handler(Looper.getMainLooper());
    private final Runnable atualizarLeituras = this::carregarLeitura;
    private TextView temperatura, umidade, status, subtitulo, bomba, ventilador;
    private TextView seloTemperatura, seloUmidade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        configurarNavbar(R.id.nav_inicio);

        View root = findViewById(android.R.id.content);
        temperatura = textoExistente(root, "24°C");
        umidade = textoExistente(root, "68%");
        seloTemperatura = textoExistente((View) temperatura.getParent(), "Ideal");
        seloUmidade = textoExistente((View) umidade.getParent(), "Ideal");
        status = textoExistente(root, "ONLINE");
        subtitulo = textoExistente(root, "Estufa ativa e monitorada");
        TextView name = textoExistente(root, "Estufa Morangos SP");
        name.setText("Estufa monitorada");
        textoExistente(root, "Olá, João!").setText("SmartBerry 360°");
        textoExistente(root, "Área: 1.200 m²    •    Variedade: Albion").setText("Monitoramento local");
        bomba = encontrarEstadoAtuador(root, "Irrigação");
        ventilador = encontrarEstadoAtuador(root, "Ventilação");
        findViewById(R.id.btn_historico).setOnClickListener(v ->
                startActivity(new Intent(this, HistoricoActivity.class)));
        exibirLeitura(new JSONObject(), "Carregando leituras…");
        consultar(repositorio::resumo, data -> name.setText(data.optString("nomeDispositivo",
                "Estufa monitorada")), error -> { });
    }

    private TextView encontrarEstadoAtuador(View root, String label) {
        TextView heading = textoExistente(root, label);
        View section = (View) heading.getParent();
        section.setOnClickListener(v -> startActivity(new Intent(this, ControleActivity.class)));
        return textoExistente(section, "Automática •\nAtiva");
    }

    @Override
    protected void onStart() {
        super.onStart();
        atualizacao.post(atualizarLeituras);
    }

    @Override
    protected void onStop() {
        atualizacao.removeCallbacks(atualizarLeituras);
        super.onStop();
    }

    private void carregarLeitura() {
        consultar(repositorio::leitura, data -> exibirLeitura(data,
                        leituraRecente(data) ? "Estufa ativa e monitorada" : "Última leitura disponível"),
                error -> exibirLeitura(new JSONObject(), SmartBerryRepository.mensagem(error)));
        atualizacao.postDelayed(atualizarLeituras, 7000);
    }

    private void exibirLeitura(JSONObject data, String message) {
        boolean online = leituraRecente(data);
        temperatura.setText(SmartBerryRepository.medida(data, "temperatura", "°C"));
        umidade.setText(SmartBerryRepository.medida(data, "umidade", "%"));
        seloTemperatura.setText(faixa(data, "temperatura", 20, 26, online));
        seloUmidade.setText(faixa(data, "umidade", 50, 70, online));
        status.setText(online ? "ONLINE" : "—");
        subtitulo.setText(message);
        bomba.setText(online ? estado(data, "irrigacaoLigada") : "—");
        ventilador.setText(online ? estado(data, "ventilacaoLigada") : "—");
    }

    private String faixa(JSONObject data, String key, double min, double max, boolean online) {
        double value = data.optDouble(key, Double.NaN);
        if (!online || !Double.isFinite(value)) return "—";
        return value >= min && value <= max ? "Ideal" : "Fora da faixa";
    }

    static boolean leituraRecente(JSONObject data) {
        if (!data.isNull("registroEpochMillis")) {
            return Math.abs(System.currentTimeMillis() - data.optLong("registroEpochMillis")) < 30_000;
        }
        Date timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT)
                .parse(data.optString("dataRegistro"), new ParsePosition(0));
        return timestamp != null && Math.abs(System.currentTimeMillis() - timestamp.getTime()) < 30_000;
    }

    private String estado(JSONObject data, String key) {
        if (data.isNull(key)) return "—";
        return data.optBoolean(key) ? "Ligada" : "Desligada";
    }
}
