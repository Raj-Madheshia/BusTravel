package com.example.bustravel;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class FirstPage extends AppCompatActivity {
//    private Button signout;
//    private FirebaseAuth mAuth;

    public static final String ERROR_DETECTED = "No NFC tag detected!";
    public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
    public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";

    private  FirebaseAuth.AuthStateListener mAuthListiner;
    private  FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    SqliteDatabaseHelper sqliteDatabaseHelper;
    private String userId;
    private String amount;
    private String msg = "";
    private String currentBus;
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Tag myTag;
    Context context;

    TextView tvNFCContent;
    TextView message;
    Button btnWrite;
    private Spinner source, destination;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_page);

        final SharedPreferences sharedPref =  getSharedPreferences("Buses", 0);
        final SharedPreferences.Editor editor = sharedPref.edit();
        currentBus = sharedPref.getString("bus_no",null);

        sqliteDatabaseHelper = new SqliteDatabaseHelper(this);


// #################################  Signout method ######################################
//        mAuth = FirebaseAuth.getInstance();
//
//        signout = (Button) findViewById(R.id.singnout);
//        signout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                FirebaseAuth.getInstance().signOut();
//                finish();
//            }
//        });
// #####################################################################################
        context = this;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userId = currentUser.getUid();

        mAuthListiner =  new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()== null){
                    Intent i=new Intent(FirstPage.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }

            }
        };

        tvNFCContent = (TextView) findViewById(R.id.nfc_contents);
        message = (TextView) findViewById(R.id.edit_message);
        btnWrite = (Button) findViewById(R.id.button);
        source = (Spinner) findViewById(R.id.source);
        destination = (Spinner) findViewById(R.id.destination);

        Cursor res = sqliteDatabaseHelper.getValByBusno(currentBus);
        if(res.getCount()!=0) {
            List<SourceDestinationData> data = new ArrayList<>();
            while(res.moveToNext()){
                SourceDestinationData detail = new SourceDestinationData(res.getString(0),res.getString(1),res.getString(2));
                data.add(detail);
            }
            ArrayAdapter<SourceDestinationData> adapter = new ArrayAdapter<SourceDestinationData>(this,
                    android.R.layout.simple_spinner_item,data);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            source.setAdapter(adapter);
            destination.setAdapter(adapter);
        }

        btnWrite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                try {
                    if(myTag ==null) {
                        Toast.makeText(context, ERROR_DETECTED, Toast.LENGTH_LONG).show();
                    } else {

                        // +";"+message.getText().toString()
                        msg = userId+";"+currentBus+";"+amount;
                        write(msg, myTag);
                        Toast.makeText(context, WRITE_SUCCESS, Toast.LENGTH_LONG ).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(context, WRITE_ERROR, Toast.LENGTH_LONG ).show();
                    e.printStackTrace();
                } catch (FormatException e) {
                    Toast.makeText(context, WRITE_ERROR, Toast.LENGTH_LONG ).show();
                    e.printStackTrace();
                }
            }
        });
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };

    }

    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListiner);
    }

    /******************************************************************************
     **********************************Read From NFC Tag***************************
     ******************************************************************************/
    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }
    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        String text = "";
//        String tagId = new String(msgs[0].getRecords()[0].getType());
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }catch (Exception e){
            Toast.makeText(FirstPage.this,"Error while reading",Toast.LENGTH_SHORT).show();
        }


        try{
    // Bug
   // $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   Null Check on Amount not working $$$$$$$$$$$$$$$$$$$$$$$$$$$$

            String eachData[] = text.split(";");
            if(eachData.length <3 || eachData[2] == null){
                amount ="0";
            }

            // +";"+"message" -------------- if needed

            msg = userId+";"+currentBus+";"+amount;
            tvNFCContent.setText(msg);
        }
        catch (Exception e){
            Toast.makeText(FirstPage.this,"Something went wrong",Toast.LENGTH_SHORT).show();
        }


    }

    /******************************************************************************
     **********************************Write to NFC Tag****************************
     ******************************************************************************/
    private void write(String text, Tag tag) throws IOException, FormatException {
        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
    }
    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;
        byte[] payload    = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1,              langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);

        return recordNFC;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        readFromIntent(intent);
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume(){
        super.onResume();
        WriteModeOn();
    }



    /******************************************************************************
     **********************************Enable Write********************************
     ******************************************************************************/
    private void WriteModeOn(){
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }
    /******************************************************************************
     **********************************Disable Write*******************************
     ******************************************************************************/
    private void WriteModeOff(){
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }

}
