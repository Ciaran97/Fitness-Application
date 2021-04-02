package com.example.vcfitness;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DailyWeightActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_weight);
    }


    public void LoadWeights()
    {



        FirebaseUser user = mAuth.getCurrentUser();

        String strUid = user.getUid();

        DocumentReference docRef = db.collection("DailyWeight/" + strUid).document("weight");



        docRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists())
                            {
                               // document.
                               // String fullname = document.get("firstName").toString() + " " + document.get("surname").toString();

                               // Fullname.setText(fullname);
Toast.makeText(DailyWeightActivity.this, "Document Found!", Toast.LENGTH_LONG).show();

                                if(document.get("weight") != null)
                                {
                                    // txtWeight.setText(document.get("weight").toString());
                                   // cardWeight.setText(document.get("weight").toString());
                                    Toast.makeText(DailyWeightActivity.this, "Document NOT Found!", Toast.LENGTH_LONG).show();
                                }



                            } else
                            {
                                //Log.d(TAG, "No such document");
                            }
                        } else
                        {
                            Toast.makeText(DailyWeightActivity.this, "Error loading Info", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }
}
