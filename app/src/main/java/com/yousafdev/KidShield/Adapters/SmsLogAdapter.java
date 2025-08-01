package com.yousafdev.KidShield.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.yousafdev.KidShield.Models.SmsLogEntry;
import com.yousafdev.KidShield.R;
import java.util.List;

public class SmsLogAdapter extends RecyclerView.Adapter<SmsLogAdapter.ViewHolder> {
    private List<SmsLogEntry> smsLogs;
    public SmsLogAdapter(List<SmsLogEntry> smsLogs) { this.smsLogs = smsLogs; }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sms_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SmsLogEntry log = smsLogs.get(position);
        String address = log.address + " (" + log.type + ")";
        holder.address.setText(address);
        holder.body.setText(log.body);
    }

    @Override
    public int getItemCount() { return smsLogs.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView address, body;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            address = itemView.findViewById(R.id.textView_sms_address);
            body = itemView.findViewById(R.id.textView_sms_body);
        }
    }
}