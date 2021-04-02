package com.example.vcfitness;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AlarmReceiver extends BroadcastReceiver {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    int intSteps;


    @Override
    public void onReceive(Context context, Intent intent) {


        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        intSteps = pref.getInt("Steps", 0);
        InsertDailySteps(intSteps);

        final SharedPreferences shared =  context.getSharedPreferences("Steps", context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = shared.edit();

        int zero = 0;
        editor.putInt("Steps", zero);
        editor.apply();

    }


    private void InsertDailySteps(int steps)
    {

        String userid = mAuth.getUid();

        Map<String, Object> user = new HashMap<>();

        Long TimeMilli =  System.currentTimeMillis();




        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(TimeMilli);



        user.put(TimeMilli.toString(), steps);



        // Add a new document with a generated ID
        db.collection("DailySteps").document(userid)
                .set(user, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {

                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        //Log.w(TAG, "Error adding document", e);

                    }
                });



    }

    }

