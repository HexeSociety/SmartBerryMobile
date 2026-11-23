package com.example.smartberry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

final class Conta {
    private static volatile String uidSessaoAtual;

    private Conta() {}

    static FirebaseAuth auth() {
        try {
            return FirebaseAuth.getInstance();
        } catch (IllegalStateException semConfiguracao) {
            return null;
        }
    }

    static FirebaseUser usuario() {
        FirebaseAuth auth = auth();
        return auth == null ? null : auth.getCurrentUser();
    }

    static void abrir(Activity activity, Class<?> tela) {
        Intent intent = new Intent(activity, tela);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    static void ajustarInsets(Activity activity) {
        ViewCompat.setOnApplyWindowInsetsListener(activity.findViewById(R.id.main), (view, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });
    }

    static String valor(EditText campo) {
        return campo.getText().toString().trim();
    }

    static void mensagem(Activity activity, String mensagem) {
        Toast.makeText(activity, mensagem, Toast.LENGTH_LONG).show();
    }

    static String erro(Exception exception) {
        if (exception instanceof FirebaseNetworkException) return "Verifique a conexão com a internet.";
        if (exception instanceof FirebaseAuthException) {
            switch (((FirebaseAuthException) exception).getErrorCode()) {
                case "ERROR_INVALID_EMAIL": return "Informe um e-mail válido.";
                case "ERROR_WEAK_PASSWORD": return "Escolha uma senha mais forte.";
                case "ERROR_EMAIL_ALREADY_IN_USE": return "Já existe uma conta com este e-mail.";
                case "ERROR_WRONG_PASSWORD":
                case "ERROR_USER_NOT_FOUND":
                case "ERROR_INVALID_CREDENTIAL":
                case "ERROR_INVALID_LOGIN_CREDENTIALS": return "E-mail ou senha inválidos.";
                case "ERROR_USER_DISABLED": return "Esta conta está desativada.";
                case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                    return "Este e-mail já está vinculado a outro método de acesso.";
                case "ERROR_OPERATION_NOT_ALLOWED": return "Ative este método de acesso no Firebase.";
                default: break;
            }
        }
        return "Não foi possível concluir a operação. Tente novamente.";
    }

    private static SharedPreferences preferencias(Context context) {
        return context.getSharedPreferences("smartberry_conta", Context.MODE_PRIVATE);
    }

    static boolean sessaoAtiva(Context context) {
        FirebaseUser user = usuario();
        if (user == null) return false;
        String uid = user.getUid();
        return uid.equals(uidSessaoAtual) || uid.equals(preferencias(context).getString("sessao", null));
    }

    static String emailLembrado(Context context) {
        return preferencias(context).getString("email", "");
    }

    static void iniciarSessao(Context context, FirebaseUser user, boolean manter) {
        uidSessaoAtual = user.getUid();
        SharedPreferences.Editor edit = preferencias(context).edit();
        if (manter) edit.putString("sessao", user.getUid()).putString("email", user.getEmail());
        else edit.remove("sessao").remove("email");
        edit.apply();
    }

    static void finalizarSessao(Context context) {
        uidSessaoAtual = null;
        FirebaseAuth auth = auth();
        if (auth != null) auth.signOut();
        preferencias(context).edit().remove("sessao").apply();
    }

    static SharedPreferences dadosLocais(Context context, FirebaseUser user) {
        return context.getSharedPreferences("perfil_" + user.getUid(), Context.MODE_PRIVATE);
    }
}
