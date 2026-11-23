package com.example.smartberry;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartberry.adapters.HorarioAdapter;
import com.example.smartberry.models.Horario;
import com.example.smartberry.network.SmartBerryRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.json.JSONObject;

final class Agendamentos {
    private final ControleActivity tela;
    private final boolean irrigacao;
    private final String equipamento;
    private final CompoundButton modo;
    private final List<Horario> horarios = new ArrayList<>();
    private final HorarioAdapter adapter;
    private final TextView titulo;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable atualizarLista = this::atualizar;
    private boolean ativo;
    private boolean atualizando;
    private boolean alterando;

    Agendamentos(ControleActivity tela, boolean irrigacao) {
        this.tela = tela;
        this.irrigacao = irrigacao;
        equipamento = irrigacao ? "irrigação" : "ventilação";
        titulo = irrigacao ? tela.findViewById(R.id.txtProgramacao) : null;
        modo = tela.findViewById(irrigacao ? R.id.switchIrrigacao : R.id.switchVentilacao);
        modo.setEnabled(false);
        modo.setOnClickListener(v -> alterarModo(modo.isChecked()));
        RecyclerView lista = tela.findViewById(irrigacao ? R.id.listaHorarios : R.id.listaHorariosVentilacao);
        lista.setLayoutManager(new LinearLayoutManager(tela));
        adapter = new HorarioAdapter(horarios, this::acoes);
        lista.setAdapter(adapter);
        tela.findViewById(irrigacao ? R.id.btnAdicionarIrrigacao : R.id.btnAdicionarVentilacao)
                .setOnClickListener(v -> escolherFrequencia(null));
    }

    private void alterarModo(boolean ativar) {
        modo.setChecked(!ativar);
        if (alterando) return;
        alterando = true;
        modo.setEnabled(false);
        tela.consultar(() -> tela.repository.alterarModoDiario(irrigacao, ativar), estado -> {
            modo.setChecked(estado);
            modo.setEnabled(true);
            alterando = false;
            mensagem(estado ? "Horários diários ativados." : "Horários diários pausados.");
            atualizar();
        }, erro -> {
            alterando = false;
            modo.setEnabled(true);
            mensagem(SmartBerryRepository.mensagem(erro));
        });
    }

    void iniciar() {
        ativo = true;
        atualizar();
    }

    void parar() {
        ativo = false;
        handler.removeCallbacks(atualizarLista);
    }

    private void atualizar() {
        if (!ativo || atualizando) return;
        handler.removeCallbacks(atualizarLista);
        atualizando = true;
        tela.consultar(() -> new JSONObject().put("modo", tela.repository.modoDiario(irrigacao))
                .put("horarios", tela.repository.listarAgendamentos(irrigacao)), resposta -> {
            if (!alterando) {
                modo.setChecked(resposta.optBoolean("modo"));
                modo.setEnabled(true);
            }
            var dados = resposta.optJSONArray("horarios");
            horarios.clear();
            for (int i = 0; i < dados.length(); i++) {
                JSONObject item = dados.optJSONObject(i);
                if (item == null || "CANCELADO".equals(item.optString("status"))) continue;
                String horario = item.optString("horario");
                boolean diario = item.optBoolean("diario");
                horarios.add(new Horario(item.optString("id"), formatarHorario(horario, diario),
                        horario, item.optInt("duracaoSegundos"), item.optString("status"), diario));
            }
            adapter.notifyDataSetChanged();
            if (titulo != null) titulo.setText(horarios.isEmpty()
                    ? "Sem agendamentos de irrigação" : "Agendamentos de irrigação");
            agendarAtualizacao();
        }, erro -> {
            if (titulo != null) titulo.setText("Agendamentos indisponíveis");
            modo.setEnabled(false);
            agendarAtualizacao();
        });
    }

    private void agendarAtualizacao() {
        atualizando = false;
        if (ativo) handler.postDelayed(atualizarLista, 7000);
    }

    private void escolherFrequencia(Horario horario) {
        new MaterialAlertDialogBuilder(tela)
                .setTitle("Frequência da " + equipamento)
                .setItems(new String[]{"Uma vez", "Todos os dias"},
                        (d, opcao) -> abrirFormulario(horario, opcao == 1))
                .show();
    }

