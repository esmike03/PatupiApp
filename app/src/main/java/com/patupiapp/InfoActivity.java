package com.patupiapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.DatePickerDialog;

import android.content.Intent;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class InfoActivity extends AppCompatActivity {

    private Button back, locate;
    private ImageView placeimg;
    private TextView latitude, longitude, place, info, cancel;
    private Button booknow;
    EditText editTextDate;

    private DatabaseReference bookingsRef;
    private ValueEventListener bookingListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_info);

        cancel = findViewById(R.id.textView17);

        booknow = findViewById(R.id.button5);
        bookingsRef = FirebaseDatabase.getInstance().getReference("Bookings");

        editTextDate = findViewById(R.id.editTextDate);
        editTextDate.requestFocus();
        editTextDate.setOnClickListener(view -> {

            // Get current date
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Create and show DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    InfoActivity.this,
                    (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                        // Set selected date to EditText
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        editTextDate.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        Intent intent = getIntent();

        if (intent != null) {
            String getplace = getIntent().getStringExtra("place");
            String getimg = getIntent().getStringExtra("img");
            double getlatitude = getIntent().getDoubleExtra("latitude", 0.0);
            double getlongitude = getIntent().getDoubleExtra("longitude", 0.0);
            String getinfo = getIntent().getStringExtra("info");

            placeimg = findViewById(R.id.placeimg);
            info = findViewById(R.id.info);
            latitude = findViewById(R.id.latitude);
            longitude = findViewById(R.id.longitude);
            place = findViewById(R.id.placename);

            info.setText(getinfo);
            place.setText(getplace);
            latitude.setText(String.valueOf(getlatitude));
            longitude.setText(String.valueOf(getlongitude));
            Picasso.get().load(getimg).into(placeimg);

            // Now you can use the storyId in your activity logic
            // For example, load the story content using this ID
        }

        locate = findViewById(R.id.locatebtn);
        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGoogleMaps();
            }
        });

        intent = getIntent();
        String getplace = intent.getStringExtra("place");
        String getimg = intent.getStringExtra("img");
        double getlatitude = intent.getDoubleExtra("latitude", 0.0);
        double getlongitude = intent.getDoubleExtra("longitude", 0.0);
        String getinfo = intent.getStringExtra("info");
        String placeEmail = intent.getStringExtra("placeEmail"); // Pass the email in intent
        String userEmail = intent.getStringExtra("userEmail");
        // Get the logged-in user's email
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String loggedInUserEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;
        String loggedInUserName = auth.getCurrentUser() != null ? auth.getCurrentUser().getDisplayName() : null;
        // Cancel button listener to delete booking
        cancel.setOnClickListener(view -> {
            if (loggedInUserEmail == null) {
                Toast.makeText(InfoActivity.this, "User is not logged in!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Query bookings to find the current booking for the user and place
            bookingsRef.orderByChild("userEmail").equalTo(loggedInUserEmail)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot bookingSnapshot : dataSnapshot.getChildren()) {
                                String bookedPlace = bookingSnapshot.child("placeName").getValue(String.class);
                                String bookedDate = bookingSnapshot.child("date").getValue(String.class);

                                // If the booking matches the current place and date, delete it
                                if (bookedPlace != null && bookedPlace.equals(getplace)) {
                                    String bookingId = bookingSnapshot.getKey(); // Get the booking ID
                                    bookingsRef.child(bookingId).removeValue().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            editTextDate.setText(""); // Clear the date field
                                            booknow.setEnabled(true); // Re-enable booking button
                                            Toast.makeText(InfoActivity.this, "Booking Cancelled.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(InfoActivity.this, "Failed to cancel booking.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(InfoActivity.this, "Error checking bookings: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        booknow.setOnClickListener(view -> {
            String selectedDate = editTextDate.getText().toString();

            if (selectedDate.isEmpty()) {
                editTextDate.setError("Please select a date!");
                editTextDate.requestFocus();
                return;
            }



            if (loggedInUserEmail == null) {
                // Handle case where user is not logged in
                Toast.makeText(InfoActivity.this, "User is not logged in!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Check if the user already has a booking for this place
            DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("Bookings");


            // Check if the user has already booked for this date
//            DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("Bookings");
            bookingsRef.orderByChild("userEmail").equalTo(loggedInUserEmail)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            boolean alreadyBooked = false;

                            for (DataSnapshot bookingSnapshot : dataSnapshot.getChildren()) {
                                String bookedDate = bookingSnapshot.child("date").getValue(String.class);

                                // Check if the user has already booked the selected date
                                if (bookedDate != null && bookedDate.equals(selectedDate)) {
                                    alreadyBooked = true;
                                    break;
                                }
                            }

                            if (alreadyBooked) {
                                // Disable the button if already booked
                                booknow.setEnabled(true);
                                Toast.makeText(InfoActivity.this, "You have already booked for this date.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Proceed with booking
                                booknow.setEnabled(true);
                                String bookingId = bookingsRef.push().getKey(); // Generate unique ID

                                Map<String, Object> bookingDetails = new HashMap<>();
                                bookingDetails.put("date", selectedDate);
                                bookingDetails.put("userEmail", loggedInUserEmail); // Use the logged-in user's email
                                bookingDetails.put("placeName", getplace);
                                bookingDetails.put("placeEmail", placeEmail);
                                bookingDetails.put("userName", loggedInUserName);

                                assert bookingId != null;
                                bookingsRef.child(bookingId).setValue(bookingDetails).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Booking successful
                                        booknow.setEnabled(false);
                                        editTextDate.setText(""); // Clear the date field
                                        attachBookingListener(loggedInUserEmail, getplace);
                                        Toast.makeText(InfoActivity.this, "Booking Successful!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Handle errors
                                        Toast.makeText(InfoActivity.this, "Booking Failed! Try again.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle errors
                            Toast.makeText(InfoActivity.this, "Error checking bookings: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });


        attachBookingListener(loggedInUserEmail, getplace);
        back = findViewById(R.id.backbtn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detach listener to prevent memory leaks
        if (bookingListener != null) {
            bookingsRef.removeEventListener(bookingListener);
        }
    }

    private void attachBookingListener(String loggedInUserEmail, String getplace) {
        bookingListener = bookingsRef.orderByChild("userEmail").equalTo(loggedInUserEmail)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean alreadyBooked = false;

                        for (DataSnapshot bookingSnapshot : dataSnapshot.getChildren()) {
                            String bookedPlace = bookingSnapshot.child("placeName").getValue(String.class);
                            String bookedDate = bookingSnapshot.child("date").getValue(String.class);

                            if (bookedPlace != null && bookedPlace.equals(getplace)) {
                                // User already booked this place
                                editTextDate.setText(bookedDate); // Display the booked date
                                editTextDate.setEnabled(false); // Disable date selection
                                booknow.setEnabled(false); // Disable booking button
                                Toast.makeText(InfoActivity.this, "You have already booked this place.", Toast.LENGTH_SHORT).show();
                                alreadyBooked = true;
                                break;
                            }
                        }

                        if (!alreadyBooked) {
                            // Enable date selection and booking button if not booked
                            editTextDate.setEnabled(true);
                            booknow.setEnabled(true);
                            editTextDate.setText(""); // Clear previous date
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(InfoActivity.this, "Error checking bookings: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void openGoogleMaps() {
        // Destination coordinates (latitude and longitude)
        double destinationLatitude = 9.5566;
        double destinationLongitude = 123.8053;

        // Destination label (for the marker on the map)
        String destinationLabel = "Tourist Destination";

        // Current location (optional, you can set to null if you don't want to specify the starting point)
        String currentLocation = "current location";

        // Create a URI for Google Maps directions
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("https")
                .authority("www.google.com")
                .appendPath("maps")
                .appendPath("dir")
                .appendPath("")
                .appendQueryParameter("api", "1")
                .appendQueryParameter("destination", latitude.getText() + "," + longitude.getText());

        if (currentLocation != null) {
            uriBuilder.appendQueryParameter("origin", currentLocation);
        }

        // Create an Intent to open Google Maps with the directions URI
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uriBuilder.build());

        // Set the package to Google Maps
        mapIntent.setPackage("com.google.android.apps.maps");

        // Check if Google Maps is available on the device
        if (mapIntent.resolveActivity(this.getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }
}