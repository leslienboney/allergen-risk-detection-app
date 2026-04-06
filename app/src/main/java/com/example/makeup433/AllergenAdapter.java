package com.example.makeup433;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class AllergenAdapter extends RecyclerView.Adapter<AllergenAdapter.AllergenViewHolder> {

    public interface Listener {
        void onAllergenClick(Allergen allergen);
        void onAllergenLongClick(Allergen allergen);
    }

    private final List<Allergen> items = new ArrayList<>();
    private final Listener listener;

    public AllergenAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Allergen> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AllergenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_allergen, parent, false);
        return new AllergenViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull AllergenViewHolder holder, int position) {
        Allergen a = items.get(position);
        holder.txtName.setText(a.name);

        if (a.note == null || a.note.trim().isEmpty()) {
            holder.txtNote.setText("(no note)");
        } else {
            holder.txtNote.setText(MessageFormat.format("Note: {0}", a.note));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAllergenClick(a);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onAllergenLongClick(a);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AllergenViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        TextView txtNote;

        AllergenViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtAllergenName);
            txtNote = itemView.findViewById(R.id.txtAllergenNote);
        }
    }
}