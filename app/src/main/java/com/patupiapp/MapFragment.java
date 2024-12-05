package com.patupiapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private MapView mapView;
    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker userMarker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        // Get the context from the activity
        context = rootView.getContext();

        // Initialize the osmdroid configuration using the context
        Configuration.getInstance().load(context, androidx.preference.PreferenceManager.getDefaultSharedPreferences(context));

        // Find the MapView
        mapView = rootView.findViewById(R.id.map);

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        // Check for location permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, proceed to get the location
            getLastKnownLocation();
        } else {
            // Request permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Set the map center and zoom level
        mapView.getController().setCenter(new GeoPoint(48.8566, 2.3522)); // Coordinates for Paris
        mapView.getController().setZoom(16.0); // Zoom level

        // Enable zoom controls (optional)
        mapView.setBuiltInZoomControls(true);

        return rootView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed to get the location
                getLastKnownLocation();
            }
        }
    }

    private void getLastKnownLocation() {
        // Get the last known location
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<android.location.Location>() {
                @Override
                public void onSuccess(android.location.Location location) {
                    if (location != null) {
                        // Use the location
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Now, you can use these latitude and longitude to center the map
                        mapView.getController().setCenter(new GeoPoint(latitude, longitude));

                        // Add a marker to the map at the user's location
                        if (userMarker != null) {
                            mapView.getOverlays().remove(userMarker); // Remove existing marker
                        }

                        Drawable icon = ContextCompat.getDrawable(context, R.drawable.pin_marker);
                        userMarker = new Marker(mapView);
                        userMarker.setIcon(icon);
                        userMarker.setPosition(new GeoPoint(latitude, longitude));
                        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                        mapView.getOverlays().add(userMarker);
                        mapView.invalidate(); // Refresh the map
                    }
                }
            });
        }
    }
}
