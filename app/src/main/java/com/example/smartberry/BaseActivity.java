package com.example.smartberry;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.smartberry.network.SmartBerryRepository;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseActivity extends AppCompatActivity {
    protected final SmartBerryRepository repository = new SmartBerryRepository();
    private final ExecutorService requests = Executors.newSingleThreadExecutor();

    protected <T> void consultar(Callable<T> request, Consumer<T> success,
                                 Consumer<Exception> failure) {
        requests.execute(() -> {
            try {
                T data = request.call();
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) success.accept(data);
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) failure.accept(error);
                });
            }
        });
    }

    // Alguns campos do protótipo não têm ID. Localize-os pelo texto inicial,
    // uma vez na criação da tela, antes de renderizar os dados.
    protected TextView textoExistente(View view, String original) {
        if (view instanceof TextView && original.contentEquals(((TextView) view).getText())) {
            return (TextView) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                TextView found = textoExistente(group.getChildAt(i), original);
                if (found != null) return found;
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        requests.shutdownNow();
        super.onDestroy();
    }


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

            if (id == R.id.nav_monitoramento) {

                if (itemSelecionado != R.id.nav_monitoramento) {
                    abrirTela(MonitoramentoActivity.class);
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
