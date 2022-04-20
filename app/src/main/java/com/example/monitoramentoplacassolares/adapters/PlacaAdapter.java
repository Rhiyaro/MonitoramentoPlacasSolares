package com.example.monitoramentoplacassolares.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.monitoramentoplacassolares.R;
import com.example.monitoramentoplacassolares.locais.PlacaMonitoramento;

import java.util.List;

public class PlacaAdapter extends BaseAdapter {

    private Context context;
    private List<PlacaMonitoramento> listaPlacas;

    public PlacaAdapter(List<PlacaMonitoramento> listaPlacas) {
        this.listaPlacas = listaPlacas;
    }

    public PlacaAdapter(Context context, List<PlacaMonitoramento> listaPlacas) {
        this.context = context;
        this.listaPlacas = listaPlacas;
    }

    @Override
    public int getCount() {
        return listaPlacas == null ? 0 : listaPlacas.size();
    }

    @Override
    public Object getItem(int position) {
        if (listaPlacas == null) return null;
        else return listaPlacas.size() > 0 ? listaPlacas.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // holder of the views to be reused.
        PlacaViewHolder placaViewHolder;

        PlacaMonitoramento placaSelecionada = listaPlacas.get(position);

        // if no previous views found
        if (convertView == null) {
            // create the container ViewHolder
            placaViewHolder = new PlacaViewHolder();

            // inflate the views from layout for the new row
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.card_placa, parent, false);

            // set the view to the ViewHolder.
            placaViewHolder.tvNome = convertView.findViewById(R.id.tvNomePlaca);

            // save the viewHolder to be reused later.
            convertView.setTag(placaViewHolder);
        } else {
            placaViewHolder = (PlacaViewHolder) convertView.getTag();
        }

        placaViewHolder.tvNome.setText(placaSelecionada.getNome());

        return convertView;
    }

    public static class PlacaViewHolder {
        TextView tvNome;
    }
}
