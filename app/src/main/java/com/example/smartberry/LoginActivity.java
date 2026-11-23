package com.example.smartberry;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;
import androidx.core.content.ContextCompat;

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private EditText email, senha;
    private View entrar, google, recuperar, cadastro;
    private CancellationSignal selecaoGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        Conta.ajustarInsets(this);
        email = findViewById(R.id.txtEmail);
        senha = findViewById(R.id.txtSenha);
        senha.setSaveEnabled(false);
        email.setText(Conta.emailLembrado(this));
        entrar = findViewById(R.id.btnLogin);
        google = findViewById(R.id.btnLoginGoogle);
        recuperar = findViewById(R.id.txtEsqueceuSenha);
        cadastro = findViewById(R.id.txtCadastro);
        entrar.setOnClickListener(v -> entrarComSenha());
        google.setOnClickListener(v -> entrarComGoogle());
        recuperar.setOnClickListener(v -> recuperarSenha());
        cadastro.setOnClickListener(v -> startActivity(new Intent(this, CadastroActivity.class)));
    }

    private FirebaseAuth autenticar() {
        FirebaseAuth auth = Conta.autenticacao();
        if (auth == null) Conta.mensagem(this, "Configure o Firebase para usar o login.");
        return auth;
    }

    private boolean emailValido() {
        if (Patterns.EMAIL_ADDRESS.matcher(Conta.valor(email)).matches()) return true;
        email.requestFocus();
        email.setError("Informe um e-mail válido");
        return false;
    }

    private void entrarComSenha() {
        if (!emailValido()) return;
        String valorSenha = senha.getText().toString();
        if (valorSenha.isEmpty()) {
            senha.requestFocus();
            senha.setError("Informe a senha");
            return;
        }
        FirebaseAuth auth = autenticar();
        if (auth == null) return;
        ocupado(true);
        concluir(auth.signInWithEmailAndPassword(Conta.valor(email), valorSenha));
    }

    private void recuperarSenha() {
        if (!emailValido()) return;
        FirebaseAuth auth = autenticar();
        if (auth == null) return;
        ocupado(true);
        auth.sendPasswordResetEmail(Conta.valor(email)).addOnCompleteListener(task -> {
            if (isDestroyed() || isFinishing()) return;
            ocupado(false);
            if (task.isSuccessful()) {
                Conta.mensagem(this, "Se o e-mail estiver cadastrado, você receberá o link de recuperação.");
            } else {
                Conta.mensagem(this, Conta.erro(task.getException()));
            }
        });
    }

    private void entrarComGoogle() {
        FirebaseAuth auth = autenticar();
        if (auth == null) return;
        int stringId = getResources().getIdentifier("default_web_client_id", "string",
                getResources().getResourcePackageName(R.string.app_name));
        if (stringId == 0) {
            Conta.mensagem(this, "Ative o login Google e atualize o google-services.json.");
            return;
        }
        ocupado(true);
        selecaoGoogle = new CancellationSignal();
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(new GetSignInWithGoogleOption.Builder(getString(stringId)).build())
                .build();
        CredentialManager.create(this).getCredentialAsync(this, request, selecaoGoogle,
                ContextCompat.getMainExecutor(this), new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse response) {
                        if (isDestroyed() || isFinishing()) return;
                        if (!(response.getCredential() instanceof CustomCredential)
                                || !GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                .equals(response.getCredential().getType())) {
                            falhaGoogle("O Google retornou uma credencial incompatível.");
                            return;
                        }
                        GoogleIdTokenCredential token;
                        try {
                            token = GoogleIdTokenCredential.createFrom(response.getCredential().getData());
                        } catch (Exception error) {
                            falhaGoogle("Não foi possível ler a credencial do Google.");
                            return;
                        }
                        concluir(auth.signInWithCredential(
                                GoogleAuthProvider.getCredential(token.getIdToken(), null)));
                    }

                    @Override
                    public void onError(GetCredentialException error) {
                        if (isDestroyed() || isFinishing()) return;
                        if (error instanceof GetCredentialCancellationException) ocupado(false);
                        else if (error instanceof NoCredentialException) falhaGoogle("Adicione uma conta Google no aparelho.");
                        else falhaGoogle("Não foi possível entrar com o Google. Confira o Firebase e a conexão.");
                    }
                });
    }

    private void falhaGoogle(String mensagem) {
        ocupado(false);
        Conta.mensagem(this, mensagem);
    }

    private void concluir(Task<AuthResult> task) {
        task.addOnCompleteListener(resultado -> {
            if (isDestroyed() || isFinishing()) return;
            FirebaseUser user = Conta.usuario();
            if (!resultado.isSuccessful() || user == null) {
                ocupado(false);
                Conta.mensagem(this, Conta.erro(resultado.getException()));
                return;
            }
            senha.getText().clear();
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Lembrar meu acesso?")
                    .setMessage("O aplicativo pode manter sua sessão e lembrar seu e-mail neste aparelho.")
                    .setPositiveButton("Lembrar", (d, which) -> abrirPainel(user, true))
                    .setNegativeButton("Só agora", (d, which) -> abrirPainel(user, false))
                    .setOnCancelListener(d -> abrirPainel(user, false))
                    .show();
        });
    }

    private void abrirPainel(FirebaseUser user, boolean lembrar) {
        Conta.iniciarSessao(this, user, lembrar);
        Conta.abrir(this, DashboardActivity.class);
    }

    private void ocupado(boolean valor) {
        entrar.setEnabled(!valor);
        google.setEnabled(!valor);
        recuperar.setEnabled(!valor);
        cadastro.setEnabled(!valor);
    }

    @Override
    protected void onDestroy() {
        if (selecaoGoogle != null) selecaoGoogle.cancel();
        super.onDestroy();
    }
}
