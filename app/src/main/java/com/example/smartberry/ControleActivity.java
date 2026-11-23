package com.example.smartberry;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import com.example.smartberry.network.SmartBerryRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONObject;

public class ControleActivity extends BaseActivity {

    private static final int DURACAO_IRRIGACAO_SEGUNDOS = 30;
    private final Handler refresh = new Handler(Looper.getMainLooper());
    private final Runnable refreshState = this::loadState;
    private Button pump, fan;
    private CompoundButton autoPump, autoFan;
    private boolean waitingForCommand;
    private boolean visible;
    private boolean stateLoaded;
    private boolean connectionErrorShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_controle);
        configurarNavbar(R.id.nav_controle);

        pump = findViewById(R.id.btnIrrigar);
        fan = findViewById(R.id.btnVentilar);
        pump.setOnClickListener(v -> control(pump.isActivated(), true));
        fan.setOnClickListener(v -> control(fan.isActivated(), false));
        pump.setEnabled(false);
        fan.setEnabled(false);

        autoPump = configureLocalAutomation(R.id.switchIrrigacao, R.id.btnAdicionarIrrigacao);
        autoFan = configureLocalAutomation(R.id.switchVentilacao, R.id.btnAdicionarVentilacao);
    }

    private CompoundButton configureLocalAutomation(int switchId, int buttonId) {
        CompoundButton toggle = findViewById(switchId);
        toggle.setChecked(false);
        toggle.setOnClickListener(v -> {
            toggle.setChecked(!toggle.isChecked());
            toast("Configure a automação pelo painel local do ESP32.");
        });
        findViewById(buttonId).setOnClickListener(v ->
                toast("Configure a automação pelo painel local do ESP32."));
        return toggle;
    }

    @Override
    protected void onStart() {
        super.onStart();
        visible = true;
        refresh.post(refreshState);
    }

    @Override
    protected void onStop() {
        visible = false;
        refresh.removeCallbacks(refreshState);
        super.onStop();
    }

    private void loadState() {
        if (waitingForCommand) return;
        consultar(repository::leitura, data -> {
            if (waitingForCommand || !visible) return;
            stateLoaded = DashboardActivity.leituraRecente(data);
            if (stateLoaded) {
                autoPump.setChecked(data.optBoolean("irrigacaoAutomatica"));
                autoFan.setChecked(data.optBoolean("ventilacaoAutomatica"));
            }
            render(pump, data, "irrigacaoLigada", "Irrigar Agora", "Parar Irrigação");
            render(fan, data, "ventilacaoLigada", "Ventilar Agora", "Parar Ventilação");
            if (!stateLoaded) showConnectionError("Aguardando uma leitura recente do ESP32");
            else connectionErrorShown = false;
            refresh.postDelayed(refreshState, 3000);
        }, error -> {
            if (waitingForCommand || !visible) return;
            stateLoaded = false;
            pump.setEnabled(false);
            fan.setEnabled(false);
            showConnectionError(SmartBerryRepository.mensagem(error));
            refresh.postDelayed(refreshState, 3000);
        });
    }

    private void render(Button button, JSONObject data, String key, String start, String stop) {
        if (!stateLoaded || data.isNull(key)) {
            button.setEnabled(false);
            return;
        }
        setState(button, data.optBoolean(key), start, stop);
        button.setEnabled(true);
    }

    private void setState(Button button, boolean active, String start, String stop) {
        button.setActivated(active);
        button.setText(active ? stop : start);
        button.setBackgroundTintList(ColorStateList.valueOf(
                Color.parseColor(active ? "#D32F2F" : "#35A736")));
    }

    private void control(boolean active, boolean irrigation) {
        String type = irrigation ? (active ? "PARAR_IRRIGACAO" : "ACIONAR_IRRIGACAO")
                : (active ? "DESLIGAR_VENTILACAO" : "LIGAR_VENTILACAO");
        if (active) {
            send(type, null);
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(irrigation ? "Ativar irrigação manual?" : "Ativar ventilação manual?")
                .setMessage(irrigation ? "A bomba será acionada por até 30 segundos."
                        : "A ventilação ficará ligada até receber o comando de parada.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (dialog, which) ->
                        send(type, irrigation ? DURACAO_IRRIGACAO_SEGUNDOS : null))
                .show();
    }

    private void send(String type, Integer value) {
        if (!stateLoaded || waitingForCommand) return;
        waitingForCommand = true;
        refresh.removeCallbacks(refreshState);
        pump.setEnabled(false);
        fan.setEnabled(false);
        toast("Enviando comando…");
        consultar(() -> repository.enviarComando(type, value), id -> awaitCommand(id, 15),
                error -> finishCommand(SmartBerryRepository.mensagem(error)));
    }

    private void awaitCommand(String id, int attempts) {
        consultar(() -> repository.statusComando(id), status -> {
            if ("EXECUTADO".equals(status)) {
                finishCommand("Comando executado pelo ESP32");
            } else if ("REJEITADO".equals(status)) {
                finishCommand("O ESP32 rejeitou o comando. Verifique o modo automático.");
            } else {
                retryCommand(id, attempts);
            }
        }, error -> retryCommand(id, attempts));
    }

    private void retryCommand(String id, int attempts) {
        if (attempts <= 1) {
            finishCommand("Execução ainda sem confirmação. Consulte o estado do ESP32.");
        } else {
            refresh.postDelayed(() -> awaitCommand(id, attempts - 1), 1000);
        }
    }

    private void finishCommand(String message) {
        waitingForCommand = false;
        if (visible) {
            toast(message);
            refresh.postDelayed(refreshState, 2000);
        }
    }

    private void showConnectionError(String message) {
        if (!connectionErrorShown) toast(message);
        connectionErrorShown = true;
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        refresh.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
