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

    private final Handler refresh = new Handler(Looper.getMainLooper());
    private final Runnable refreshReadings = this::loadReading;
    private TextView temperature, humidity, status, subtitle, pump, fan;
    private TextView temperatureBadge, humidityBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        configurarNavbar(R.id.nav_inicio);

        View root = findViewById(android.R.id.content);
        temperature = textoExistente(root, "24°C");
        humidity = textoExistente(root, "68%");
        temperatureBadge = textoExistente((View) temperature.getParent(), "Ideal");
        humidityBadge = textoExistente((View) humidity.getParent(), "Ideal");
        status = textoExistente(root, "ONLINE");
        subtitle = textoExistente(root, "Estufa ativa e monitorada");
        TextView name = textoExistente(root, "Estufa Morangos SP");
        name.setText("Estufa monitorada");
        textoExistente(root, "Olá, João!").setText("SmartBerry 360°");
        textoExistente(root, "Área: 1.200 m²    •    Variedade: Albion").setText("Monitoramento local");
        pump = actuatorState(root, "Irrigação");
        fan = actuatorState(root, "Ventilação");
        findViewById(R.id.btn_historico).setOnClickListener(v ->
                startActivity(new Intent(this, HistoricoActivity.class)));
        render(new JSONObject(), "Carregando leituras…");
        consultar(repository::resumo, data -> name.setText(data.optString("nomeDispositivo",
                "Estufa monitorada")), error -> { });
    }

    private TextView actuatorState(View root, String label) {
        TextView heading = textoExistente(root, label);
        View section = (View) heading.getParent();
        section.setOnClickListener(v -> startActivity(new Intent(this, ControleActivity.class)));
        return textoExistente(section, "Automática •\nAtiva");
    }

    @Override
    protected void onStart() {
        super.onStart();
        refresh.post(refreshReadings);
    }

    @Override
    protected void onStop() {
        refresh.removeCallbacks(refreshReadings);
        super.onStop();
    }

    private void loadReading() {
        consultar(repository::leitura, data -> render(data,
                        leituraRecente(data) ? "Estufa ativa e monitorada" : "Última leitura disponível"),
                error -> render(new JSONObject(), SmartBerryRepository.mensagem(error)));
        refresh.postDelayed(refreshReadings, 7000);
    }

    private void render(JSONObject data, String message) {
        boolean online = leituraRecente(data);
        temperature.setText(SmartBerryRepository.medida(data, "temperatura", "°C"));
        humidity.setText(SmartBerryRepository.medida(data, "umidade", "%"));
        temperatureBadge.setText(faixa(data, "temperatura", 20, 26, online));
        humidityBadge.setText(faixa(data, "umidade", 50, 70, online));
        status.setText(online ? "ONLINE" : "—");
        subtitle.setText(message);
        pump.setText(online ? estado(data, "irrigacaoLigada") : "—");
        fan.setText(online ? estado(data, "ventilacaoLigada") : "—");
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
