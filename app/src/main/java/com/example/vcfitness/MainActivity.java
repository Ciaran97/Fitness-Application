package com.example.vcfitness;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity{

    private FirebaseAuth mAuth;
    EditText etUsername, etPassword;

private static final String TAG = "MainActivity";

    SQLiteDatabase db;
    public static final String Col_1 = "RowID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();






        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);



    }

    public void OpenSignUpPage(View view) {
        startActivity(new Intent(MainActivity.this, SignUpActivity.class));
    }






    public void LoginClick(View view) {

        String password = etPassword.getText().toString();
        String email = etUsername.getText().toString();

        if (password.isEmpty() && email.isEmpty()) {
            Toast.makeText(this, "Please enter valid credentials", Toast.LENGTH_SHORT).show();

        }
        else{
            //signIn();
            //logs user in using given email and password
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                Toast.makeText(MainActivity.this, "Authentication successful.", Toast.LENGTH_SHORT).show();
                                FirebaseUser user = mAuth.getCurrentUser();


                                Intent intents = new Intent(MainActivity.this, Dashboard.class);

                                startActivity(intents);

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                            }

                            // ...
                        }
                    });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
        if(currentUser != null){
            Intent intents = new Intent(MainActivity.this, Dashboard.class);
            startActivity(intents);
        }
        else{

        }
    }
}
