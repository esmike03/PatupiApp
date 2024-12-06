package com.patupiapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BusinessFragment extends Fragment {

    RecyclerView recyclerView, recyclerView2;
    DatabaseReference database, database2;
    ReAdapterDestination adapter, adapter2;
    ArrayList<Destination> list, list2;
    ImageButton morebtn;
    Button set, map;
    EditText bname, bdescription, blatitude, blongitude;
    ImageView imageView;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri background;

    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_business, container, false);

        bname = rootView.findViewById(R.id.barbershopname);
        bdescription = rootView.findViewById(R.id.barbershopdescription);
        blatitude = rootView.findViewById(R.id.editTextText5);
        blongitude = rootView.findViewById(R.id.editTextText4);
        set = rootView.findViewById(R.id.button3);
        imageView = rootView.findViewById(R.id.imageView);
        map = rootView.findViewById(R.id.button4);

        database = FirebaseDatabase.getInstance().getReference("barbershop");
        set.setEnabled(false);

        String userEmailed = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        database.orderByChild("email").equalTo(userEmailed).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        // Retrieve the data for the matching barbershop
                        Barbershop barbershop = dataSnapshot.getValue(Barbershop.class);

                        if (barbershop != null) {
                            // Set the values in the respective fields
                            bname.setText(barbershop.getPlace());
                            bdescription.setText(barbershop.getInfo());
                            blatitude.setText(String.valueOf(barbershop.getLattitude()));
                            blongitude.setText(String.valueOf(barbershop.getLongitude()));

                            // Load the image into the ImageView


                        }
                    }
                } else {
                    // Email does not exist, enable the button to add a new entry
                    set.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DatabaseError", error.getMessage());
            }
        });

        // Check if the email exists in the database
        database.orderByChild("email").equalTo(userEmailed).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Email exists, keep the button disabled
                    set.setEnabled(false);
                } else {
                    // Email does not exist, enable the button
                    set.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DatabaseError", error.getMessage());
            }
        });
        // Set click listener for latitude field
        blatitude.setOnClickListener(view -> navigateToMapFragment());

        map.setOnClickListener(view -> navigateToMapFragment());

        // Set click listener for longitude field
        blongitude.setOnClickListener(view -> navigateToMapFragment());
        // Set button click listener
        set.setOnClickListener(view -> {
            // Get data from EditText fields
            String place = bname.getText().toString().trim();
            String info = bdescription.getText().toString().trim();
            String latitudeStr = blatitude.getText().toString().trim();
            String longitudeStr = blongitude.getText().toString().trim();

            // Check if fields are not empty
            if (!place.isEmpty() && !info.isEmpty() && !latitudeStr.isEmpty() && !longitudeStr.isEmpty() && background != null) {
                try {
                    // Convert the image to a Base64-encoded string
                    InputStream inputStream = requireContext().getContentResolver().openInputStream(background);
                    byte[] imageBytes = new byte[inputStream.available()];
                    inputStream.read(imageBytes);
                    String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                    // Prepare data for GitHub API
                    String fileName = UUID.randomUUID().toString() + ".jpg"; // Generate a unique file name
                    String repoOwner = "esmike03"; // Replace with your GitHub username
                    String repoName = "patupi-storage"; // Replace with your GitHub repository name
                    String branch = "main"; // Replace with the branch where you want to upload
                    String accessToken = "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"; // Replace with your PAT

                    // GitHub API URL
                    String url = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/contents/" + fileName;

                    // Create the JSON payload
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("message", "Add image: " + fileName);
                    jsonBody.put("content", base64Image);
                    jsonBody.put("branch", branch);

                    // Make the HTTP request to upload the image
                    new Thread(() -> {
                        try {
                            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                            connection.setRequestMethod("PUT");
                            connection.setRequestProperty("Authorization", "token " + accessToken);
                            connection.setRequestProperty("Content-Type", "application/json");
                            connection.setDoOutput(true);

                            OutputStream os = connection.getOutputStream();
                            os.write(jsonBody.toString().getBytes());
                            os.close();

                            int responseCode = connection.getResponseCode();
                            if (responseCode == 201) {
                                // Parse the response to get the image URL
                                InputStream responseStream = connection.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                                StringBuilder responseBuilder = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    responseBuilder.append(line);
                                }
                                reader.close();
                                JSONObject responseJson = new JSONObject(responseBuilder.toString());
                                String imageUrl = responseJson.getJSONObject("content").getString("download_url");

                                // Continue with saving other data
                                double latitude = Double.parseDouble(latitudeStr);
                                double longitude = Double.parseDouble(longitudeStr);

                                String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                                Barbershop barbershop = new Barbershop(place, info, latitude, longitude, imageUrl, userEmail);

                                // Push the Barbershop data to Firebase Database
                                String id = database.push().getKey(); // Generate a unique ID for the new entry
                                if (id != null) {
                                    database.child(id).setValue(barbershop)
                                            .addOnSuccessListener(aVoid -> {
                                                requireActivity().runOnUiThread(() ->
                                                        Toast.makeText(requireContext(), "Barbershop added successfully!", Toast.LENGTH_SHORT).show()
                                                );
                                            })
                                            .addOnFailureListener(e -> {
                                                requireActivity().runOnUiThread(() ->
                                                        Toast.makeText(requireContext(), "Failed to add barbershop: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                                );
                                            });
                                }
                            } else {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "Failed to upload image to GitHub", Toast.LENGTH_SHORT).show()
                                );
                            }
                        } catch (Exception e) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                        }
                    }).start();
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all fields and select an image.", Toast.LENGTH_SHORT).show();
            }
        });



        imageView.setOnClickListener(v -> openImagePicker());
        if (getArguments() != null) {
            double latitude = getArguments().getDouble("latitude");
            double longitude = getArguments().getDouble("longitude");

            // Display the latitude and longitude in TextViews or use them as needed


            blatitude.setText(""+latitude);
            blongitude.setText(""+longitude);
        }
        recyclerView2 = rootView.findViewById(R.id.recycler_view);

        bookingList = new ArrayList<>();
        bookingAdapter = new BookingAdapter(bookingList);
        recyclerView2.setAdapter(bookingAdapter);
