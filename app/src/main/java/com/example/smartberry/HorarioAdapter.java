package com.example.smartberry;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HorarioAdapter extends RecyclerView.Adapter<HorarioAdapter.HorarioViewHolder> {

    private final List<Horario> horarios;

    public HorarioAdapter(List<Horario> horarios) {
        this.horarios = horarios;
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

        h.txtHora.setText(item.getHora());
        h.txtDuracao.setText("Duração: " + item.getDuracaoMin() + " min");

        if (item.isConcluido()) {
            h.txtStatus.setText("Concluído");
            h.txtStatus.setTextColor(Color.parseColor("#35A736"));
        } else {
            h.txtStatus.setText("Pendente");
            h.txtStatus.setTextColor(Color.parseColor("#E07B00"));
        }
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
