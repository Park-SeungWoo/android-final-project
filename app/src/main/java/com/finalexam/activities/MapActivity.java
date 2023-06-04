package com.finalexam.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.google.android.gms.maps.model.MarkerOptions;

import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, PlacesUtil {
    GoogleMap googleMap;
    double currentLat, currentLng;
    RoutesUtil routesUtil;
    TextView priceTxt, nameTxt, addrTxt, distTxt, departTimeTxt, arriveTimeTxt, durationTxt;

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
        routesUtil.cleanAllVariables();
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    //    google maps
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {  // google map ready
        this.googleMap = googleMap;
        googleMap.setBuildingsEnabled(true);
        googleMap.setPadding(0,0,0,20);
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
        LatLng sourceLoc = new LatLng(currentLat, currentLng);
        routesUtil.drawDirections(sourceLoc, cafeLoc);
        setInfoTexts();
    }

    // utils
    private void init() {
        // find view
        nameTxt = findViewById(R.id.footerCafeName);
        addrTxt = findViewById(R.id.footerAddr);
        priceTxt = findViewById(R.id.footerTotPrice);
        distTxt = findViewById(R.id.distanceTxt);
        departTimeTxt = findViewById(R.id.startTimeTxt);
        arriveTimeTxt = findViewById(R.id.endTimeTxt);
        durationTxt = findViewById(R.id.durationTxt);

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
                            routesUtil.cleanAllVariables();  // remove marker, route info
                            clearAllTexts();
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

    private void setInfoTexts() {
        Place cafe = places.get(0);
        nameTxt.setText(cafe.getName());
        addrTxt.setText(routesUtil.getDestAddress());
        distTxt.setText(routesUtil.getDistance() + " meter");
        departTimeTxt.setText(routesUtil.getDepartureTime());
        arriveTimeTxt.setText(routesUtil.getArrivalTime());
        durationTxt.setText(routesUtil.getDuration());
    }

    private void clearAllTexts() {
        nameTxt.setText("Cafe Name");
        addrTxt.setText("Cafe Address");
        distTxt.setText("0 meter");
        departTimeTxt.setText("00:00 AM");
        arriveTimeTxt.setText("00:00 AM");
        durationTxt.setText("0분");
    }
}
