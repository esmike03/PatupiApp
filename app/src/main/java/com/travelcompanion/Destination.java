package com.travelcompanion;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.net.URL;

public class Destination {

    String place, background, info;
    double lattitude, longitude;



    public String getBackground() {
        return background;
    }

    public String getPlace() {
        return place;
    }

    public double getLattitude() {
        return lattitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getInfo() { return info; }
}
