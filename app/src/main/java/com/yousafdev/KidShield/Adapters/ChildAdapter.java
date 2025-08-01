package com.yousafdev.KidShield.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yousafdev.KidShield.Models.Child;
import com.yousafdev.KidShield.R;
import java.util.List;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ChildViewHolder> {

    private List<Child> childList;
    private OnChildListener onChildListener;

    public ChildAdapter(List<Child> childList, OnChildListener onChildListener) {
        this.childList = childList;
        this.onChildListener = onChildListener;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_child, parent, false);
        return new ChildViewHolder(view, onChildListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Child child = childList.get(position);
        holder.childEmail.setText(child.getEmail());
    }

    @Override
    public int getItemCount() {
        return childList.size();
    }

    public class ChildViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView childEmail;
        OnChildListener onChildListener;

        public ChildViewHolder(@NonNull View itemView, OnChildListener onChildListener) {
            super(itemView);
            childEmail = itemView.findViewById(R.id.textView_child_email);
            this.onChildListener = onChildListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onChildListener.onChildClick(getAdapterPosition());
        }
    }

    public interface OnChildListener {
        void onChildClick(int position);
    }
}