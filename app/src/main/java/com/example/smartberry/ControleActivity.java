package com.example.smartberry;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import com.example.smartberry.network.SmartBerryRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONObject;

public class ControleActivity extends BaseActivity {

    private static final int DURACAO_IRRIGACAO_SEGUNDOS = 30;
    private final Handler atualizacao = new Handler(Looper.getMainLooper());
    private final Runnable atualizarEstado = this::carregarEstado;
    private Button bomba, ventilador;
    private Agendamentos agendamentosIrrigacao, agendamentosVentilacao;
    private boolean aguardandoComando;
    private boolean visivel;
    private boolean estadoCarregado;
    private boolean erroConexaoExibido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_controle);
        configurarNavbar(R.id.nav_controle);

        bomba = findViewById(R.id.btnIrrigar);
        ventilador = findViewById(R.id.btnVentilar);
        bomba.setOnClickListener(v -> controlar(bomba.isActivated(), true));
        ventilador.setOnClickListener(v -> controlar(ventilador.isActivated(), false));
        bomba.setEnabled(false);
        ventilador.setEnabled(false);

        agendamentosIrrigacao = new Agendamentos(this, true);
        agendamentosVentilacao = new Agendamentos(this, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        visivel = true;
        agendamentosIrrigacao.iniciar();
        agendamentosVentilacao.iniciar();
        atualizacao.post(atualizarEstado);
    }

    @Override
    protected void onStop() {
        visivel = false;
        agendamentosIrrigacao.parar();
        agendamentosVentilacao.parar();
        atualizacao.removeCallbacks(atualizarEstado);
        super.onStop();
    }

    private void carregarEstado() {
        if (aguardandoComando) return;
        consultar(repositorio::leitura, data -> {
            if (aguardandoComando || !visivel) return;
            estadoCarregado = DashboardActivity.leituraRecente(data);
            exibirEstado(bomba, data, "irrigacaoLigada", "Irrigar Agora", "Parar Irrigação");
            exibirEstado(ventilador, data, "ventilacaoLigada", "Ventilar Agora", "Parar Ventilação");
            if (!estadoCarregado) mostrarErroConexao("Aguardando uma leitura recente do ESP32");
            else erroConexaoExibido = false;
            atualizacao.postDelayed(atualizarEstado, 3000);
        }, error -> {
            if (aguardandoComando || !visivel) return;
            estadoCarregado = false;
            bomba.setEnabled(false);
            ventilador.setEnabled(false);
            mostrarErroConexao(SmartBerryRepository.mensagem(error));
            atualizacao.postDelayed(atualizarEstado, 3000);
        });
    }

    private void exibirEstado(Button button, JSONObject data, String key, String start, String stop) {
        if (!estadoCarregado || data.isNull(key)) {
            button.setEnabled(false);
            return;
        }
        definirEstado(button, data.optBoolean(key), start, stop);
        button.setEnabled(true);
    }

    private void definirEstado(Button button, boolean active, String start, String stop) {
        button.setActivated(active);
        button.setText(active ? stop : start);
        button.setBackgroundTintList(ColorStateList.valueOf(
                Color.parseColor(active ? "#D32F2F" : "#35A736")));
    }

    private void controlar(boolean active, boolean irrigation) {
        String type = irrigation ? (active ? "PARAR_IRRIGACAO" : "ACIONAR_IRRIGACAO")
                : (active ? "DESLIGAR_VENTILACAO" : "LIGAR_VENTILACAO");
        if (active) {
            enviar(type, null);
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(irrigation ? "Ativar irrigação manual?" : "Ativar ventilação manual?")
                .setMessage(irrigation ? "A bomba será acionada por até 30 segundos."
                        : "A ventilação ficará ligada até receber o comando de parada.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (dialog, which) ->
                        enviar(type, irrigation ? DURACAO_IRRIGACAO_SEGUNDOS : null))
                .show();
    }

    private void enviar(String type, Integer value) {
        if (!estadoCarregado || aguardandoComando) return;
        aguardandoComando = true;
        atualizacao.removeCallbacks(atualizarEstado);
        bomba.setEnabled(false);
        ventilador.setEnabled(false);
        mostrarMensagem("Enviando comando…");
        consultar(() -> repositorio.enviarComando(type, value), id -> aguardarComando(id, 15),
                error -> finalizarComando(SmartBerryRepository.mensagem(error)));
    }

    private void aguardarComando(String id, int attempts) {
        consultar(() -> repositorio.consultarStatusComando(id), status -> {
            if ("EXECUTADO".equals(status)) {
                finalizarComando("Comando executado pelo ESP32");
            } else if ("REJEITADO".equals(status)) {
                finalizarComando("O ESP32 rejeitou o comando. Verifique o modo automático.");
            } else {
                repetirConsulta(id, attempts);
            }
        }, error -> repetirConsulta(id, attempts));
    }

    private void repetirConsulta(String id, int attempts) {
        if (attempts <= 1) {
            finalizarComando("Execução ainda sem confirmação. Consulte o estado do ESP32.");
        } else {
            atualizacao.postDelayed(() -> aguardarComando(id, attempts - 1), 1000);
        }
    }

    private void finalizarComando(String message) {
        aguardandoComando = false;
        if (visivel) {
            mostrarMensagem(message);
            atualizacao.postDelayed(atualizarEstado, 2000);
        }
    }

    private void mostrarErroConexao(String message) {
        if (!erroConexaoExibido) mostrarMensagem(message);
        erroConexaoExibido = true;
    }

    private void mostrarMensagem(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        atualizacao.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
