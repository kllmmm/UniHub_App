package com.example.unihub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private List<Event> eventList;
    private OnDeleteClickListener listener;

    public interface OnDeleteClickListener {
        void onDelete(Event event, int position);
    }

    //Constructor
    public EventAdapter(List<Event> eventList, OnDeleteClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtDateTime, txtDescription;
        ImageButton btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);

            txtDateTime = itemView.findViewById(R.id.txtDateTime);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);

        return new ViewHolder(view);
    }

    //Method for binding the data to the views
    @Override
    public void onBindViewHolder(@NonNull EventAdapter.ViewHolder holder, int position) {

        Event event = eventList.get(position);

        holder.txtDateTime.setText(event.getDate() + " - " + event.getTime());
        holder.txtDescription.setText(event.getDescription());

        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {
                //--Build and show the confirmation dialog
                new android.app.AlertDialog.Builder(v.getContext())
                        .setTitle("Διαγραφή")
                        .setMessage("Θέλετε να διαγράψετε το:\n\"" + event.getDescription() + "\"")
                        .setPositiveButton("Διαγραφή", (dialog, which) -> {
                            //--Execute the deletion only if the user confirms
                            listener.onDelete(event, pos);
                        })
                        .setNegativeButton("Ακύρωση", (dialog, which) -> {
                            //--Do nothing, dialog will dismiss automatically
                            dialog.dismiss();
                        })
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
