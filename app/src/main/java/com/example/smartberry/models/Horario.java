package com.example.smartberry.models;

public class Horario {
    private final String id;
    private final String horario;
    private final int duracaoSegundos;
    private final String status;
    private final String dataApi;
    private final boolean diario;

    public Horario(String id, String horario, String dataApi, int duracaoSegundos, String status, boolean diario) {
        this.id = id;
        this.horario = horario;
        this.duracaoSegundos = duracaoSegundos;
        this.status = status;
        this.dataApi = dataApi;
        this.diario = diario;
    }

    public String getId() { return id; }
    public String getHorario() { return horario; }
    public int getDuracaoSegundos() { return duracaoSegundos; }
    public String getStatus() { return status; }
    public String getDataApi() { return dataApi; }
    public boolean isDiario() { return diario; }
    public boolean podeEditar() { return "PENDENTE".equals(status) || "PAUSADO".equals(status); }
    public boolean emProcessamento() {
        return "DISPARANDO".equals(status) || "AGUARDANDO_PLACA".equals(status) || "DISPARADO".equals(status);
    }
}
