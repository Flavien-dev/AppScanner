package com.chapfla.appscanner;

import static java.lang.Double.parseDouble;
import static java.lang.String.valueOf;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocaBenneActivity extends AppCompatActivity implements OnMapReadyCallback {

    // déclaration des objets
    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    FragmentGestionActivity fragmentMessage;
    SharedPreferences permissionStatus;
    private static final int SMS_PERMISSION_CONSTANT = 100;
    private double latitude;
    private double longitude;
    private EditText et_numero;
    private EditText et_message;
    private Button btn_envoyer;
    private String monNumero;
    private String monMessage;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loca_benne);

        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // création du numéro et du message
        monNumero = "0774864961";
        monMessage = "la benne est pleine, il faut venir la vider";

        // Récuperer le fragent message
        fragmentMessage = (FragmentGestionActivity) getSupportFragmentManager().findFragmentById(R.id.fragment_gestion);
        // Récuperer les elements du fragment
        btn_envoyer = fragmentMessage.getView().findViewById(R.id.btn_envoyer_mess_benne);
        et_message = fragmentMessage.getView().findViewById(R.id.message_benne);
        et_numero = fragmentMessage.getView().findViewById(R.id.numero_benne);

        et_numero.setText(monNumero);

        Intent MainActivity = getIntent();

        // récupération de la latitude et de la longitude
        latitude = Double.parseDouble(MainActivity.getStringExtra("latitude"));
        longitude = Double.parseDouble(MainActivity.getStringExtra("longitude"));

        // le text de la description de la benne
        et_message.setText("https://maps.google.com/?ll="+ latitude + "," + longitude + ", " + monMessage);

        /**
         * s'exécute dès que le bouton envoyer est pressé
         */
        btn_envoyer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage(et_numero.getText().toString(),et_message.getText().toString());
            }
        });


        // vérifie si l'envoi des sms est autorisé
        if (ActivityCompat.checkSelfPermission(LocaBenneActivity.this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // si l'application ne peut pas envoyer de sms
            if (ActivityCompat.shouldShowRequestPermissionRationale(LocaBenneActivity.this, Manifest.permission.SEND_SMS)) {
                // affiche la boite de dialogue
                AlertDialog.Builder builder = new AlertDialog.Builder(LocaBenneActivity.this);
                builder.setTitle("Need SMS Permission");
                builder.setMessage("This app needs SMS permission to send Messages.");

                /**
                 * s'exécute si on presse sur le bouton Grant
                 */
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    // dès qu'on presse dessus, la permission d'envoi de sms est autorisé
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(LocaBenneActivity.this,
                                new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CONSTANT);
                    }
                });
                /**
                 * s'exécute si on presse sur le bouton Cancel
                 */
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                // montre le builder
                builder.show();
                // si la permission est à faux, la boite de dialogue s'affiche à nouveau
            } else {
                // le fichier manifest ajoute la permission d'envoi de sms
                ActivityCompat.requestPermissions(LocaBenneActivity.this, new String[]{Manifest.permission.SEND_SMS}
                        , SMS_PERMISSION_CONSTANT);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.SEND_SMS, true);
            editor.commit();

        }
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
                // met à jour la position de
                LatLng userLocation = new LatLng(latitude,longitude);

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

            // met à jour la position de l'utilisateur
            LatLng userLocation = new LatLng(latitude,longitude);

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

    /**
     * envoyer un sms
     * @param strMobileNo numéro du destinataire
     * @param strMessage  message à envoyer au destinataire
     */
    public void SendMessage(String strMobileNo, String strMessage) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(strMobileNo, null, strMessage, null, null);
            Toast.makeText(getApplicationContext(), "Your Message Sent",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
