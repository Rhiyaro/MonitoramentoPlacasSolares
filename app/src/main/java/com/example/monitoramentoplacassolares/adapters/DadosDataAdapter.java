package com.example.monitoramentoplacassolares.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitoramentoplacassolares.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DadosDataAdapter extends RecyclerView.Adapter<DadosDataAdapter.DadosDataViewHolder>{

    private Context context;
    private List<JSONObject> dados = Collections.synchronizedList(new ArrayList<>());

    public DadosDataAdapter(Context context, List<JSONObject> dados) {
        this.context = context;
        this.dados = dados;
    }

    @NonNull
    @Override
    public DadosDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.card_dados, parent, false);
        return new DadosDataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DadosDataViewHolder holder, int position) {
        JSONObject dado = dados.get(position);

        TextView tvData = holder.textViews.get(0);
        tvData.setText(dado.optString("data", "---"));

        Iterator<String> keysIt = dado.keys();
        String key;
        int i = 1;
        while (keysIt.hasNext()){
            key = keysIt.next();
            TextView tvAux = holder.textViews.get(i);

            if (key.equalsIgnoreCase("data")) continue;

            JSONArray jsonArrayAux = dado.optJSONArray(key);
            if (jsonArrayAux == null) {
                tvAux.setText(dado.optString(key, "---"));
            } else {
                StringBuilder valorMostrado = new StringBuilder();
                valorMostrado.append(jsonArrayAux.optString(0, "-"));
                for (int j = 1; j < jsonArrayAux.length(); j++) {
                    valorMostrado.append(" / ");
                    valorMostrado.append(jsonArrayAux.optString(j, "-"));;
                }
                tvAux.setText(valorMostrado.toString());
            }

            i++;
        }

        for (int j = 0; j < holder.textViews.size(); j++) {
            TextView tvAux = holder.textViews.get(j);
            if (tvAux.getText().toString().equals("-")) {
                tvAux.setText("");
                tvAux.getLayoutParams().width = 0;
            }
        }
    }

    @Override
    public int getItemCount() {
        if (dados != null){
            return dados.size();
        }

        return 0;
    }

    public static class DadosDataViewHolder extends RecyclerView.ViewHolder {

        List<TextView> textViews;

        public DadosDataViewHolder(@NonNull View itemView) {
            super(itemView);

            textViews = new ArrayList<>();
            TextView tvAux;

            tvAux = itemView.findViewById(R.id.txtCardDadosData);
            textViews.add(tvAux);

            tvAux = itemView.findViewById(R.id.txtCardListaDados1);
            textViews.add(tvAux);

            tvAux = itemView.findViewById(R.id.txtCardListaDados2);
            textViews.add(tvAux);

            tvAux = itemView.findViewById(R.id.txtCardListaDados3);
            textViews.add(tvAux);

            tvAux = itemView.findViewById(R.id.txtCardListaDados4);
            textViews.add(tvAux);

            tvAux = itemView.findViewById(R.id.txtCardListaDados5);
            textViews.add(tvAux);

            tvAux = itemView.findViewById(R.id.txtCardListaDados6);
            textViews.add(tvAux);

            tvAux = itemView.findViewById(R.id.txtCardListaDados7);
            textViews.add(tvAux);

            tvAux = itemView.findViewById(R.id.txtCardListaDados8);
            textViews.add(tvAux);
        }
    }
}
