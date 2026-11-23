package com.example.smartberry.network;

import android.net.Uri;

import com.example.smartberry.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class SmartBerryRepository {
    private static final String DISCOVERY_PATH = "/api/esp32/automatico";
    private String deviceId = BuildConfig.SMARTBERRY_ESP32_ID.trim();

    public JSONObject leitura() throws Exception {
        return new JSONObject(request("GET", devicePath("/api/sensores/esp32/") + "/ultima", null));
    }

    public JSONObject resumo() throws Exception {
        return new JSONObject(request("GET", devicePath("/api/monitoramento/") + "/atual", null));
    }

    public JSONArray grafico24h() throws Exception {
        return new JSONArray(request("GET", devicePath("/api/monitoramento/")
                + "/grafico?horas=24", null));
    }

    public JSONArray grafico(int dias) throws Exception {
        String periodo = dias == 1 ? "/grafico?hoje=true" : "/historico?dias=" + dias;
        return new JSONArray(request("GET", devicePath("/api/monitoramento/") + periodo, null));
    }

    public JSONArray historicoIrrigacao(int dias) throws Exception {
        return new JSONArray(request("GET", devicePath("/api/comandos/esp32/")
                + "/historico-irrigacao?dias=" + dias, null));
    }

    public String enviarComando(String tipo, Integer valor) throws Exception {
        JSONObject body = new JSONObject().put("esp32Id", resolveDeviceId())
                .put("tipo", tipo);
        if (valor != null) body.put("valor", valor);
        return new JSONObject(request("POST", "/api/comandos", body)).getString("id");
    }

    public JSONArray listarAgendamentos(boolean irrigacao) throws Exception {
        return new JSONArray(request("GET", caminhoAgendamentos(irrigacao), null));
    }

    public boolean modoDiario(boolean irrigacao) throws Exception {
        return new JSONObject(request("GET", caminhoAgendamentos(irrigacao) + "/automatico", null))
                .getBoolean("ativo");
    }

    public boolean alterarModoDiario(boolean irrigacao, boolean ativo) throws Exception {
        return new JSONObject(request("PUT", caminhoAgendamentos(irrigacao) + "/automatico",
                new JSONObject().put("ativo", ativo))).getBoolean("ativo");
    }

    public void salvarAgendamento(boolean irrigacao, String id, String horario,
                                   int duracaoSegundos, boolean diario) throws Exception {
        JSONObject dados = new JSONObject().put("horario", horario)
                .put("duracaoSegundos", duracaoSegundos).put("diario", diario);
        String caminho = caminhoAgendamentos(irrigacao);
        if (id == null) request("POST", caminho, dados);
        else request("PUT", caminho + "/" + Uri.encode(id), dados);
    }

    public void excluirAgendamento(boolean irrigacao, String id) throws Exception {
        request("DELETE", caminhoAgendamentos(irrigacao) + "/" + Uri.encode(id), null);
    }

    private String caminhoAgendamentos(boolean irrigacao) throws Exception {
        if (deviceId.isEmpty()) {
            deviceId = new JSONObject(request("GET", DISCOVERY_PATH + "?incluirOffline=true", null)).getString("id");
        }
        return devicePath("/api/esp32/") + (irrigacao ? "/agendamentos-irrigacao" : "/agendamentos-ventilacao");
    }

    public String statusComando(String id) throws Exception {
        return new JSONObject(request("GET", "/api/comandos/" + Uri.encode(id), null))
                .getString("status");
    }

    private String devicePath(String prefix) throws Exception {
        return prefix + Uri.encode(resolveDeviceId());
    }

    private String resolveDeviceId() throws Exception {
        if (deviceId.isEmpty()) {
            deviceId = new JSONObject(request("GET", DISCOVERY_PATH, null)).getString("id");
        }
        return deviceId;
    }

    private String request(String method, String path, JSONObject body) throws IOException {
        if (BuildConfig.SMARTBERRY_API_KEY.isEmpty()) {
            throw new IOException("Configure a chave da API");
        }
        String base = BuildConfig.SMARTBERRY_API_URL.replaceAll("/+$", "");
        HttpURLConnection connection = (HttpURLConnection) new URL(base + path).openConnection();
        try {
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod(method);
            connection.setRequestProperty("X-SmartBerry-Key", BuildConfig.SMARTBERRY_API_KEY);
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(4000);
            if (body != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                try (java.io.OutputStream output = connection.getOutputStream()) {
                    output.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }
            }
            int status = connection.getResponseCode();
            if (status == 401) throw new IOException("Chave da API inválida");
            if (path.equals(DISCOVERY_PATH) || path.startsWith(DISCOVERY_PATH + "?")) {
                if (status == 404) throw new IOException("Aguardando leituras do ESP32");
                if (status == 409) throw new IOException("Mais de um ESP32 online");
            }
            if (path.contains("/agendamentos-")) {
                if (status == 400) throw new IOException("Confira a data e a duração do agendamento.");
                if (status == 409) throw new IOException("O agendamento não pode mais ser alterado ou a placa está desativada.");
            }
            if (status == 404) throw new IOException("Nenhum dado disponível");
            if (status < 200 || status >= 300) throw new IOException("Erro da API (HTTP " + status + ")");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                return response.toString();
            }
        } finally {
            connection.disconnect();
        }
    }

    public static String medida(JSONObject data, String key, String unit) {
        double value = data.optDouble(key, Double.NaN);
        return Double.isFinite(value) ? String.format(Locale.getDefault(), "%.1f%s", value, unit) : "—";
    }

    public static String mensagem(Exception error) {
        if (error instanceof java.net.SocketTimeoutException
                || error instanceof java.net.ConnectException
                || error instanceof java.net.UnknownHostException) return "API indisponível";
        return error instanceof IOException && error.getMessage() != null
                ? error.getMessage() : "Falha ao consultar a API";
    }
}
