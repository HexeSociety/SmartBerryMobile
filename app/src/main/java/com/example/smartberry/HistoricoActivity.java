
package com.example.smartberry;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoricoActivity extends BaseActivity {

    private Button btnHoje;
    private Button btnSemana;
    private Button btnMes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_historico);

        // Navbar sem nenhum item selecionado
        configurarNavbar(-1);

        RecyclerView lista = findViewById(R.id.listaCiclos);

        lista.setLayoutManager(new LinearLayoutManager(this));

        List<Ciclo> ciclos = new ArrayList<>();

        ciclos.add(new Ciclo("Ontem, 18:00", "Automático", 40, 15, false));
        ciclos.add(new Ciclo("Ontem, 06:00", "Manual", 30, 10, true));
        ciclos.add(new Ciclo("Hoje, 12:00", "Manual", 3, 2, true));
        ciclos.add(new Ciclo("Hoje, 17:00", "Manual", 3, 2, true));
        ciclos.add(new Ciclo("Hoje, 21:00", "Manual", 3, 2, true));

        lista.setAdapter(new CicloAdapter(ciclos));

        btnHoje = findViewById(R.id.btnHoje);
        btnSemana = findViewById(R.id.btnSemana);
        btnMes = findViewById(R.id.btnMes);

        atualizarFiltro(btnHoje);

        btnHoje.setOnClickListener(v -> atualizarFiltro(btnHoje));

        btnSemana.setOnClickListener(v -> atualizarFiltro(btnSemana));

        btnMes.setOnClickListener(v -> atualizarFiltro(btnMes));
    }

    private void atualizarFiltro(Button selecionado) {

        btnHoje.setSelected(selecionado == btnHoje);

        btnSemana.setSelected(selecionado == btnSemana);

        btnMes.setSelected(selecionado == btnMes);
    }
}
