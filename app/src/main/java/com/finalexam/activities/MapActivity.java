package com.finalexam.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.finalexam.BuildConfig;
import com.finalexam.MainActivity;
import com.finalexam.R;
import com.finalexam.utils.PlacesUtil;
import com.finalexam.utils.RoutesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, PlacesUtil {
    GoogleMap googleMap;
    double currentLat, currentLng;
    RoutesUtil routesUtil;
    TextView priceTxt, nameTxt, addrTxt;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        // show google map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMapView);
        mapFragment.getMapAsync(this);

        init();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    //    google maps
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {  // google map ready
        this.googleMap = googleMap;
        routesUtil = new RoutesUtil(googleMap);

        getCurLocAndNearCafe();
    }

    // PlacesUtil
    @Override
    public void onStarted() {
    }

    @Override
    public void onFinished() {
        Place cafe = places.get(0);
        LatLng cafeLoc = new LatLng(cafe.getLatitude(), cafe.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(cafeLoc);
        markerOptions.title(cafe.getName());
        markerOptions.snippet(cafe.getVicinity());
        googleMap.addMarker(markerOptions);

        // draw routes
        routesUtil.drawDirections(currentLat + "," + currentLng, cafe.getLatitude() + "," + cafe.getLongitude());


    }

    // utils
    private void init() {
        // find view
        nameTxt = findViewById(R.id.footerCafeName);
        addrTxt = findViewById(R.id.footerAddr);
        priceTxt = findViewById(R.id.footerTotPrice);

        Intent intent = getIntent();
        String totPriceTxt = intent.getStringExtra("price");  // get total price
        priceTxt.setText(totPriceTxt);
    }

    private void getCurLocAndNearCafe() {
        FusedLocationProviderClient locationProvider = new FusedLocationProviderClient(this);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000 * 5);
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        if (location.getLatitude() != currentLat || location.getLongitude() != currentLng) {
                            googleMap.clear();  // remove marker, route
                            places.clear();  // clear cafe lists

                            currentLat = location.getLatitude();
                            currentLng = location.getLongitude();

                            LatLng currentLoc = new LatLng(currentLat, currentLng);

                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15));

                            findPlaces(currentLoc, PlaceType.CAFE);
                        }
                    }
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {  // check location permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {  // check internet permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 0);
        }
        googleMap.setMyLocationEnabled(true);
        locationProvider.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
}
