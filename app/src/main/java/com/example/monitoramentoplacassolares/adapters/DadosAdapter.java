package com.example.monitoramentoplacassolares.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.monitoramentoplacassolares.R;

import org.json.JSONObject;

public class DadosAdapter extends RecyclerView.Adapter<DadosAdapter.DadosViewHolder> {
    public static final String TAG = "DadosAdapter";

    //TODO: Adaptar para JSONObjects

    private String[] Dados;
    private JSONObject dados;

    public DadosAdapter(String Dados) {
        if (Dados != null) {
            this.Dados = Dados.split(";");

        }else
            this.Dados = null;
    }

    public DadosAdapter(JSONObject dados){
        this.dados = dados;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public DadosViewHolder onCreateViewHolder(ViewGroup parent,
                                              int viewType) {
        // create a new view
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_rcdados, parent, false);
        DadosViewHolder vh = new DadosViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(DadosViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

//        if(this.Dados != null) {
//            String dados[] = this.Dados[position].split(",");
//            for (int i = 0; i < holder.textView.length; i++) {
//                holder.textView[i].setText((dados[i]));
//            }
//        }
        //TODO: MODIFICAR PARA JSONOBJECTS
        if(this.dados != null){
            //Iterator dadosIt = this.dados.optJSONArray()
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if(Dados == null)
            return 0;
        return this.Dados.length;
    }

    public static class DadosViewHolder extends RecyclerView.ViewHolder {

        public TextView[] textView = new TextView[7];

        public DadosViewHolder(View v) {
            super(v);
            textView[0] = (TextView) v.findViewById(R.id.txtDados1);
            textView[1] = (TextView) v.findViewById(R.id.txtDados2);
            textView[2] = (TextView) v.findViewById(R.id.txtDados3);
            textView[3] = (TextView) v.findViewById(R.id.txtDados4);
            textView[4] = (TextView) v.findViewById(R.id.txtDados5);
            textView[5] = (TextView) v.findViewById(R.id.txtDados6);
            textView[6] = (TextView) v.findViewById(R.id.txtDados7);

        }
    }
}
