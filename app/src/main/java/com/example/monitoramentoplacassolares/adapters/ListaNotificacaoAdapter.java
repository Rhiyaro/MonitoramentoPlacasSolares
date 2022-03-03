package com.example.monitoramentoplacassolares.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitoramentoplacassolares.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListaNotificacaoAdapter extends RecyclerView.Adapter<ListaNotificacaoAdapter.ListaNotifViewHolder> {

    private Context context;
    private List<JSONObject> notificacoes = Collections.synchronizedList(new ArrayList<JSONObject>());

    public ListaNotificacaoAdapter(Context context) {
        this.context = context;
    }

    public ListaNotificacaoAdapter(Context context, List<JSONObject> notificacoes) {
        this.context = context;
        this.notificacoes = notificacoes;
    }

    public ListaNotificacaoAdapter(Context context, ArrayList<JSONObject> notificacoes) {
        this.context = context;
        this.notificacoes = notificacoes;
    }

    @NonNull
    @Override
    public ListaNotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.card_notificacao, parent, false);
        return new ListaNotifViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ListaNotifViewHolder holder, int position) {
        JSONObject notificacao = notificacoes.get(position);
        holder.txtData.setText(notificacao.optString("data_criacao"));

        String estado = notificacao.optString("estado");
        holder.txtTipo.setText(estado);

        int cor = Color.RED;
        if(estado.toLowerCase().equals("normal")){
            cor = Color.GREEN;
            holder.txtIndicador.setText("OK");
        } else {
            holder.txtIndicador.setText("FALHA");
        }
        holder.txtIndicador.setBackgroundColor(cor);

        holder.txtConfirm.setText(notificacao.optString("confirmada"));

        final ListaNotifViewHolder auxHolder = holder;

        holder.btDetalhes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abreDetalhes(v, auxHolder);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (notificacoes != null){
            return notificacoes.size();
        }
        return 0;
    }

    public void abreDetalhes(View view, ListaNotifViewHolder holder){
        //TODO: Implementar aba "detalhes"
        Toast.makeText(view.getContext(), "Detalhes Notificação de "+holder.txtData.getText().toString(), Toast.LENGTH_SHORT).show();
    }

    public class ListaNotifViewHolder extends RecyclerView.ViewHolder {

        TextView txtDisparada, txtData;
        TextView txtDizTipo, txtTipo;
        TextView txtDizConf, txtConfirm;
        TextView txtIndicador;
        ImageButton btDetalhes;

        public ListaNotifViewHolder(@NonNull View itemView) {
            super(itemView);

            txtDisparada = itemView.findViewById(R.id.txt_disparada);
            txtData = itemView.findViewById(R.id.txt_data_criacao);
            txtDizTipo = itemView.findViewById(R.id.txt_diz_tipo);
            txtTipo = itemView.findViewById(R.id.txt_tipo);

            txtConfirm = itemView.findViewById(R.id.txt_confirmada);

            txtIndicador = itemView.findViewById(R.id.bt_indicador_falha);
            btDetalhes = itemView.findViewById(R.id.bt_detalhes);
        }
    }
}
