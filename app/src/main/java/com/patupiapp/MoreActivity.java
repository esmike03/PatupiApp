package com.patupiapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MoreActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<Destination> list;
    private ReAdapterDestination adapter;
    private SearchView searchView;
    private Context context;
    private Button back;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_more);

        database = FirebaseDatabase.getInstance().getReference("barbershop");

        searchView = findViewById(R.id.search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        list = new ArrayList<>();

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Destination place = dataSnapshot.getValue(Destination.class);
                    list.add(place);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        adapter = new ReAdapterDestination(this,list);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {
            Destination clickedPlace = list.get(position);
            Log.d("RecyclerView", "Clicked item: " + clickedPlace.getPlace());
            startAnotherActivity(clickedPlace.getPlace(), clickedPlace.getBackground(), clickedPlace.getInfo(), clickedPlace.getLattitude(), clickedPlace.getLongitude());

        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void filterList(String newText) {
        ArrayList<Destination> filteredlist = new ArrayList<>();
        for (Destination place : list){
            if (place.getPlace().toLowerCase().contains(newText.toLowerCase())){
                filteredlist.add(place);
            }
        }

        if (filteredlist.isEmpty()){
            Toast.makeText(context, "No Data Found!", Toast.LENGTH_SHORT).show();
        }else{
            adapter.setFilteredList(filteredlist);
        }
    }

    private void startAnotherActivity(String place, String img, String info, Double latitude, Double longitude) {
        // Start another activity with the selected story ID
        Intent intent = new Intent(this, InfoActivity.class);
        intent.putExtra("place", place); // Pass story ID or other relevant data
        intent.putExtra("img", img); // Pass story ID or other relevant data
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("info", info);
        startActivity(intent);
    }
}