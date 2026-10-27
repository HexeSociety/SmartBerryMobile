package com.example.smartberry;
public class Ciclo {
    private final String data;      // "Ontem, 18:00"
    private final String modo;      // "Automático" ou "Manual"
    private final int litros;       // 40
    private final int duracaoMin;   // 15
    private final boolean alerta;   // true se houve problema

    public Ciclo(String data, String modo, int litros, int duracaoMin, boolean alerta) {
        this.data = data;
        this.modo = modo;
        this.litros = litros;
        this.duracaoMin = duracaoMin;
        this.alerta = alerta;
    }

    public String getData() { return data; }
    public String getModo() { return modo; }
    public int getLitros() { return litros; }
    public int getDuracaoMin() { return duracaoMin; }
    public boolean isAlerta() { return alerta; }
}