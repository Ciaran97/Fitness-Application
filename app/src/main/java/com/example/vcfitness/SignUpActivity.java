package com.example.vcfitness;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    EditText etPassword2;
    EditText etPassword;
    EditText etEmail;
    EditText etFName;
    EditText etSurname;
    EditText etCell;

    private FirebaseAuth mAuth;

    private static final String TAG = "SignUpActivity";
   private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPassword2 = findViewById(R.id.etPassword2);
        etFName = findViewById(R.id.etFName);
        etSurname = findViewById(R.id.etSurname);
        etCell = findViewById(R.id.etCell);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();




    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
        {
            FirebaseAuth.getInstance().signOut();
        }

    }

    public void insertUserAuth()
    {

       final String Email = etEmail.getText().toString();
       final String password = etPassword.getText().toString();
        String password2 = etPassword2.getText().toString();


        if(Email == "" || password == "" || password2 == "")
        {
           Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show();
        }
            else  if(password.equals(password2))
            {
                mAuth.createUserWithEmailAndPassword(Email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                                    // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignUpActivity.this, "User added.", Toast.LENGTH_SHORT).show();

                            insertUserDetails(user.getUid());
                           signIn(Email, password);

                        } else
                            {
                                    // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                    }
                });
            }else
                {
                Toast.makeText(this, "Passwords Do Not Match", Toast.LENGTH_LONG).show();
                }

    }

    public void Adding_User(View view) {
        insertUserAuth();

    }


    public void insertUserDetails(String userid)
    {

       String FirstName = etFName.getText().toString();
        String Surname = etSurname.getText().toString().trim();
        String cell = etCell.getText().toString().trim();
        String Userid;
       Userid = mAuth.getUid();

        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", FirstName);
        user.put("surname", Surname);
        user.put("cell", cell);
        user.put("step_goal", null);
        user.put("gender", null);
        user.put("weight", null);
        user.put("height", null);
        user.put("weight_goal", null);
        user.put("birthday", null);

// Add a new document with a generated ID
        db.collection("users").document(Userid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(SignUpActivity.this, "DB Insert success", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        Toast.makeText(SignUpActivity.this, "DB Insert failed: " + e, Toast.LENGTH_LONG).show();
                    }
                });

    }

    public void signIn(String email, String password){

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(SignUpActivity.this, "Authentication successful.", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();


                            Intent intents = new Intent(SignUpActivity.this, Dashboard.class);

                            startActivity(intents);

                        } else
                        {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());

                        }


                    }
                });


    }

}
