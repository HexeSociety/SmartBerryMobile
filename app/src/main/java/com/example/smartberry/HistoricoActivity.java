package com.example.smartberry;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartberry.adapters.CicloAdapter;
import com.example.smartberry.models.Ciclo;
import com.example.smartberry.network.SmartBerryRepository;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HistoricoActivity extends BaseActivity {

    private Button today, week, month;
    private TextView readingStatus, cycleStatus, temperatureLegend, humidityLegend;
    private View chart;
    private final List<Ciclo> ciclos = new ArrayList<>();
    private final CicloAdapter adapter = new CicloAdapter(ciclos);
    private int selection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historico);
        configurarNavbar(-1);

        RecyclerView lista = findViewById(R.id.listaCiclos);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adapter);
        today = findViewById(R.id.btnHoje);
        week = findViewById(R.id.btnSemana);
        month = findViewById(R.id.btnMes);
        chart = findViewById(R.id.chartContainer);
        readingStatus = findViewById(R.id.txtHistoricoSubtitle1);
        cycleStatus = findViewById(R.id.txtHistoricoSubtitle2);
        View root = findViewById(android.R.id.content);
        temperatureLegend = textoExistente(root, "Temp Média (24°C)");
        humidityLegend = textoExistente(root, "Umid. Média (70%)");
        temperatureLegend.setText("Temperatura (°C)");
        humidityLegend.setText("Umidade (%)");
        today.setOnClickListener(v -> load(1));
        week.setOnClickListener(v -> load(7));
        month.setOnClickListener(v -> load(30));
        load(1);
    }

    private void load(int days) {
        today.setSelected(days == 1);
        week.setSelected(days == 7);
        month.setSelected(days == 30);
        int request = ++selection;
        readingStatus.setText("Carregando leituras…");
        cycleStatus.setText("Carregando acionamentos…");
        chart.setBackground(null);
        ciclos.clear();
        adapter.notifyDataSetChanged();
        consultar(() -> repository.grafico(days), data -> {
            if (selection != request) return;
            chart.setBackground(new ChartDrawable(data, true, true));
            readingStatus.setText(data.length() == 0 ? "Sem leituras no período"
                    : days == 1 ? "Tendência de hoje" : "Tendência dos últimos " + days + " dias");
        }, error -> {
            if (selection == request) readingStatus.setText(SmartBerryRepository.mensagem(error));
        });
        consultar(() -> repository.historicoIrrigacao(days), data -> {
            if (selection == request) showCycles(data);
        }, error -> {
            if (selection == request) cycleStatus.setText(SmartBerryRepository.mensagem(error));
        });
    }

    private void showCycles(JSONArray data) {
        for (int i = 0; i < data.length(); i++) {
            JSONObject command = data.optJSONObject(i);
            if (command == null) continue;
            String date = command.optString("dataConclusao");
            if (date.length() >= 16) date = date.substring(0, 16).replace('T', ' ');
            ciclos.add(new Ciclo(date, command.optInt("valor")));
        }
        cycleStatus.setText(ciclos.isEmpty() ? "Sem acionamentos no período"
                : "Acionamentos de irrigação confirmados");
        adapter.notifyDataSetChanged();
    }
}
