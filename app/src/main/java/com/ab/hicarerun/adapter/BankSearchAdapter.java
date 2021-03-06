package com.ab.hicarerun.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ab.hicarerun.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BankSearchAdapter extends RecyclerView.Adapter<BankSearchAdapter.ViewHolder>
        implements Filterable {
    private Context context;
    private List<String> items;
    private List<String> itemsFiltered;
    private BankAdapterListener listener;


    public BankSearchAdapter(Context context, List<String> items) {
        this.context = context;
        this.items = items;
        this.itemsFiltered = items;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bank_row_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, final int position) {
        try {
            final String name = itemsFiltered.get(position);
            int[] attrs = new int[]{R.attr.selectableItemBackground};
            TypedArray typedArray = context.obtainStyledAttributes(attrs);
            int backgroundResource = typedArray.getResourceId(0, 0);
            holder.layout.setBackgroundResource(backgroundResource);
            holder.name.setText(name);
            holder.name.setOnClickListener(v -> listener.onSelected(name, position));
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return itemsFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String query = charSequence.toString();

                List<String> filtered = new ArrayList<>();

                if (query.isEmpty()) {
                    filtered = items;
                } else {
                    for (String movie : items) {
                        if (movie.toLowerCase().contains(query.toLowerCase())) {
                            filtered.add(movie);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.count = filtered.size();
                results.values = filtered;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                itemsFiltered = (ArrayList<String>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void onBankSelected(BankAdapterListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public LinearLayout layout;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.tvName);
            layout = view.findViewById(R.id.lnrbank);

        }

    }


    public interface BankAdapterListener {
        void onSelected(String item, int position);
    }

}
