package com.example.bustravel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText email;
    private EditText pass;
    private Button login;
    private  FirebaseAuth mAuth;
    private  FirebaseAuth.AuthStateListener mAuthListiner;
    SqliteDatabaseHelper sqliteDatabaseHelper;
    String place;
    String dist;
    String busno;
    String buses[][] = {
            {"Tambo", "0", "360"},
            {"DHOBI GHAT","0.6", "360"},
            {"CHEETA CAMP","1.5","360"},
            {"MANDALA","2.5", "360"},
            {"BARC GATE 6", "3.7","360"},
            {"TELECOM FACT", "4.5", "360"},
            {"PUNJAB WADI", "5.2","360"},
            {"DECONAR", "5.9", "360"},
            {"SAMRAT NAGAR","6.5","360"},
            {"SUBHASNAGAR","7","360"},
            {"CHEMBUR BRIDGE","8","360"},
            {"NEHRU NAGAR","8.7","360"},
            {"KURLA STATION","10.1","360"},
            {"MAHUL GAON","0","361"},
            {"MAHUL MARKET","0.9","361"},
            {"WADALA ROAD","1.6","361"},
            {"MAZGAON DOCK","2.3","361"},
            {"HP NAGAR","3.2","361"},
            {"SHANKAR MANDIR","4.0","361"},
            {"VASHINAKA","5.2","361"},
            {"RCF POLICE", "5.7","361"},
            {"CHEMBUR COLONY","6.4","361"},
            {"NAVJEEVAN SOCIETY","7.2","361"},
            {"BASANT PARK","7.9","361"},
            {"UMARSHI CHOWK","8.7","361"},
            {"NEHRU NAGAR","9.5","361"},
            {"KURLA STATION","10.5","361"},
            {"test","est","38"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sqliteDatabaseHelper = new SqliteDatabaseHelper(this);
        //FireBase Auth
        mAuth = FirebaseAuth.getInstance();
        long datalenght = sqliteDatabaseHelper.getProfilesCount();
        if(datalenght == 0){
            for(String[] data: buses){
                place = data[0];
                dist = data[1];
                busno = data[2];
                sqliteDatabaseHelper.insertDate(place,dist, busno);
            }
        }

        email = (EditText) findViewById(R.id.editEmail);
        pass = (EditText) findViewById(R.id.editPass);
        login = (Button) findViewById(R.id.login);

        mAuthListiner =  new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!= null){
                    Intent i=new Intent(MainActivity.this, OptionActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }


            }
        };

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSignal();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListiner);
    }

    public void startSignal(){
        String e = email.getText().toString();
        String p = pass.getText().toString();

        // check for email and pass
        if(TextUtils.isEmpty(e) || TextUtils.isEmpty(p)){
            Toast.makeText(MainActivity.this,"Email id and Password field required", Toast.LENGTH_SHORT).show();
        }
        else{

            // ******************** Actual Authentication Part  *************************

            mAuth.signInWithEmailAndPassword(e,p).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(!task.isSuccessful()){
                        Toast.makeText(MainActivity.this,"Sign In Error!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }


}
