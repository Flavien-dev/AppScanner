package com.chapfla.appscanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

// https://parallelcodes.com/android-send-sms-programmatically-with-permissions/

public class MessActivity extends AppCompatActivity {

    // déclaration des objets et variables
    EditText edtMobileNo, edtMessage;
    Button btn_envoyer;
    SharedPreferences permissionStatus;
    private boolean sentToSettings = false;
    private static final int SMS_PERMISSION_CONSTANT = 100;

    /**
     * s'exécute au lancement de l'application
     * @param savedInstanceState
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messagerie);

        // initialise les objets
        edtMessage = (EditText) findViewById(R.id.tf_message);
        edtMobileNo = (EditText) findViewById(R.id.tf_num_tel);
        btn_envoyer = (Button) findViewById(R.id.btn_envoyer);

        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);

        /**
         * s'exécute si on envoie un sms
         */
        btn_envoyer.setOnClickListener(new View.OnClickListener() {
            /**
             * s'exécute si on presse sur le bouton envoyer
             * @param view
             */
            @Override
            public void onClick(View view) {
                // fonction qui envoie un sms
                SendMessage(edtMobileNo.getText().toString(), edtMessage.getText().toString());
            }
        });

        // vérifie si l'envoi des sms est autorisé
        if (ActivityCompat.checkSelfPermission(MessActivity.this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // si l'application ne peut pas envoyer de sms
            if (ActivityCompat.shouldShowRequestPermissionRationale(MessActivity.this, Manifest.permission.SEND_SMS)) {
                // affiche la boite de dialogue
                AlertDialog.Builder builder = new AlertDialog.Builder(MessActivity.this);
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
                        ActivityCompat.requestPermissions(MessActivity.this,
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
                ActivityCompat.requestPermissions(MessActivity.this, new String[]{Manifest.permission.SEND_SMS}
                        , SMS_PERMISSION_CONSTANT);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.SEND_SMS, true);
            editor.commit();

        }
    }

    /**
     * envoie un sms
     * @param strMobileNo numéro du destinataire
     * @param strMessage message à envoyer au destinataire
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