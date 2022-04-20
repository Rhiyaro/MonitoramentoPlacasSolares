package com.example.monitoramentoplacassolares.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.monitoramentoplacassolares.R;
import com.example.monitoramentoplacassolares.locais.LocalMonitoramento;

import java.util.List;

public class LocalAdapter extends BaseAdapter {

    private Context context;
    private List<LocalMonitoramento> listaLocais;

    public LocalAdapter(List<LocalMonitoramento> listaLocais) {
        this.listaLocais = listaLocais;
    }

    public LocalAdapter(Context context, List<LocalMonitoramento> listaLocais) {
        this.context = context;
        this.listaLocais = listaLocais;
    }

    @Override
    public int getCount() {
        return listaLocais == null ? 0 : listaLocais.size();
    }

    @Override
    public Object getItem(int position) {
        if (listaLocais == null) return null;
        else return listaLocais.size() > 0 ? listaLocais.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // holder of the views to be reused.
        LocalViewHolder localViewHolder;

        LocalMonitoramento localSelecionado = listaLocais.get(position);

        // if no previous views found
        if (convertView == null) {
            // create the container ViewHolder
            localViewHolder = new LocalViewHolder();

            // inflate the views from layout for the new row
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.card_local, parent, false);

            // set the view to the ViewHolder.
            localViewHolder.tvNome = convertView.findViewById(R.id.tvNomeLocal);

            // save the viewHolder to be reused later.
            convertView.setTag(localViewHolder);
        } else {
            localViewHolder = (LocalViewHolder) convertView.getTag();
        }

        localViewHolder.tvNome.setText(localSelecionado.getNome());

        return convertView;
    }

    public static class LocalViewHolder {
        TextView tvNome;
    }
}
