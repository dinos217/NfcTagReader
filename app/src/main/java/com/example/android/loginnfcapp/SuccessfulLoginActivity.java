package com.example.android.loginnfcapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class SuccessfulLoginActivity extends Activity {

    Context context = this;
    EditText editText;
    String userPass;

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    private android.util.Log Log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_successful_login);

        NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        final NfcAdapter nfcAdapter = manager.getDefaultAdapter();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("username");
            String successfullLogIn = getString(R.string.successfullLogIn);
            TextView successMessageTextView = (TextView) findViewById(R.id.loginMessage_id);
            successMessageTextView.setText(successfullLogIn);
            TextView username = (TextView) findViewById(R.id.username);
            username.setText(value + " !");
        }


        if (!nfcAdapter.isEnabled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SuccessfulLoginActivity.this);
            builder.setMessage("NFC is not available. Do you want to enable NFC?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivityForResult(new Intent(Settings.ACTION_NFC_SETTINGS), 0);
                        }
                    }).setNegativeButton("Cancel", null);
            AlertDialog alert = builder.create();
            alert.show();
        }
        handleIntent(getIntent());
    }

    public void sygrisi(String userPassword, String result) {           //ΑΥΤΗ Η ΜΕΘΟΔΟΣ ΕΛΕΓΧΕΙ ΑΝ ΕΙΝΑΙ ΕΝΕΡΓΟΠΟΙΗΜΕΝΟ ΤΟ NFC
                                                                        //KAI ΕΠΕΙΤΑ ΣΥΓΚΡΙΝΕΙ ΤΙΣ ΤΙΜΕΣ ΤΟΥ NFC KAI ΤΟΥ ΧΡΗΣΤΗ.
        editText = (EditText) findViewById(R.id.user_pass);             //ΚΑΛΕΙΤΑΙ ΣΤΗΝ OnPostExecute ΤΗΣ AsyncTask
        userPass = editText.getText().toString();
        ImageView imgView = (ImageView) findViewById(R.id.imgView);


        NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        final NfcAdapter nfcAdapter = manager.getDefaultAdapter();

        AsyncTask myTask = new NdefReaderTask(this);

        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            if (userPass.equalsIgnoreCase(result)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SuccessfulLoginActivity.this);
                builder.setMessage("Door unlocked!")
                        .setPositiveButton("OK", null);
                AlertDialog alert = builder.create();
                alert.show();
                imgView.setImageResource(R.drawable.enter_door);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(SuccessfulLoginActivity.this);
                builder.setMessage("The password '" + userPass + "' does not match with the NFC tag. Please try again.")
                        .setPositiveButton("OK", null);
                AlertDialog alert = builder.create();
                alert.show();
                imgView.setImageResource(R.drawable.closed_door);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        final NfcAdapter nfcAdapter = manager.getDefaultAdapter();
        setupForegroundDispatch(this, nfcAdapter);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        final NfcAdapter nfcAdapter = manager.getDefaultAdapter();
        stopForegroundDispatch(this, nfcAdapter);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called.
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask(this).execute(tag);

            } else {
                Log.d("imegamatos", "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask(this).execute(tag);
                    break;
                }
            }
        }
    }

    ////// EDW KSEKINAEI H AsyncTask /////////

    public static class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @SuppressLint("StaticFieldLeak")
        public SuccessfulLoginActivity activity;
        private android.util.Log Log;

        public NdefReaderTask(SuccessfulLoginActivity a)
        {
            this.activity = a;
        }

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
            /*
             * See NFC forum specification for "Text Record Type Definition" at 3.2.1
             *
             * http://www.nfc-forum.org/specs/
             *
             * bit_7 defines encoding
             * bit_6 reserved for future use, must be 0
             * bit_5..0 length of IANA language code
             */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }


        @Override
        protected void onPostExecute(String result) {

            String userInput = activity.userPass;
            activity.sygrisi(userInput, result);  //ΕΔΩ ΚΑΛΕΙΤΑΙ Η ΜΕΘΟΔΟΣ ΤΗΣ MAIN ACTIVITY ΠΟΥ ΣΥΓΚΡΙΝΕΙ ΤΙΣ 2 ΤΙΜΕΣ
        }
    }
//
//    /**
//     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
//     * @param adapter
//     * The {@link NfcAdapter} used for the foreground dispatch.
//     */
    public void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }
        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }
}