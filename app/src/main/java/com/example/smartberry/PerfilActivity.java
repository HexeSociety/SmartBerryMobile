package com.example.smartberry;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.AtomicFile;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseUser;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class PerfilActivity extends BaseActivity {

    private Switch switchNotificacoes;
    private Button btnSair;
    private ImageView imgPerfil;
    private AtomicFile fotoPerfil;

    private final ActivityResultLauncher<Void> tirarFoto = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(), this::salvarFoto);

    private final ActivityResultLauncher<String> pedirPermissaoCamera = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), concedida -> {
                if (concedida) abrirCamera();
                else Toast.makeText(this, "Permissão da câmera não concedida", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);
        configurarNavbar(R.id.nav_perfil);

        if (!Conta.sessaoAtiva(this)) return;
        FirebaseUser user = Conta.usuario();
        if (user == null) return;
        atualizarPerfil(user);
        imgPerfil = findViewById(R.id.imgPerfil);
        fotoPerfil = new AtomicFile(new File(getFilesDir(), "foto-perfil-" + user.getUid() + ".png"));
        try (var entrada = fotoPerfil.openRead()) {
            Bitmap foto = BitmapFactory.decodeStream(entrada);
            if (foto != null) imgPerfil.setImageBitmap(foto);
        } catch (IOException ignored) {
            // Mantém a imagem padrão enquanto não houver foto salva.
        }
        imgPerfil.setFocusable(true);
        imgPerfil.setContentDescription("Tirar foto de perfil");
        imgPerfil.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                abrirCamera();
            } else {
                pedirPermissaoCamera.launch(Manifest.permission.CAMERA);
            }
        });

        switchNotificacoes = findViewById(R.id.switchNotificacoes);
        btnSair = findViewById(R.id.btnSair);

        switchNotificacoes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Notificações ativadas",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notificações desativadas",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnSair.setOnClickListener(v -> sair());
    }

    private void atualizarPerfil(FirebaseUser user) {
        var dados = Conta.dadosLocais(this, user);
        var raiz = findViewById(android.R.id.content);
        TextView nome = textoExistente(raiz, "João Silva");
        String nomeExibicao = dados.getString("nome", user.getDisplayName());
        nome.setText(nomeExibicao == null || nomeExibicao.isEmpty() ? user.getEmail() : nomeExibicao);
        String estufa = dados.getString("estufa", "");
        String local = dados.getString("localizacao", "");
        textoExistente(raiz, "Produtor Familiar • Estufa Morangos SP")
                .setText(estufa.isEmpty() ? user.getEmail() : local.isEmpty() ? estufa : estufa + " • " + local);
        String area = dados.getString("area", "");
        textoExistente(raiz, "3 Hectares").setText(area.isEmpty() ? "—" : area + " m²");
        String cultivo = dados.getString("cultivo", "");
        textoExistente(raiz, "Morango Albion").setText(cultivo.isEmpty() ? "—" : cultivo);
    }

    private void sair() {
        Conta.finalizarSessao(this);
        CredentialManager.create(this).clearCredentialStateAsync(new ClearCredentialStateRequest(),
                null, ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<Void, ClearCredentialException>() {
                    @Override public void onResult(Void result) { }
                    @Override public void onError(ClearCredentialException exception) { }
                });
        Conta.abrir(this, LoginActivity.class);
    }

    private void abrirCamera() {
        try {
            tirarFoto.launch(null);
        } catch (ActivityNotFoundException | SecurityException erro) {
            Toast.makeText(this, "Não foi possível abrir a câmera", Toast.LENGTH_SHORT).show();
        }
    }

    private void salvarFoto(Bitmap foto) {
        if (foto == null || fotoPerfil == null) return;

        FileOutputStream saida = null;
        try {
            saida = fotoPerfil.startWrite();
            if (!foto.compress(Bitmap.CompressFormat.PNG, 100, saida)) {
                throw new IOException("Falha ao gravar a foto");
            }
            fotoPerfil.finishWrite(saida);
            imgPerfil.setImageBitmap(foto);
        } catch (IOException erro) {
            fotoPerfil.failWrite(saida);
            Toast.makeText(this, "Não foi possível salvar a foto", Toast.LENGTH_SHORT).show();
        }
    }
}
