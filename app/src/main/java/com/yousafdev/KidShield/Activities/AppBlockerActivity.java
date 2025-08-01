package com.yousafdev.KidShield.Activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yousafdev.KidShield.Adapters.AppBlockerAdapter;
import com.yousafdev.KidShield.Models.AppInfo;
import com.yousafdev.KidShield.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppBlockerActivity extends AppCompatActivity implements AppBlockerAdapter.OnAppBlockListener {

    private RecyclerView recyclerView;
    private AppBlockerAdapter adapter;
    private List<AppInfo> fullAppList; // Keep a copy of the full list for searching
    private Map<String, Boolean> blockedStatusMap;
    private ProgressBar progressBar;
    private EditText searchEditText;
    private DatabaseReference childRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_blocker);

        String childUid = getIntent().getStringExtra("CHILD_UID");
        if (childUid == null) {
            Toast.makeText(this, "Child ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        childRef = FirebaseDatabase.getInstance().getReference("users").child(childUid);

        progressBar = findViewById(R.id.progressBar_apps);
        recyclerView = findViewById(R.id.recyclerView_apps);
        searchEditText = findViewById(R.id.editText_search);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fullAppList = new ArrayList<>();
        blockedStatusMap = new HashMap<>();
        // Pass a new empty list to the adapter, which we will update
        adapter = new AppBlockerAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        loadData();

        // Add the TextWatcher for the search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String text) {
        List<AppInfo> filteredList = new ArrayList<>();
        for (AppInfo item : fullAppList) {
            if (item.appName.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.filterList(filteredList);
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);

        childRef.child("blocked_apps").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                blockedStatusMap.clear();
                for (DataSnapshot statusSnapshot : snapshot.getChildren()) {
                    blockedStatusMap.put(statusSnapshot.getKey().replace("_", "."), statusSnapshot.getValue(Boolean.class));
                }
                loadInstalledApps();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AppBlockerActivity.this, "Failed to load statuses.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadInstalledApps() {
        childRef.child("installed_apps").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullAppList.clear();
                for (DataSnapshot appSnapshot : snapshot.getChildren()) {
                    AppInfo app = appSnapshot.getValue(AppInfo.class);
                    if (app != null) {
                        Boolean isBlocked = blockedStatusMap.get(app.packageName);
                        app.isBlocked = (isBlocked != null && isBlocked);
                        fullAppList.add(app);
                    }
                }
                // Initially, display the full list
                adapter.filterList(fullAppList);
                progressBar.setVisibility(View.GONE);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AppBlockerActivity.this, "Failed to load apps.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAppBlockChanged(String packageName, boolean isBlocked) {
        childRef.child("blocked_apps").child(packageName.replace(".", "_")).setValue(isBlocked);
        // Update the master list so the state is preserved during search
        for (AppInfo app : fullAppList) {
            if (app.packageName.equals(packageName)) {
                app.isBlocked = isBlocked;
                break;
            }
        }
    }
}