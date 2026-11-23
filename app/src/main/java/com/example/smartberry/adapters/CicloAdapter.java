package com.example.smartberry.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartberry.R;
import com.example.smartberry.models.Ciclo;
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
        h.txtModo.setText("Modo: Manual");
        h.txtLitros.setText("—");
        h.txtDuracao.setText("Programado: " + c.getDuracaoProgramadaSegundos() + " s");
    }

    @Override
    public int getItemCount() {
        return ciclos.size();
    }

    static class CicloViewHolder extends RecyclerView.ViewHolder {
        final TextView txtData, txtModo, txtLitros, txtDuracao;

        CicloViewHolder(@NonNull View itemView) {
            super(itemView);
            txtData = itemView.findViewById(R.id.txtDataCiclo);
            txtModo = itemView.findViewById(R.id.txtModoCiclo);
            txtLitros = itemView.findViewById(R.id.txtLitrosCiclo);
            txtDuracao = itemView.findViewById(R.id.txtDuracaoCiclo);
        }
    }
}