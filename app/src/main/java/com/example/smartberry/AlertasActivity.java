package com.example.smartberry;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import org.json.JSONObject;
import com.example.smartberry.network.SmartBerryRepository;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.Manifest;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;

public class AlertasActivity extends BaseActivity {

    // Flag indicating whether notification permission is granted (Android 13+)
    private boolean notificationsEnabled = false;

    // ---------------------------------------------------------------------
    // UI components (IDs are defined in activity_alertas.xml)
    // ---------------------------------------------------------------------
    private TextView dataHoraTempAlta;
    private TextView descricaoTempAlta;
    private TextView dataHoraUmidadeBaixa;
    private TextView descricaoUmidadeBaixa;
    private TextView dataHoraSensorSemResposta;
    private TextView descricaoSensorSemResposta; // currently used for sensor‑no‑response card
    private TextView dataHoraSemConexao;
    private TextView descricaoSemConexao;
    private TextView dataHoraIrrigacaoConcluida;
    private TextView descricaoIrrigacaoConcluida;
    // NOTE: the layout does NOT contain a dedicated view for ventilation.

    // ---------------------------------------------------------------------
    // SharedPreferences keys for temperature limits
    // ---------------------------------------------------------------------
    private SharedPreferences prefs;
    private static final String PREF_MIN_TEMP = "temp_min";
    private static final String PREF_MAX_TEMP = "temp_max";

    // ---------------------------------------------------------------------
    // Notification channel constants (Android O+)
    // ---------------------------------------------------------------------
    private static final String CHANNEL_ID = "alertas_channel";
    private static final String CHANNEL_NAME = "Alertas de Estufa";
    private static final int NOTIF_ID_TEMP = 1001;
    private static final int NOTIF_ID_IRRIG = 1002;
    private static final int NOTIF_ID_VENT = 1003;

    // ---------------------------------------------------------------------
    // Handler for the periodic refresh (must be cancelled in onDestroy).
    // ---------------------------------------------------------------------
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refresher = new Runnable() {
        @Override
        public void run() {
            buscarDados();
            handler.postDelayed(this, 30_000);
        }
    };

    // ---------------------------------------------------------------------
    // State tracking
    // ---------------------------------------------------------------------
    private Boolean prevVentilacaoLigada = null;
    private Boolean prevIrrigacaoLigada = null;
    private boolean tempLowAlertShown = false;
    private boolean tempHighAlertShown = false;

    // Temporary values for combined irrigation/ventilation handling
    private Boolean newIrrigacaoLigada = null;
    private Boolean newVentilacaoLigada = null;
    private boolean irrigacaoChanged = false;
    private boolean ventilacaoChanged = false;
    private String irrigacaoMsg = "";
    private String ventilacaoMsg = "";
    private String irrigacaoDataHora = "";

    // ---------------------------------------------------------------------
    // Permission request code
    // ---------------------------------------------------------------------
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1001;

