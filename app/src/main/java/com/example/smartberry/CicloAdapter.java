package com.example.smartberry;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class CicloAdapter extends RecyclerView.Adapter<CicloAdapter.CicloViewHolder> {

    private final List<Ciclo> ciclos;

    public CicloAdapter(List<Ciclo> ciclos) {
        this.ciclos = ciclos;
    }

    @NonNull
    @Override
    public CicloViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ciclo, parent, false);
        return new CicloViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CicloViewHolder h, int position) {
        Ciclo c = ciclos.get(position);

        h.txtData.setText(c.getData());
        h.txtModo.setText("Modo: " + c.getModo());
        h.txtLitros.setText("-" + c.getLitros() + "L");
        h.txtDuracao.setText("Duração: " + c.getDuracaoMin() + " min");

        // Ajuste visual conforme o alerta
        if (c.isAlerta()) {
            h.txtLitros.setTextColor(Color.parseColor("#D32F2F"));
            h.card.setStrokeColor(Color.parseColor("#D32F2F"));
        } else {
            h.txtLitros.setTextColor(Color.parseColor("#35A736"));
            h.card.setStrokeColor(Color.parseColor("#E8E8DC"));
        }
    }

    @Override
    public int getItemCount() {
        return ciclos.size();
    }

    static class CicloViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView txtData, txtModo, txtLitros, txtDuracao;

        CicloViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardCiclo);
            txtData = itemView.findViewById(R.id.txtDataCiclo);
            txtModo = itemView.findViewById(R.id.txtModoCiclo);
            txtLitros = itemView.findViewById(R.id.txtLitrosCiclo);
            txtDuracao = itemView.findViewById(R.id.txtDuracaoCiclo);
        }
    }
}