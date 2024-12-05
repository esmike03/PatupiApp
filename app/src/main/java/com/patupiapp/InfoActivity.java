package com.patupiapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.Picasso;

public class InfoActivity extends AppCompatActivity {

    private Button back, locate;
    private ImageView placeimg;
    private TextView latitude, longitude, place, info;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_info);

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