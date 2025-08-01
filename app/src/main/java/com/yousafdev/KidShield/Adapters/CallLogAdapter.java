package com.yousafdev.KidShield.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yousafdev.KidShield.Models.CallLogEntry;
import com.yousafdev.KidShield.R;

import java.util.List;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.ViewHolder> {
    private List<CallLogEntry> callLogs;
    public CallLogAdapter(List<CallLogEntry> callLogs) { this.callLogs = callLogs; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_call_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CallLogEntry log = callLogs.get(position);
        holder.number.setText(log.number);
        String details = log.type + " on " + log.date + " (" + log.duration_seconds + "s)";
        holder.details.setText(details);
    }

    @Override
    public int getItemCount() { return callLogs.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView number, details;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.textView_call_number);
            details = itemView.findViewById(R.id.textView_call_details);
        }
    }
}