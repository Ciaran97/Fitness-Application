package com.example.vcfitness;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Dashboard extends AppCompatActivity implements SensorEventListener {

    ImageView imageview;
    FirebaseAuth mAuth;
    int newWeight;
    public int DaysSteps;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView Fullname, cardWeight, steps;
    int StepGoal;

    //For Stepcounter
    SensorManager sensorManager;
    boolean running = false;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        imageview = findViewById(R.id.imageviewD);
        Fullname = findViewById(R.id.lblFullName);
        cardWeight = findViewById(R.id.CardWeight);
        steps = findViewById(R.id.txtSteps);

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        DaysSteps = pref.getInt("Steps", 0);
        LoadProfileInfo();

        //For Step Counter
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Set the alarm to start at approximately 2:00 p.m.
        set_Alarm();
    }



    public void btnProfile(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);

    }

    public void Logout(View view) {

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Dashboard.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);


    }

    private void loadProfilePicture() throws IOException {

        String pictureUrl = mAuth.getCurrentUser().getPhotoUrl().toString();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReferenceFromUrl(pictureUrl);
        final File localFile = File.createTempFile("images", "jpg");



        storageRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Successfully downloaded data to local file
                        // ...
                        Bitmap pp = BitmapFactory.decodeFile(String.valueOf(localFile));

                        imageview.setImageBitmap(pp);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                // ...
            }
        });
    }

    public void AddDailyWeight(View view)
    {



        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input Today's Weight");

        // Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                 newWeight = Integer.parseInt(input.getText().toString());

                String userid = mAuth.getUid();

                Map<String, Object> user = new HashMap<>();


                user.put("weight", newWeight);



                // Add a new document with a generated ID
                db.collection("users").document(userid)
                        .update(user)
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
                                Toast.makeText(Dashboard.this, "DB Insert failed: " + e, Toast.LENGTH_LONG).show();
                            }
                        });

                user.clear();




            Long TimeMilli =  System.currentTimeMillis();
           

                user.put(TimeMilli.toString(), newWeight);


                DocumentReference docRef = db.collection("DailyWeight/").document(userid);

                docRef.set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        Toast.makeText(Dashboard.this, "DB Insert success", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(Dashboard.this, "DB Insert failed: " + e, Toast.LENGTH_LONG).show();
                    }
                });


            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();




    }

    private void LoadProfileInfo()
    {
        FirebaseUser user = mAuth.getCurrentUser();

        String strUid = user.getUid();




        DocumentReference docRef = db.collection("users").document(strUid);

        docRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists())
                            {
                                String fullname = document.get("firstName").toString() + " " + document.get("surname").toString();

                                Fullname.setText(fullname);


                                if(document.get("weight") != null)
                                {
                                   // txtWeight.setText(document.get("weight").toString());
                                    cardWeight.setText(document.get("weight").toString());
                                }

                                if(document.get("step_goal") != null)
                                {

                                    steps.setText(DaysSteps + "/" + Integer.parseInt(document.get("step_goal").toString()));
                                    StepGoal = Integer.parseInt(document.get("step_goal").toString());
                                }


                            } else
                                {
                                //Log.d(TAG, "No such document");
                                }
                        } else
                            {
                           Toast.makeText(Dashboard.this, "Error loading Info", Toast.LENGTH_LONG).show();
                            }
                    }
                });


    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(running) {
         DaysSteps++;
         steps.setText(DaysSteps + "/" + StepGoal);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("Steps",DaysSteps);
            editor.apply();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        running = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        if(countSensor != null){
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        }else{
            Toast.makeText(this, "Sensor not Found!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        running = false;

        //sensor stops counting when unregistered
        //sensorManager.unregisterListener(this);
    }

    private void InsertStepsDB(){
        String userid = mAuth.getUid();

        Map<String, Object> user = new HashMap<>();

        Long TimeMilli =  System.currentTimeMillis();
        String strFormat = "dd/MM/yyyy";

        SimpleDateFormat formater = new SimpleDateFormat(strFormat);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(TimeMilli);
        String strDate = formater.format(calendar.getTime());


        user.put(strDate, DaysSteps);



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
                        Toast.makeText(Dashboard.this, "DB Insert failed: " + e, Toast.LENGTH_LONG).show();
                    }
                });

        DaysSteps = 0;

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==2)
        {
            InsertStepsDB();        }
    }

    public void btnGallery(View view) {

        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
    }

    public void View_Goals(View view) {
        Intent intent = new Intent(this, GoalsActivity.class);
        startActivity(intent);
    }

    private void set_Alarm(){

            Intent receiverIntent = new Intent(this, AlarmReceiver.class);
            PendingIntent sender = PendingIntent.getBroadcast(this, 123456789, receiverIntent, 0);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);

            AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, sender);

    }
}

