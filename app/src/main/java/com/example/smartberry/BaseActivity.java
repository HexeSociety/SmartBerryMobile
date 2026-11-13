package com.example.smartberry;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseActivity extends AppCompatActivity {

    protected void configurarNavbar(int itemSelecionado) {

        BottomNavigationView navbar =
                findViewById(R.id.bottomNavigation);

        // Permite ou desativa a seleção visual dos itens
        if (itemSelecionado == -1) {

            navbar.getMenu().setGroupCheckable(0, true, false);

        } else {

            navbar.getMenu().setGroupCheckable(0, true, true);
            navbar.setSelectedItemId(itemSelecionado);
        }

        navbar.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            // Início
            if (id == R.id.nav_inicio) {

                if (itemSelecionado != R.id.nav_inicio) {
                    abrirTela(DashboardActivity.class);
                }

                return true;
            }

            // Perfil
            if (id == R.id.nav_perfil) {

                if (itemSelecionado != R.id.nav_perfil) {
                    abrirTela(PerfilActivity.class);
                }

                return true;
            }

            // Controle
            if (id == R.id.nav_controle) {

                if (itemSelecionado != R.id.nav_controle) {
                    abrirTela(ControleActivity.class);
                }

                return true;
            }

            // Alertas
            if (id == R.id.nav_alertas) {

                if (itemSelecionado != R.id.nav_alertas) {
                    abrirTela(AlertasActivity.class);
                }

                return true;
            }


            return false;
        });
    }

    private void abrirTela(Class<?> tela) {

        Intent intent = new Intent(this, tela);

        startActivity(intent);

        overridePendingTransition(0, 0);

        finish();
    }
}
