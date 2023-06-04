package com.finalexam.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.finalexam.BuildConfig;
import com.finalexam.R;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, PlacesListener {
    GoogleMap googleMap;
    FusedLocationProviderClient locationProvider;
    List<Place> cafes;
    Marker cafeMarker;
    double currentLat, currentLng;
    List<LatLng> routePoints;  // to draw route
    TextView priceTxt, nameTxt, addrTxt;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

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

//        initLocationAndGetCafe();
        getCurrentLocation();
    }

    //    places api
    @Override
    public void onPlacesFailure(PlacesException e) {
        System.out.println("Fail !!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    @Override
    public void onPlacesStart() {
        System.out.println("Start~~~~~~~~~~~~~~");
    }

    @Override
    public void onPlacesSuccess(List<Place> places) {  // paging 되어서 여러번 호출 되고 마지막에 finish 한번 호출됨
        System.out.println("Found!!!!!!!!!!!!!!!!!!!!!!" + places.size());
        cafes.addAll(places);
    }

    @Override
    public void onPlacesFinished() {
        // indicate route
        System.out.println("Finish~~~~~~~~~~~~~~~~~~~~");
        Place cafe = cafes.get(0);
        LatLng latLng = new LatLng(cafe.getLatitude(), cafe.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(cafe.getName());
        markerOptions.snippet(cafe.getVicinity());
        cafeMarker = googleMap.addMarker(markerOptions);
        drawDirections(currentLat + "," + currentLng, cafe.getLatitude() + "," + cafe.getLongitude());
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

        routePoints = new ArrayList<>();
    }

    private void getCurrentLocation() {
        locationProvider = new FusedLocationProviderClient(this);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000 * 5);
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        if (location.getLatitude() != currentLat || location.getLongitude() != currentLng) {
                            if (cafeMarker != null)
                                cafeMarker.remove();  // remove before cafe marker
                            cafes = new ArrayList<Place>();  // clear cafe lists
                            currentLat = location.getLatitude();
                            currentLng = location.getLongitude();

                            LatLng currentLoc = new LatLng(currentLat, currentLng);

                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15));

                            findNearCafe(currentLoc);
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

    private void findNearCafe(LatLng locations) {
        // must not set application restrict in google api key
        // I don't know why but, if we restrict the key only this android app, we'll be denied to access.
        new NRPlaces.Builder()
                .listener(MapActivity.this)
                .key(BuildConfig.GOOGLE_API_KEY)
                .latlng(locations.latitude, locations.longitude)
                .radius(500)
                .type(PlaceType.CAFE)
                .build()
                .execute();
    }

    private void drawDirections(String sourceLocStr, String destLocStr) {
        new Thread(() -> {  // if we use network api in same thread with android app, it throws NetworkOnMainThreadException.
            try {
                String arrival_time;
                String departure_time;
                String duration;
                String distance;
                String destAddress;
                String departAddress;

                // find data
                JSONObject resultJson = getDirectionResult(sourceLocStr, destLocStr);  // has routes, geocoded_waypoints, status
                JSONObject routeJson = (JSONObject) resultJson.getJSONArray("routes").get(0);  // has bounds, legs, overview_polyline, summary, warnings, waypoint_order
                JSONObject directionInfoJson = (JSONObject) routeJson.getJSONArray("legs").get(0);  // has arrival_time, departure_time, distance, duration, end_address, end_location, start_address, start_location, steps, traffic_speed_entry, via_waypoint

                String points = routeJson.getJSONObject("overview_polyline").getString("points");
                List<LatLng> pointsList = PolyUtil.decode(points);
                routePoints.addAll(pointsList);
//                JSONArray stepsArray = directionInfoJson.getJSONArray("steps");
//                int i = 0;
//                List<LatLng> everyPoints = new ArrayList<>();
//                while (true) {
//                    try {
//                        JSONObject step = (JSONObject) stepsArray.get(i++);  // step
//                        String travelMode = step.getString("travel_mode");
//                        String pointsString = step.getJSONObject("polyline").getString("points");
//                        List<LatLng> points = PolyUtil.decode(pointsString);
//                        everyPoints.addAll(points);
//                        if (travelMode.equals("WALKING")) {  // if step by walk
//
//                        } else if (travelMode.equals("TRANSIT")) {  // if step by transportation
//
//                        }
//                    } catch (JSONException e) {  // if get all points from every step
//                        routePoints.addAll(everyPoints);
//                        break;
//                    }
//                }

                // extract data
                arrival_time = directionInfoJson.getJSONObject("arrival_time").getString("text");
                departure_time = directionInfoJson.getJSONObject("departure_time").getString("text");
                duration = directionInfoJson.getJSONObject("duration").getString("text");
                distance = directionInfoJson.getJSONObject("distance").getString("text");
                destAddress = directionInfoJson.getString("end_address");
                departAddress = directionInfoJson.getString("start_address");


            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }).start();


        while(routePoints.isEmpty()){
            System.out.println("waiting new thread");
        }
        // draw
        PolylineOptions polyOptions = new PolylineOptions()
                .addAll(routePoints)
                .width(8f)
                .color(Color.RED);
        googleMap.addPolyline(polyOptions);
    }

    private JSONObject getDirectionResult(String sourceLocStr, String destLocStr) throws IOException, JSONException {
//        https://routes.googleapis.com/directions/v2:computeRoutes?key=YOUR_API_KEY

        String baseUrlStr = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + sourceLocStr + "&" +
                "destination=" + destLocStr + "&" +
                "mode=" + "transit" + "&" +
                "alternative=" + "true" + "&" +
                "avoid=" + "tolls|highways|ferries" + "&" +
                "departure_time" + new Date() + "&" +
                "key=" + BuildConfig.GOOGLE_API_KEY;
        URL baseUrl = new URL(baseUrlStr);
        HttpURLConnection connection = (HttpURLConnection) baseUrl.openConnection();
        connection.setRequestMethod("GET"); // get method
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);  // to get result

        // get result
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;

        while ((line = reader.readLine()) != null) {  // while can read line
            stringBuilder.append(line);
        }

        JSONObject result = new JSONObject(stringBuilder.toString());
        System.out.println(result);

        reader.close();
        return result;
    }
}