    private void solicitarPermissaoNotificacoes() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS);
            } else {
                notificationsEnabled = true;
            }
        } else {
            // Permission not needed on older Android versions
            notificationsEnabled = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                notificationsEnabled = true;
            } else {
                notificationsEnabled = false; // keep toasts only
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alertas);
        configurarNavbar(R.id.nav_alertas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        vincularComponentes();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Create notification channel (no‑op on pre‑O devices)
        criarCanalNotificacoes();
        // Request notification permission (Android 13+)
        solicitarPermissaoNotificacoes();

        // Immediate fetch and start periodic refresh.
        buscarDados();
        handler.postDelayed(refresher, 30_000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevent memory‑leaks by removing pending callbacks.
        handler.removeCallbacksAndMessages(null);
    }

    /** Bind all TextView fields to their IDs defined in the layout. */
    private void vincularComponentes() {
        dataHoraTempAlta = findViewById(R.id.dataHoraTempAlta);
        descricaoTempAlta = findViewById(R.id.descricaoTempAlta);
        dataHoraUmidadeBaixa = findViewById(R.id.dataHoraUmidadeBaixa);
        descricaoUmidadeBaixa = findViewById(R.id.descricaoUmidadeBaixa);
        dataHoraSensorSemResposta = findViewById(R.id.dataHoraSensorSemResposta);
        descricaoSensorSemResposta = findViewById(R.id.descricaoSensorSemResposta);
        dataHoraSemConexao = findViewById(R.id.dataHoraSemComexao);
        descricaoSemConexao = findViewById(R.id.descricaoSemCnexao);
        dataHoraIrrigacaoConcluida = findViewById(R.id.dataHoraIrrigacaoConcluida);
        descricaoIrrigacaoConcluida = findViewById(R.id.descricaoIriigacaoConcluida);
    }

    private void buscarDados() {
        consultar(repositorio::leitura, data -> {
            String stamp = data.optString("dataRegistro").replace('T', ' ');
            if (stamp.length() > 19) stamp = stamp.substring(0, 19);
            dataHoraTempAlta.setText(stamp);
            dataHoraUmidadeBaixa.setText(stamp);
            dataHoraSensorSemResposta.setText(stamp);
            dataHoraSemConexao.setText(stamp);
            descricaoTempAlta.setText("Temperatura: "
                    + SmartBerryRepository.medida(data, "temperatura", "°C"));
            descricaoUmidadeBaixa.setText("Umidade: "
                    + SmartBerryRepository.medida(data, "umidade", "%"));
            descricaoSemConexao.setText("API acessível");
            boolean recent = DashboardActivity.leituraRecente(data);
            descricaoSensorSemResposta.setText(recent
                    ? "Sensor respondendo" : "Aguardando nova leitura do ESP32");
            if (!recent) return;
            double temperature = data.optDouble("temperatura", Double.NaN);
            if (Double.isFinite(temperature)) verificarLimitesTemperatura(temperature);
            if (!data.isNull("irrigacaoLigada")) {
                newIrrigacaoLigada = data.optBoolean("irrigacaoLigada");
                irrigacaoDataHora = stamp;
                irrigacaoMsg = "Irrigação " + (newIrrigacaoLigada ? "ligada" : "desligada");
                irrigacaoChanged = prevIrrigacaoLigada != null
                        && !prevIrrigacaoLigada.equals(newIrrigacaoLigada);
            }
            if (!data.isNull("ventilacaoLigada")) {
                newVentilacaoLigada = data.optBoolean("ventilacaoLigada");
                ventilacaoMsg = "Ventilação " + (newVentilacaoLigada ? "ligada" : "desligada");
                ventilacaoChanged = prevVentilacaoLigada != null
                        && !prevVentilacaoLigada.equals(newVentilacaoLigada);
            }
            processarMudancasAtuadores();
        }, error -> {
            descricaoTempAlta.setText("Temperatura: —");
            descricaoUmidadeBaixa.setText("Umidade: —");
            descricaoIrrigacaoConcluida.setText("Estado da irrigação indisponível");
            descricaoSemConexao.setText(SmartBerryRepository.mensagem(error));
            descricaoSensorSemResposta.setText("Estado do sensor indisponível");
            dataHoraTempAlta.setText("");
            dataHoraUmidadeBaixa.setText("");
            dataHoraIrrigacaoConcluida.setText("");
            dataHoraSemConexao.setText("");
            dataHoraSensorSemResposta.setText("");
        });
    }

    /** Process combined changes for irrigation and ventilation after both have been fetched. */
    private void processarMudancasAtuadores() {
        // Update UI for irrigation (always show the latest state)
        if (newIrrigacaoLigada != null) {
            descricaoIrrigacaoConcluida.setText("Irrigação: " + (newIrrigacaoLigada ? "Ligada" : "Desligada"));
            dataHoraIrrigacaoConcluida.setText(irrigacaoDataHora);
        }
        // Combine notifications according to change flags
        if (irrigacaoChanged && ventilacaoChanged) {
            String combinedMsg = irrigacaoMsg + " e " + ventilacaoMsg;
            Toast.makeText(this, combinedMsg, Toast.LENGTH_SHORT).show();
            if (notificationsEnabled) {
                mostrarNotificacao(NOTIF_ID_IRRIG, "Irrigação/Ventilação", combinedMsg);
            }
        } else if (irrigacaoChanged) {
            Toast.makeText(this, irrigacaoMsg, Toast.LENGTH_SHORT).show();
            if (notificationsEnabled) {
                mostrarNotificacao(NOTIF_ID_IRRIG, "Irrigação", irrigacaoMsg);
            }
        } else if (ventilacaoChanged) {
            Toast.makeText(this, ventilacaoMsg, Toast.LENGTH_SHORT).show();
            if (notificationsEnabled) {
                mostrarNotificacao(NOTIF_ID_VENT, "Ventilação", ventilacaoMsg);
            }
        }
        // Update previous state values
        if (newIrrigacaoLigada != null) {
            prevIrrigacaoLigada = newIrrigacaoLigada;
        }
        if (newVentilacaoLigada != null) {
            prevVentilacaoLigada = newVentilacaoLigada;
        }
        // Reset temporary flags
        irrigacaoChanged = false;
        ventilacaoChanged = false;
        newIrrigacaoLigada = null;
        newVentilacaoLigada = null;
        irrigacaoMsg = "";
        ventilacaoMsg = "";
        irrigacaoDataHora = "";
    }

    /** Checks temperature against user‑defined limits stored in SharedPreferences. */
    private void verificarLimitesTemperatura(double temperatura) {
        if (!prefs.contains(PREF_MIN_TEMP) || !prefs.contains(PREF_MAX_TEMP)) {
            return; // limits not configured yet
        }
        float min = prefs.getFloat(PREF_MIN_TEMP, Float.MIN_VALUE);
        float max = prefs.getFloat(PREF_MAX_TEMP, Float.MAX_VALUE);
        if (temperatura < min) {
            if (!tempLowAlertShown) {
                String msg = "Temperatura abaixo do mínimo";
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                if (notificationsEnabled) {
                    mostrarNotificacao(NOTIF_ID_TEMP, "Temperatura", msg);
                }
                tempLowAlertShown = true;
                tempHighAlertShown = false;
            }
        } else if (temperatura > max) {
            if (!tempHighAlertShown) {
                String msg = "Temperatura acima do máximo";
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                if (notificationsEnabled) {
                    mostrarNotificacao(NOTIF_ID_TEMP, "Temperatura", msg);
                }
                tempHighAlertShown = true;
                tempLowAlertShown = false;
            }
        } else {
            // back within range – reset flags so future alerts may fire again
            tempLowAlertShown = false;
            tempHighAlertShown = false;
        }
    }

    /** Public API to set temperature limits programmatically. */
    public void definirLimitesTemperatura(float min, float max) {
        if (min >= max) {
            throw new IllegalArgumentException("Valor mínimo deve ser menor que o máximo");
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(PREF_MIN_TEMP, min);
        editor.putFloat(PREF_MAX_TEMP, max);
        editor.apply();
    }

    /** Retrieve the currently stored limits (may be NaN if not set). */
    public float[] obterLimitesTemperatura() {
        float min = prefs.getFloat(PREF_MIN_TEMP, Float.NaN);
        float max = prefs.getFloat(PREF_MAX_TEMP, Float.NaN);
        return new float[]{min, max};
    }

    /** Create a notification channel for Android O and above. */
    private void criarCanalNotificacoes() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notificações de alertas da estufa");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /** Show a notification; reuses same ID to replace previous notification of the same type. */
    private void mostrarNotificacao(int id, String title, String text) {
        // Guard against posting when permission not granted
        if (!notificationsEnabled) return;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alertas) // existing drawable in the project
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(id, builder.build());
        }
    }

    // ---------------------------------------------------------------------
    // IMPORTANT NOTE:
    // - Clear‑text HTTP to the local API requires "android:usesCleartextTraffic=\"true\"" in AndroidManifest.xml.
    // - The layout does not contain a dedicated view for ventilation.
    // ---------------------------------------------------------------------
}
