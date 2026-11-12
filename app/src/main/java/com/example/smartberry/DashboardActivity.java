package com.example.smartberry;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;

public class DashboardActivity extends BaseActivity {

    ImageView btn_historico;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        configurarNavbar(R.id.nav_inicio);

        btn_historico =findViewById(R.id.btn_historico);

        btn_historico.setOnClickListener(v ->{

            Intent intent = new Intent(
                    DashboardActivity.this,
                    HistoricoActivity.class
            );

            startActivity(intent);
        });
    }
}