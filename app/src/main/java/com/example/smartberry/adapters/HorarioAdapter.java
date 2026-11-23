package com.example.smartberry.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartberry.R;
import com.example.smartberry.models.Horario;

import java.util.List;
import java.util.function.Consumer;

public class HorarioAdapter extends RecyclerView.Adapter<HorarioAdapter.HorarioViewHolder> {

    private final List<Horario> horarios;
    private final Consumer<Horario> acoes;

    public HorarioAdapter(List<Horario> horarios, Consumer<Horario> acoes) {
        this.horarios = horarios;
        this.acoes = acoes;
    }

    @NonNull
    @Override
    public HorarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_horario_controle, parent, false);
        return new HorarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HorarioViewHolder h, int position) {
        Horario item = horarios.get(position);

        h.txtHora.setText(item.getHorario());
        int segundos = item.getDuracaoSegundos();
        h.txtDuracao.setText("Duração: " + (segundos % 60 == 0 ? segundos / 60 + " min" : segundos + " s"));
        String status;
        int cor;
        switch (item.getStatus()) {
            case "EXECUTADO":
                status = "Acionado";
                cor = Color.parseColor("#35A736");
                break;
            case "PAUSADO":
                status = "Pausado";
                cor = Color.GRAY;
                break;
            case "EXPIRADO":
            case "REJEITADO":
                status = "EXPIRADO".equals(item.getStatus()) ? "Expirado" : "Rejeitado";
                cor = Color.parseColor("#B3261E");
                break;
            default:
                status = item.podeEditar() ? "Pendente" : "Aguardando placa";
                cor = Color.parseColor("#E07B00");
        }
        h.txtStatus.setText(status);
        h.txtStatus.setTextColor(cor);
        h.itemView.setOnClickListener(item.emProcessamento() ? null : v -> acoes.accept(item));
        h.itemView.setClickable(!item.emProcessamento());
        h.itemView.setContentDescription(item.emProcessamento()
                ? null : item.getHorario() + ", " + status + ". Toque para opções.");
    }

    @Override
    public int getItemCount() {
        return horarios.size();
    }

    static class HorarioViewHolder extends RecyclerView.ViewHolder {
        final TextView txtHora, txtDuracao, txtStatus;

        HorarioViewHolder(@NonNull View itemView) {
            super(itemView);
            txtHora = itemView.findViewById(R.id.txtHora);
            txtDuracao = itemView.findViewById(R.id.txtDuracaoHorario);
            txtStatus = itemView.findViewById(R.id.txtStatusHorario);
        }
    }
}
