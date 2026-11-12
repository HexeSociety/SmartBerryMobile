package com.example.smartberry;

public class Horario {
    private final String hora;       // "06:00"
    private final int duracaoMin;    // 15
    private final boolean concluido; // true = Concluído, false = Pendente

    public Horario(String hora, int duracaoMin, boolean concluido) {
        this.hora = hora;
        this.duracaoMin = duracaoMin;
        this.concluido = concluido;
    }

    public String getHora() { return hora; }
    public int getDuracaoMin() { return duracaoMin; }
    public boolean isConcluido() { return concluido; }
}
