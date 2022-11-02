package com.chapfla.appscanner;

// importation des librairies
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.chapfla.appscanner.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * classe principale de l'application
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // déclaration des variables
    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    private double latitude = 0.0;
    private double longitude = 0.0;
    private boolean isBtnGPS;

    /**
     * permet de dire si on a la permission de se localiser
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                // vérifie qu'on ait bien la permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,100,1,locationListener);
                }
            }
        }
    }

    /**
     * s'exécute lors du lancement de l'application
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent MainActivity = getIntent();

        Log.wtf("latitude", MainActivity.getStringExtra("latitude"));
        Log.wtf("longitude", MainActivity.getStringExtra("longitude"));
        Log.wtf("etat du bouton gps", String.valueOf(MainActivity.getStringExtra("isBtnGPS")));

        latitude = Double.parseDouble(MainActivity.getStringExtra("latitude"));
        longitude = Double.parseDouble(MainActivity.getStringExtra("longitude"));
        isBtnGPS = Boolean.valueOf(MainActivity.getStringExtra("isBtnGPS"));

    }

    /**
     * permet d'afficher des éléments dès que la map est prête
     * @param googleMap carte google map
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // déclaration des objets
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            /**
             * s'exécute lorsque la position du téléphone change
             * @param location position du téléphone
             */
            @Override
            public void onLocationChanged(@NonNull Location location) {

                LatLng userLocation;

                if (isBtnGPS && latitude == 1000 && longitude == 1000) {
                    userLocation = new LatLng(location.getLatitude(),location.getLongitude());
                } else {
                    userLocation = new LatLng(latitude,longitude);
                }
                // affiche le nouveau marqueur à la position donnée
                displayMarker(userLocation);
            }
        };


        // vérifie si l'application à la permission d'accèder à une localisation
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            // modifie la localisation affichée
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,100,1,locationListener);

            // crée une nouvelle position
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            LatLng userLocation;

            if (isBtnGPS && latitude == 1000 && longitude == 1000) {
                userLocation = new LatLng(location.getLatitude(),location.getLongitude());
            } else {
                userLocation = new LatLng(latitude,longitude);
            }

            // affiche le marqueur à la position de l'utilisateur
            displayMarker(userLocation);
        }
    }

    /**
     * affiche le marqueur sur la carte
     * @param userLocation position de l'utilisateur
     */
    public void displayMarker(LatLng userLocation) {
        // nettoie la carte
        mMap.clear();
        // ajoute le marqueur à la position de l'utilisateur
        mMap.addMarker(new MarkerOptions().position(userLocation).title("Ma position"));
        // déplace la caméra à la position du marqueur
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 18), 500, null);
    }
}