//        recyclerView = rootView.findViewById(R.id.distanationList);

        DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("Bookings");
        bookingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                bookingList.clear(); // Clear previous data

                // Get the place name from the bname EditText
                String placeName = bname.getText().toString().trim();

                // Iterate through the bookings and add them to the list if they match the place name
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Booking booking = snapshot.getValue(Booking.class);

                    // Check if the booking's placeName matches the bname text
                    if (booking != null && booking.getPlaceName().equalsIgnoreCase(placeName)) {

                        // Fetch the email from the booking
                        String userEmail = booking.getUserEmail();

                        // Now use the email to fetch the user's name from the "Users" table
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                        usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Check if a user with this email exists
                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                    User user = userSnapshot.getValue(User.class);

                                    if (user != null) {
                                        // Get the user's name from the User object
                                        String userName = user.getUserName();

                                        // Set the userName in the booking object (or create a new object for display)
                                        booking.setUserName(userName);

                                        // Add the booking to the list to display in RecyclerView
                                        bookingList.add(booking);
                                    }
                                }

                                // Notify the adapter about the data change after updating the list
                                bookingAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Handle the error
                                Toast.makeText(getContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle the database error for bookings
                Toast.makeText(getContext(), "Error loading bookings", Toast.LENGTH_SHORT).show();
            }
        });




        database2 = FirebaseDatabase.getInstance().getReference("Barbershop");
        database = FirebaseDatabase.getInstance().getReference("barbershop");

        recyclerView2.setHasFixedSize(true);
        recyclerView2.setLayoutManager(new LinearLayoutManager(getContext()));

//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        list2 = new ArrayList<>();
        list = new ArrayList<>();

        adapter2 = new ReAdapterDestination(getContext(), list2);
        adapter = new ReAdapterDestination(getContext(), list);

//        recyclerView.setAdapter(adapter);

        adapter2.setOnItemClickListener(position -> {
            Destination clickedPlace = list2.get(position);
            Log.d("RecyclerView", "Clicked item: " + clickedPlace.getPlace());
            startAnotherActivity(clickedPlace.getPlace(), clickedPlace.getBackground(), clickedPlace.getInfo(), clickedPlace.getLattitude(), clickedPlace.getLongitude());

        });

//        adapter.setOnItemClickListener(position -> {
//            Destination clickedPlace = list.get(position);
//            Log.d("RecyclerView", "Clicked item: " + clickedPlace.getPlace());
//            startAnotherActivity(clickedPlace.getPlace(), clickedPlace.getBackground(), clickedPlace.getInfo(), clickedPlace.getLattitude(), clickedPlace.getLongitude());
//
//        });


        // Load destinations from Firebase
        loadDestinations();
        loadHotels();

//        morebtn = rootView.findViewById(R.id.morebtn);
//        morebtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getContext(), MoreActivity.class);
//                startActivity(intent);
//            }
//        });


        return rootView;
    }

    private void navigateToMapFragment() {
        // Create a new instance of MapFragment
        MapFragment mapFragment = new MapFragment();

        // Create a bundle to pass data
        Bundle bundle = new Bundle();
        bundle.putString("latitude", blatitude.getText().toString());
        bundle.putString("longitude", blongitude.getText().toString());

        // Set arguments (latitude and longitude) for MapFragment
        mapFragment.setArguments(bundle);

        // Replace current fragment with MapFragment
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, mapFragment) // Replace the container with MapFragment
                .addToBackStack(null) // Add to back stack for navigation
                .commit();
    }
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            background = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), background);
                imageView.setImageBitmap(bitmap); // Display the selected image in the ImageView
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Barbershop {
        private String place;
        private String info;
        private double lattitude;
        private double longitude;

        private String background;
        private String email;

        public Barbershop() {
        }
        public Barbershop(String place, String info, double lattitude, double longitude, String background, String email) {
            this.place = place;
            this.info = info;
            this.lattitude = lattitude;
            this.longitude = longitude;
            this.background = background;
            this.email = email;
        }

        // Getters for Firebase
        public String getPlace() {
            return place;
        }

        public String getInfo() {
            return info;
        }

        public double getLattitude() {
            return lattitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getBackground() {
            return background;
        }
        public String getEmail() {
            return email;
        }
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
