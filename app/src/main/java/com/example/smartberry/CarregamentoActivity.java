package com.example.smartberry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CarregamentoActivity extends AppCompatActivity {
    private static final long TEMPO_MINIMO_SPLASH = 1500;
    private static final String PREFS_NAME = "SmartBerryPrefs";
    private static final String KEY_TOKEN = "jwt_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carregamento);

        iniciarVerificacoes();
    }

    private void iniciarVerificacoes() {
        long tempoInicial = System.currentTimeMillis();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // 1. Verifica conexão com a API
            boolean apiConectada = checarStatusApi();

            // 2. Verifica se existe sessão ativa/token salvo
            boolean usuarioAutenticado = verificarSessaoUsuario();

            // 3. Garante o tempo mínimo de tela
            long tempoDecorrido = System.currentTimeMillis() - tempoInicial;
            long tempoRestante = TEMPO_MINIMO_SPLASH - tempoDecorrido;

            if (tempoRestante > 0) {
                try {
                    Thread.sleep(tempoRestante);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // 4. Decisão de navegação na Thread Principal
            mainHandler.post(() -> {
                if (!apiConectada) {
                    Toast.makeText(CarregamentoActivity.this,
                            "Sem conexão com os servidores. Verifique sua internet.",
                            Toast.LENGTH_LONG).show();
                    // Opcional: Permitir ao usuário tentar novamente ou abrir em modo offline
                    return;
                }

                if (usuarioAutenticado) {
                    irParaMain();
                } else {
                    irParaLogin();
                }
            });
        });
    }

    private boolean checarStatusApi() {
        try {
            // Simulação de chamada HTTP GET /ping ou GET /status na sua API
            Thread.sleep(600);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean verificarSessaoUsuario() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String token = preferences.getString(KEY_TOKEN, null);

        // Se houver um token salvo, considera logado.
        // Opcional: Se sua API tiver um endpoint GET /auth/validate, você pode passar o token
        // dentro da checarStatusApi() para confirmar no backend se o token não expirou.
        return token != null && !token.isEmpty();
    }

    private void irParaMain() {
        Intent intent = new Intent(CarregamentoActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void irParaLogin() {
        Intent intent = new Intent(CarregamentoActivity.this, HistoricoActivity.class);
        startActivity(intent);
        finish();
    }
}