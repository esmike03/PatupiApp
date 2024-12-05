package com.patupiapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    RecyclerView recyclerView, recyclerView2;
    DatabaseReference database, database2;
    ReAdapterDestination adapter, adapter2;
    ArrayList<Destination> list, list2;
    ImageButton morebtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView2 = rootView.findViewById(R.id.hotellist);
        recyclerView = rootView.findViewById(R.id.distanationList);

        database2 = FirebaseDatabase.getInstance().getReference("barbershop");
        database = FirebaseDatabase.getInstance().getReference("barbershop");

        recyclerView2.setHasFixedSize(true);
        recyclerView2.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        list2 = new ArrayList<>();
        list = new ArrayList<>();

        adapter2 = new ReAdapterDestination(getContext(), list2);
        adapter = new ReAdapterDestination(getContext(), list);
        recyclerView2.setAdapter(adapter2);
        recyclerView.setAdapter(adapter);

        adapter2.setOnItemClickListener(position -> {
            Destination clickedPlace = list2.get(position);
            Log.d("RecyclerView", "Clicked item: " + clickedPlace.getPlace());
            startAnotherActivity(clickedPlace.getPlace(), clickedPlace.getBackground(), clickedPlace.getInfo(), clickedPlace.getLattitude(), clickedPlace.getLongitude());

        });

        adapter.setOnItemClickListener(position -> {
            Destination clickedPlace = list.get(position);
            Log.d("RecyclerView", "Clicked item: " + clickedPlace.getPlace());
            startAnotherActivity(clickedPlace.getPlace(), clickedPlace.getBackground(), clickedPlace.getInfo(), clickedPlace.getLattitude(), clickedPlace.getLongitude());

        });


        // Load destinations from Firebase
        loadDestinations();
        loadHotels();

        morebtn = rootView.findViewById(R.id.morebtn);
        morebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MoreActivity.class);
                startActivity(intent);
            }
        });


        return rootView;
    }

    // Method to load destinations from Firebase
    private void loadDestinations() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear(); // Clear the list before adding new data

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Destination destination = dataSnapshot.getValue(Destination.class);
                    list.add(destination);

                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    private void loadHotels() {
        database2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list2.clear(); // Clear the list before adding new data

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Destination destination = dataSnapshot.getValue(Destination.class);
                    list2.add(destination);

                }
                adapter2.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    private void startAnotherActivity(String place, String img, String info, Double latitude, Double longitude) {
        // Start another activity with the selected story ID
        Intent intent = new Intent(requireContext(), InfoActivity.class);
        intent.putExtra("place", place); // Pass story ID or other relevant data
        intent.putExtra("img", img); // Pass story ID or other relevant data
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("info", info);
        startActivity(intent);
    }

    // Method to open Google Maps with specified coordinates
// Method to open Google Maps with specified coordinates and set directions


}
