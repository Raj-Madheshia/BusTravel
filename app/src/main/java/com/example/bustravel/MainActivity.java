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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FireBase Auth
        mAuth = FirebaseAuth.getInstance();


        email = (EditText) findViewById(R.id.editEmail);
        pass = (EditText) findViewById(R.id.editPass);
        login = (Button) findViewById(R.id.login);

        mAuthListiner =  new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!= null){
                    Intent i=new Intent(MainActivity.this, FirstPage.class);
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
