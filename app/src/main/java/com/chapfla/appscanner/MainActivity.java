package com.chapfla.appscanner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {

    // déclaration des objets
    private Button btn_scanner;
    private Button btn_message;
    private Button btn_gps;
    private Button btn_scan_localisation;
    private Button btn_locaBenne;

    public boolean isBtnGPS;
    public boolean isBtnGPSScanBenne;

    /**
     * s'exécute lors de la création de l'application
     * @param savedInstanceState
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialiser le bouton
        btn_scanner = (Button) findViewById(R.id.btn_scanner);
        btn_message = (Button) findViewById(R.id.btn_messagerie);
        btn_gps = (Button) findViewById(R.id.btn_gps);
        btn_scan_localisation = (Button) findViewById(R.id.btn_scanner_gps);
        btn_locaBenne = (Button) findViewById(R.id.btn_scanner_benne);

        /**
         * s'exécute quand le bouton messagerie est pressé
         */
        btn_message.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this,MessActivity.class));
        });

        /**
         * s'exécute quand le bouton gps est pressé
         */
        btn_gps.setOnClickListener(view -> {
            // initialisation des variables
            isBtnGPS = true;
            isBtnGPSScanBenne = false;
            String latitude = "1000";
            String longitude = "1000";

            // crée l'intent qui va recevoir les données
            Intent MapsActivity = new Intent(MainActivity.this, MapsActivity.class);

            // envoi des données que l'intent va recevoir
            MapsActivity.putExtra("latitude", latitude);
            MapsActivity.putExtra("longitude", longitude);
            MapsActivity.putExtra("isBtnGPS", String.valueOf(isBtnGPS));

            // lancement de l'activité
            startActivity(MapsActivity);
        });

        /**
         * s'exécute quand le bouton scanner localisation est pressé
         */
        btn_scan_localisation.setOnClickListener(view -> {
            isBtnGPS = false;
            isBtnGPSScanBenne = false;
            // scannage du code qr contenant les données de position
            scanCodeLocation();
        });

        /**
         * s'exécute quand le bouton scanner est pressé
         */
        btn_scanner.setOnClickListener(v-> {
            scanCode();
        });

        /**
         * s'exécute dès que le bouton locaBenne est pressé
         */
        btn_locaBenne.setOnClickListener(view -> {
            isBtnGPS = false;
            isBtnGPSScanBenne = true;
            scanCodeLocation();
        });
    }

    /**
     * scanne le qr code contenant des données de position
     */
    private void scanCodeLocation() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volunm up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        if (isBtnGPS && !isBtnGPSScanBenne) {
            barLaucherLocation.launch(options);
        } else {
            barLaucherLocationBenne.launch(options);
        }

    }

    /**
     * scanne le code barre ou qr
     */
    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volunm up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLaucher.launch(options);
    }

    /**
     * affiche un boite de dialogue
     */
    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result ->
    {
        // si il y a un résultat, la boite de dialogue s'affiche
        if (result.getContents() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                /**
                 * si on presse sur "ok", la boite se ferme
                 * @param dialogInterface
                 * @param i
                 */
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String latitude = "-1000";
                    String longitude = "-1000";

                    // crée l'intent qui va permettre d'envoyer les données sur une nouvelle activité
                    Intent MapsActivity = new Intent(MainActivity.this, MapsActivity.class);

                    // envoi des données à l'activité destinataire
                    MapsActivity.putExtra("latitude", latitude);
                    MapsActivity.putExtra("longitude", longitude);
                    MapsActivity.putExtra("isBtnGPS", String.valueOf(isBtnGPS));
                    MapsActivity.putExtra("isBtnGPS", String.valueOf(isBtnGPSScanBenne));

                    dialogInterface.dismiss();
                }
            }).show();
        }
    });


    ActivityResultLauncher<ScanOptions> barLaucherLocation = registerForActivityResult(new ScanContract(), result ->
    {
        // si il y a un résultat, la boite de dialogue s'affiche
        if (result.getContents() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Result");
            String strLatLong = result.getContents();
            builder.setMessage(strLatLong);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                /**
                 * si on presse sur "ok", la boite se ferme
                 * @param dialogInterface
                 * @param i
                 */
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // récupération des coordonnées
                    String[] latlong = strLatLong.split(",");
                    String latitude = latlong[0];
                    String longitude = latlong[1];

                    // crée l'intent qui va permettre d'envoyer les données sur une nouvelle activité
                    Intent MapsActivity = new Intent(MainActivity.this, MapsActivity.class);

                    // envoi des données à l'activité destinataire
                    MapsActivity.putExtra("latitude", latitude);
                    MapsActivity.putExtra("longitude", longitude);
                    MapsActivity.putExtra("isBtnGPS", String.valueOf(isBtnGPS));
                    MapsActivity.putExtra("isBtnGPS", String.valueOf(isBtnGPSScanBenne));

                    // lancer l'activité MapsActivity
                    startActivity(MapsActivity);

                    dialogInterface.dismiss();
                }
            }).show();
        }
    });

    ActivityResultLauncher<ScanOptions> barLaucherLocationBenne = registerForActivityResult(new ScanContract(), result ->
    {
        // si il y a un résultat, la boite de dialogue s'affiche
        if (result.getContents() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Result");
            String strLatLong = result.getContents();
            builder.setMessage(strLatLong);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                /**
                 * si on presse sur "ok", la boite se ferme
                 * @param dialogInterface
                 * @param i
                 */
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // récupération des coordonnées
                    String[] latlong = strLatLong.split(",");
                    String latitude = latlong[0];
                    String longitude = latlong[1];

                    Log.wtf("latitude",latitude);

                    // crée l'intent qui va permettre d'envoyer les données sur une nouvelle activité
                    Intent LocaBenneActivity = new Intent(MainActivity.this, LocaBenneActivity.class);

                    // envoi des données à l'activité destinataire
                    LocaBenneActivity.putExtra("latitude", latitude);
                    LocaBenneActivity.putExtra("longitude", longitude);
                    LocaBenneActivity.putExtra("isBtnGPS", String.valueOf(isBtnGPS));
                    LocaBenneActivity.putExtra("isBtnGPS", String.valueOf(isBtnGPSScanBenne));

                    // lancer l'activité MapsActivity
                    startActivity(LocaBenneActivity);

                    dialogInterface.dismiss();
                }
            }).show();
        }
    });
}