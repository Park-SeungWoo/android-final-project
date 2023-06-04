package com.finalexam.utils;

import android.graphics.Color;

import com.finalexam.BuildConfig;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RoutesUtil {
    final List<LatLng> routePoints;
    final GoogleMap googleMap;

    String arrival_time;
    String departure_time;
    String duration;
    String distance;
    String destAddress;
    String departAddress;

    public RoutesUtil(GoogleMap googleMap) {
        this.routePoints = new ArrayList<>();
        this.googleMap = googleMap;
    }

    // google direction
    public Polyline drawDirections(String sourceLocStr, String destLocStr) {
        routePoints.clear();
        new Thread(() -> {  // if we use network api in same thread with android app, it throws NetworkOnMainThreadException.
            try {
                // find data
                JSONObject resultJson = getDirectionResult(sourceLocStr, destLocStr);  // has routes, geocoded_waypoints, status
                JSONObject routeJson = (JSONObject) resultJson.getJSONArray("routes").get(0);  // has bounds, legs, overview_polyline, summary, warnings, waypoint_order
                JSONObject directionInfoJson = (JSONObject) routeJson.getJSONArray("legs").get(0);  // has arrival_time, departure_time, distance, duration, end_address, end_location, start_address, start_location, steps, traffic_speed_entry, via_waypoint

                String points = routeJson.getJSONObject("overview_polyline").getString("points");
                List<LatLng> pointsList = PolyUtil.decode(points);
                routePoints.addAll(pointsList);
                /*
                JSONArray stepsArray = directionInfoJson.getJSONArray("steps");
                int i = 0;
                List<LatLng> everyPoints = new ArrayList<>();
                while (true) {
                    try {
                        JSONObject step = (JSONObject) stepsArray.get(i++);  // step
                        String travelMode = step.getString("travel_mode");
                        String pointsString = step.getJSONObject("polyline").getString("points");
                        List<LatLng> points = PolyUtil.decode(pointsString);
                        everyPoints.addAll(points);
                        if (travelMode.equals("WALKING")) {  // if step by walk

                        } else if (travelMode.equals("TRANSIT")) {  // if step by transportation

                        }
                    } catch (JSONException e) {  // if get all points from every step
                        routePoints.addAll(everyPoints);
                        break;
                    }
                }

                 */

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


        while (routePoints.isEmpty()) {
        }

        // draw
        PolylineOptions polyOptions = new PolylineOptions()
                .addAll(routePoints)
                .width(15f)
                .color(Color.BLUE);
        return googleMap.addPolyline(polyOptions);  // draw new polyline
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


    /*
    google route
     */
    public void drawRoute(LatLng source, LatLng dest) {
        new Thread(() -> {
            try {
                System.out.println("Resultttttttttttttttttttttttttttttttttttttttttttt");
                System.out.println(getRoutesResult(source, dest));
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private JSONObject getRoutesResult(LatLng source, LatLng dest) throws IOException, JSONException {
        // connection setting
        String baseUrlStr = "https://routes.googleapis.com/directions/v2:computeRoutes";
        URL baseUrl = new URL(baseUrlStr);
        HttpURLConnection connection = (HttpURLConnection) baseUrl.openConnection();
        connection.setRequestMethod("POST"); // get method
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("X-Goog-Api-Key", BuildConfig.GOOGLE_API_KEY);
        connection.setRequestProperty("X-Goog-FieldMask", "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline");
        connection.setDoOutput(true);  // send data
        connection.setDoInput(true);  // get data

        // prepare body
        JSONObject body = getJsonBody(source, dest);

        // set body into the connection and send
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(body.toString().getBytes(StandardCharsets.UTF_8));  // data setting
        outputStream.flush();  // send data
        outputStream.close();

        // get result
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;

        while ((line = reader.readLine()) != null) {  // while able to read line
            stringBuilder.append(line);
        }
        JSONObject result = new JSONObject(stringBuilder.toString());
        reader.close();


        return result;
    }

    private JSONObject getJsonBody(LatLng source, LatLng dest) throws JSONException {
        JSONObject body = new JSONObject();
        JSONObject sourceObj = getLocationJson(source);
        JSONObject destObj = getLocationJson(dest);

        body.put("origin", sourceObj)
                .put("destination", destObj)
//                .put("travelMode", "WALK")
                .put("computeAlternativeRoutes", true);

        return body;
    }

    private JSONObject getLocationJson(LatLng location) throws JSONException {
        JSONObject locationJson = new JSONObject();
        locationJson.put("location", new JSONObject()
                .put("latLng", new JSONObject()
                        .put("latitude", location.latitude)
                        .put("longitude", location.longitude)));
        return locationJson;
    }

    // naver route
    public void drawRouteWithNaver(LatLng source, LatLng dest) {

    }
}
