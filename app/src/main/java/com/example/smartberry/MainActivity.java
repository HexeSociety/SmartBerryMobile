package com.example.smartberry;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Conta.sessaoAtiva(this)) {
            Conta.abrir(this, DashboardActivity.class);
        } else {
            Conta.finalizarSessao(this);
            Conta.abrir(this, LoginActivity.class);
        }
    }
}
