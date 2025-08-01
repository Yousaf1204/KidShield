package com.yousafdev.KidShield.Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.yousafdev.KidShield.Models.AppInfo;
import com.yousafdev.KidShield.R;
import java.util.ArrayList;
import java.util.List;

public class AppBlockerAdapter extends RecyclerView.Adapter<AppBlockerAdapter.ViewHolder> {

    private List<AppInfo> appList;
    private OnAppBlockListener listener;

    public AppBlockerAdapter(List<AppInfo> appList, OnAppBlockListener listener) {
        this.appList = appList;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo app = appList.get(position);
        holder.appName.setText(app.appName);

        holder.blockSwitch.setOnCheckedChangeListener(null);
        holder.blockSwitch.setChecked(app.isBlocked);

        holder.blockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                app.isBlocked = isChecked; // Update the model state
                listener.onAppBlockChanged(app.packageName, isChecked);
            }
        });
    }

    @Override public int getItemCount() { return appList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView appName;
        SwitchMaterial blockSwitch;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.textView_app_name);
            blockSwitch = itemView.findViewById(R.id.switch_block_app);
        }
    }

    public interface OnAppBlockListener {
        void onAppBlockChanged(String packageName, boolean isBlocked);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(List<AppInfo> filteredList) {
        appList = filteredList;
        notifyDataSetChanged();
    }
}