package com.finalexam.utils;

import android.util.Log;

import com.finalexam.BuildConfig;
import com.finalexam.activities.MapActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

public interface PlacesUtil extends PlacesListener{
    List<Place> places = new ArrayList<>();

    default void findPlaces(LatLng locations, String type) {
        // must not set application restrict in google api key
        // I don't know why but, if we restrict the key only this android app, we'll be denied to access.
        new NRPlaces.Builder()
                .listener(this)
                .key(BuildConfig.GOOGLE_API_KEY)
                .latlng(locations.latitude, locations.longitude)
                .radius(500)
                .type(type)
                .build()
                .execute();
    }

    @Override
    default void onPlacesFailure(PlacesException e) {
        Log.e(e.getMessage(), "Fail to find places");
    }

    @Override
    default void onPlacesStart() {
        System.out.println("Start finding places");
        onStarted();
    }

    @Override
    default void onPlacesSuccess(List<Place> places) {  // paging call. It means before finish handler call, this method could call several times.
        System.out.println("Success! finding but not done til finished");
        this.places.addAll(places);
    }

    @Override
    default void onPlacesFinished() {
        System.out.println("Finished! find all near places");
        onFinished();
    }

    void onStarted();
    void onFinished();
}