    private void abrirFormulario(Horario item, boolean diario) {
        BottomSheetDialog dialogo = new BottomSheetDialog(tela);
        View formulario = tela.getLayoutInflater().inflate(R.layout.item_bottom_sheet_controle, null);
        dialogo.setContentView(formulario);
        TextView textoTitulo = formulario.findViewById(R.id.txtTituloSheet);
        textoTitulo.setText((item == null ? "Agendar " : "Editar ") + equipamento
                + (diario ? " diária" : ""));
        Button escolher = formulario.findViewById(R.id.btnEscolherHora);
        escolher.setText("Escolher data e hora");
        escolher.setOnClickListener(v -> escolherHorario(escolher, diario));
        EditText duracao = formulario.findViewById(R.id.etDuracao);
        if (item != null) {
            escolher.setTag(item.getDataApi());
            escolher.setText(formatarHorario(item.getDataApi(), diario));
            duracao.setText(String.valueOf(Math.max(1, item.getDuracaoSegundos() / 60)));
        }
        Button salvar = formulario.findViewById(R.id.btnSalvarHorario);
        salvar.setOnClickListener(v -> {
            String horario = (String) escolher.getTag();
            if (horario == null) {
                mensagem("Escolha a data e o horário de São Paulo.");
                return;
            }
            int minutos;
            try {
                minutos = Integer.parseInt(duracao.getText().toString());
            } catch (NumberFormatException erro) {
                minutos = 0;
            }
            if (minutos < 1 || minutos > 5) {
                duracao.setError("Informe entre 1 e 5 minutos");
                return;
            }
            if (alterando) return;
            int segundos = minutos * 60;
            alterando = true;
            salvar.setEnabled(false);
            tela.consultar(() -> {
                tela.repository.salvarAgendamento(irrigacao, item == null ? null : item.getId(),
                        horario, segundos, diario);
                return null;
            }, resultado -> {
                alterando = false;
                dialogo.dismiss();
                mensagem("Agendamento salvo.");
                atualizar();
            }, erro -> {
                alterando = false;
                salvar.setEnabled(true);
                mensagem(SmartBerryRepository.mensagem(erro));
            });
        });
        dialogo.show();
    }

    private void escolherHorario(Button escolher, boolean diario) {
        Calendar agora = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        new DatePickerDialog(tela, (data, ano, mes, dia) ->
                new TimePickerDialog(tela, (relogio, hora, minuto) -> {
                    String horario = String.format(Locale.ROOT, "%04d-%02d-%02dT%02d:%02d:00",
                            ano, mes + 1, dia, hora, minuto);
                    escolher.setTag(horario);
                    escolher.setText(formatarHorario(horario, diario));
                }, agora.get(Calendar.HOUR_OF_DAY), agora.get(Calendar.MINUTE), true).show(),
                agora.get(Calendar.YEAR), agora.get(Calendar.MONTH), agora.get(Calendar.DAY_OF_MONTH)).show();
    }

    private static String formatarHorario(String horario, boolean diario) {
        if (horario.length() < 16) return horario;
        return (diario ? "Diário • " : "") + horario.substring(8, 10) + "/" + horario.substring(5, 7) + "/" + horario.substring(0, 4)
                + " " + horario.substring(11, 16);
    }

    private void acoes(Horario item) {
        String[] opcoes = item.podeEditar() ? new String[]{"Editar", "Excluir"} : new String[]{"Excluir"};
        new MaterialAlertDialogBuilder(tela).setTitle(item.getHorario())
                .setItems(opcoes, (d, opcao) -> {
                    if ("Editar".equals(opcoes[opcao])) escolherFrequencia(item);
                    else confirmarExclusao(item);
                }).show();
    }

    private void confirmarExclusao(Horario item) {
        if (alterando) return;
        new MaterialAlertDialogBuilder(tela)
                .setTitle("Excluir agendamento?")
                .setMessage(item.getHorario())
                .setNegativeButton("Voltar", null)
                .setPositiveButton("Excluir agendamento", (d, w) -> {
                    if (alterando) return;
                    alterando = true;
                    tela.consultar(() -> {
                        tela.repository.excluirAgendamento(irrigacao, item.getId());
                        return null;
                    }, resultado -> {
                        alterando = false;
                        mensagem("Agendamento excluído.");
                        atualizar();
                    }, erro -> {
                        alterando = false;
                        mensagem(SmartBerryRepository.mensagem(erro));
                        atualizar();
                    });
                }).show();
    }

    private void mensagem(String texto) {
        Toast.makeText(tela, texto, Toast.LENGTH_SHORT).show();
    }
}
