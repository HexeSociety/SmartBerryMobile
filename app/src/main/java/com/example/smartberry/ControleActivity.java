package com.example.smartberry;

import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Collections;
import java.util.Locale;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;

public class ControleActivity extends BaseActivity {

    private boolean revertendoSwitch = false;
    private void configurarAgendamento(String processo, MaterialSwitch sw, Button btnAdicionar,
                                       RecyclerView lista, List<Horario> dados, int icone) {
        String nome = Character.toUpperCase(processo.charAt(0)) + processo.substring(1);

        lista.setLayoutManager(new LinearLayoutManager(this));
        HorarioAdapter adapter = new HorarioAdapter(dados);
        lista.setAdapter(adapter);

        // Botão adicionar só vale com o modo automático ligado
        atualizarBotaoAdicionar(btnAdicionar, sw.isChecked());
        btnAdicionar.setOnClickListener(v -> abrirNovoHorario(processo, dados, adapter));

        sw.setOnCheckedChangeListener((botao, ligado) -> {
            if (revertendoSwitch) return;

            if (ligado) {
                atualizarBotaoAdicionar(btnAdicionar, true);
                mostrarAlerta("Modo " + nome + " Automático Ativado", icone);
            } else {
                // Volta para ligado até o usuário confirmar
                revertendoSwitch = true;
                sw.setChecked(true);
                revertendoSwitch = false;

                new MaterialAlertDialogBuilder(this)
                        .setTitle("Desativar modo automático?")
                        .setMessage("O modo automático da " + processo + " será desativado e os "
                                + "horários agendados deixarão de ser executados.")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Desativar", (dialog, which) -> {
                            revertendoSwitch = true;
                            sw.setChecked(false);
                            revertendoSwitch = false;
                            atualizarBotaoAdicionar(btnAdicionar, false);
                            mostrarAlerta("Modo " + nome + " Automático Desativado", icone);
                        })
                        .show();
            }
        });
    }

    private void atualizarBotaoAdicionar(Button botao, boolean ativo) {
        botao.setEnabled(ativo);
        botao.setAlpha(ativo ? 1f : 0.5f);
    }

    private void abrirNovoHorario(String processo, List<Horario> dados, HorarioAdapter adapter) {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View v = getLayoutInflater().inflate(R.layout.bottom_sheet_controle, null);
        sheet.setContentView(v);

        TextView titulo = v.findViewById(R.id.txtTituloSheet);
        Button btnHora = v.findViewById(R.id.btnEscolherHora);
        TextInputEditText etDuracao = v.findViewById(R.id.etDuracao);
        Button btnSalvar = v.findViewById(R.id.btnSalvarHorario);

        titulo.setText("Novo horário de " + processo);
        int[] escolhido = {-1, -1}; // hora e minuto

        btnHora.setOnClickListener(x -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(6)
                    .setMinute(0)
                    .setTitleText("Escolha o horário")
                    .build();
            picker.addOnPositiveButtonClickListener(p -> {
                escolhido[0] = picker.getHour();
                escolhido[1] = picker.getMinute();
                btnHora.setText(String.format(Locale.getDefault(), "%02d:%02d",
                        escolhido[0], escolhido[1]));
            });
            picker.show(getSupportFragmentManager(), "hora");
        });

        btnSalvar.setOnClickListener(x -> {
            String txt = etDuracao.getText() == null ? "" : etDuracao.getText().toString().trim();

            if (escolhido[0] < 0) {
                Toast.makeText(this, "Escolha o horário", Toast.LENGTH_SHORT).show();
                return;
            }
            if (txt.isEmpty() || Integer.parseInt(txt) <= 0) {
                etDuracao.setError("Informe a duração em minutos");
                return;
            }

            String hora = String.format(Locale.getDefault(), "%02d:%02d", escolhido[0], escolhido[1]);
            for (Horario h : dados) {
                if (h.getHora().equals(hora)) {
                    Toast.makeText(this, "Já existe um horário às " + hora, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            dados.add(new Horario(hora, Integer.parseInt(txt), false)); // entra como Pendente
            Collections.sort(dados, (a, b) -> a.getHora().compareTo(b.getHora()));
            adapter.notifyDataSetChanged();
            sheet.dismiss();
        });

        sheet.show();
    }
    private void mostrarAlerta(String mensagem, int iconeRes) {
        ViewGroup raiz = findViewById(android.R.id.content);
        View alerta = getLayoutInflater().inflate(R.layout.alerta_controle, raiz, false);
        ((TextView) alerta.findViewById(R.id.txtAlerta)).setText(mensagem);
        ((ImageView) alerta.findViewById(R.id.imgAlerta)).setImageResource(iconeRes);
        float d = getResources().getDisplayMetrics().density;
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP;
        lp.setMargins((int) (20 * d), (int) (48 * d), (int) (20 * d), 0);
        alerta.setLayoutParams(lp);

        alerta.setAlpha(0f);
        raiz.addView(alerta);
        alerta.animate().alpha(1f).setDuration(200).start();

        alerta.postDelayed(() -> alerta.animate().alpha(0f).setDuration(300)
                .withEndAction(() -> raiz.removeView(alerta)).start(), 3000);
    }

    private void configurarControleManual(Button botao, String processo,
                                          String textoIniciar, String textoParar, int icone) {
        String nome = Character.toUpperCase(processo.charAt(0)) + processo.substring(1);

        botao.setOnClickListener(v -> {
            if (!botao.isActivated()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Ativar " + processo + " manual?")
                        .setMessage("Você vai assumir o controle manual da " + processo
                                + ". Ela continuará ligada até você parar.")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Confirmar", (dialog, which) -> {
                            botao.setActivated(true);
                            botao.setText(textoParar);
                            botao.setBackgroundTintList(
                                    ColorStateList.valueOf(Color.parseColor("#D32F2F")));
                            mostrarAlerta(nome + " Iniciada", icone);
                        })
                        .show();
            } else {
                botao.setActivated(false);
                botao.setText(textoIniciar);
                botao.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#35A736")));
                mostrarAlerta(nome + " Encerrada", icone);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controle);
        configurarNavbar(R.id.nav_controle);

        //navbar
        configurarNavbar(R.id.nav_controle);

        // Irrigação
        List<Horario> horariosIrrigacao = new ArrayList<>();

        configurarAgendamento("irrigação",
                findViewById(R.id.switchIrrigacao),
                findViewById(R.id.btnAdicionarIrrigacao),
                findViewById(R.id.listaHorarios),
                horariosIrrigacao,
                R.drawable.icon_gotas);

        // Ventilação
        configurarAgendamento("ventilação",
                findViewById(R.id.switchVentilacao),
                findViewById(R.id.btnAdicionarVentilacao),
                findViewById(R.id.listaHorariosVentilacao),
                new ArrayList<>(),
                R.drawable.icon_ventilacao);

        // Alertas de controle manual
        configurarControleManual(findViewById(R.id.btnIrrigar), "irrigação",
                "Irrigar Agora", "Parar Irrigação", R.drawable.icon_gotas);
        configurarControleManual(findViewById(R.id.btnVentilar), "ventilação",
                "Ventilar Agora", "Parar Ventilação", R.drawable.icon_ventilacao);

        // Lista de horários
        RecyclerView lista = findViewById(R.id.listaHorarios);
        lista.setLayoutManager(new LinearLayoutManager(this));
    }
}