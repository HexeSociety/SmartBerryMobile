package com.example.smartberry.models;

public class Ciclo {
    private final String data;
    private final int duracaoProgramadaSegundos;

    public Ciclo(String data, int duracaoProgramadaSegundos) {
        this.data = data;
        this.duracaoProgramadaSegundos = duracaoProgramadaSegundos;
    }

    public String getData() { return data; }
    public int getDuracaoProgramadaSegundos() { return duracaoProgramadaSegundos; }
}
