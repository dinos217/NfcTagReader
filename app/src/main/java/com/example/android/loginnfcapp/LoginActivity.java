package com.example.android.loginnfcapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View.OnClickListener;

import java.text.BreakIterator;

public class LoginActivity extends AppCompatActivity {

    public String username, password;
    public EditText loginInputUsername, loginInputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Context context = this;
        NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        final NfcAdapter nfcAdapter = manager.getDefaultAdapter();

        if (nfcAdapter == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);      //ΑΝ Η ΣΥΣΚΕΥΗ ΔΕΝ ΥΠΟΣΤΗΡΙΖΕΙ NFC
            builder.setMessage("Your device does not support NFC.")                                 //ΕΜΦΑΝΙΖΕΤΑΙ ΑΝΑΛΟΓΟ ΜΗΝΥΜΑ.
                    .setPositiveButton("OK, CLOSE APPLICATION.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            LoginActivity.this.finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public void checkLogIn(View view) {

        loginInputUsername = (EditText)findViewById(R.id.username);
        loginInputPassword = (EditText)findViewById(R.id.password);

        username = loginInputUsername.getText().toString();
        password = loginInputPassword.getText().toString();
        if (password.equals("") || username.equals("")) {
            Toast.makeText(this, "Username and Password fields should not be empty", Toast.LENGTH_SHORT).show();
        }
        if (username.equals("login@gmail.com") && password.equals("12345")) {
            Intent intent = new Intent(this, SuccessfulLoginActivity.class);
            intent.putExtra("username",username);
            startActivity(intent);
        }

    }
}