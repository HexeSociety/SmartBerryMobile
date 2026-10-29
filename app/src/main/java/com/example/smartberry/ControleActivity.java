package com.example.smartberry;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;

public class ControleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controle);

        RecyclerView lista = findViewById(R.id.listaHorarios);
        lista.setLayoutManager(new LinearLayoutManager(this));

        List<Horario> horarios = new ArrayList<>();
        horarios.add(new Horario("06:00", 15, true));
        horarios.add(new Horario("12:00", 15, false));
        horarios.add(new Horario("18:00", 15, false));

        lista.setAdapter(new HorarioAdapter(horarios));

        MaterialSwitch switchIrrigacao = findViewById(R.id.switchIrrigacao);
        switchIrrigacao.setOnCheckedChangeListener((botao, ligado) -> {
            // ligado = true → modo automático da irrigação ativado
        });
    }
}