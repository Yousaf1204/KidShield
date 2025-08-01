package com.yousafdev.KidShield.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yousafdev.KidShield.Adapters.CallLogAdapter;
import com.yousafdev.KidShield.Adapters.SmsLogAdapter;
import com.yousafdev.KidShield.Models.CallLogEntry;
import com.yousafdev.KidShield.Models.SmsLogEntry;
import com.yousafdev.KidShield.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChildDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private String childUid;

    private RecyclerView callLogRecyclerView, smsLogRecyclerView;
    private CallLogAdapter callLogAdapter;
    private SmsLogAdapter smsLogAdapter;
    private List<CallLogEntry> callLogList;
    private List<SmsLogEntry> smsLogList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_detail);

        childUid = getIntent().getStringExtra("CHILD_UID");
        String childEmail = getIntent().getStringExtra("CHILD_EMAIL");

        TextView title = findViewById(R.id.textView_child_detail_title);
        title.setText(childEmail);
        progressBar = findViewById(R.id.progressBar_details);

        // MapView Setup
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // App Blocker Button Setup
        Button manageAppsButton = findViewById(R.id.button_manage_apps);
        manageAppsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AppBlockerActivity.class);
            intent.putExtra("CHILD_UID", childUid);
            startActivity(intent);
        });

        // RecyclerViews Setup
        setupRecyclerViews();

        if (childUid != null) {
            listenForDataChanges();
        }
    }

    private void setupRecyclerViews() {
        callLogRecyclerView = findViewById(R.id.recyclerView_call_logs);
        callLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        callLogList = new ArrayList<>();
        callLogAdapter = new CallLogAdapter(callLogList);
        callLogRecyclerView.setAdapter(callLogAdapter);

        smsLogRecyclerView = findViewById(R.id.recyclerView_sms_logs);
        smsLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        smsLogList = new ArrayList<>();
        smsLogAdapter = new SmsLogAdapter(smsLogList);
        smsLogRecyclerView.setAdapter(smsLogAdapter);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void listenForDataChanges() {
        DatabaseReference childDataRef = FirebaseDatabase.getInstance().getReference("users").child(childUid).child("data");

        childDataRef.child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && googleMap != null) {
                    Double lat = snapshot.child("latitude").getValue(Double.class);
                    Double lon = snapshot.child("longitude").getValue(Double.class);
                    if (lat != null && lon != null) {
                        LatLng childLocation = new LatLng(lat, lon);
                        googleMap.clear();
                        googleMap.addMarker(new MarkerOptions().position(childLocation).title("Last known location"));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(childLocation, 15f));
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        childDataRef.child("call_logs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callLogList.clear();
                for(DataSnapshot logSnapshot : snapshot.getChildren()){
                    CallLogEntry entry = logSnapshot.getValue(CallLogEntry.class);
                    callLogList.add(entry);
                }
                Collections.reverse(callLogList);
                callLogAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        childDataRef.child("sms_logs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                smsLogList.clear();
                for(DataSnapshot logSnapshot : snapshot.getChildren()){
                    SmsLogEntry entry = logSnapshot.getValue(SmsLogEntry.class);
                    smsLogList.add(entry);
                }
                Collections.reverse(smsLogList);
                smsLogAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onStop() { super.onStop(); mapView.onStop(); }
    @Override protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}