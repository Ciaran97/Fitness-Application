package com.example.vcfitness;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

//Firebase declarations
    private StorageReference mStorageRef;
    StorageReference profileRef;
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    //intent requests
    private static final int IMAGE_GALLERY_RESULT = 10;
    private static final int SELECT_IMAGE = 1;
    private static final int REQUEST_CAMERA= 2;


    //public fields
    Uri uripp;
    String profileimageUrl;
    final Calendar myCalender = Calendar.getInstance();


    //objects from UI
    ImageView imageview;
    Spinner spinGender;
    EditText Datetxt;
    TextView lblWeight;
    EditText txtWeight;
    EditText txtHeight;
    EditText txtWeightGoal;
    EditText txtStepGoal;
    TextView txtfullname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Initilisation of UI components
        imageview = findViewById(R.id.imgProfilePic);
        txtfullname = findViewById(R.id.txtName);
        txtWeight = findViewById(R.id.txtWeight);
        txtHeight = findViewById(R.id.txtHeight);
        txtWeightGoal = findViewById(R.id.txtWeightGoal);
        txtStepGoal = findViewById(R.id.txtStepGoal);
        lblWeight = findViewById(R.id.weightCard);
        spinGender = findViewById(R.id.spinGender);


        //Firebase
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        //calling method to load user info from firestore (collection user) and display to user
        LoadProfileInfo();


        //calender datepicker code
        Datetxt =  findViewById(R.id.txtDate);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int MonthOfYear, int dayOfMonth) {
                myCalender.set(Calendar.YEAR, year);
                myCalender.set(Calendar.MONTH, MonthOfYear);
                myCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        Datetxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(ProfileActivity.this, date, myCalender.get(Calendar.YEAR), myCalender.get(Calendar.MONTH), myCalender.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        LoadSpinners();

    }

    public void Change_pp(View view)
    {
        selectImage();

    }

  private void loadProfilePicture() throws IOException {

       Uri pictureUrl = mAuth.getCurrentUser().getPhotoUrl();
       FirebaseStorage storage = FirebaseStorage.getInstance();

     StorageReference storageRef = storage.getReferenceFromUrl(pictureUrl.toString());
     final File localFile = File.createTempFile("images", "jpg");


//Glide.with(this).load(pictureUrl).into(imageview) ;
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

  //select image from user gallery or snap a picture using the camera
    private void selectImage() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                //use camera to take a picture
                if (options[item].equals("Take Photo"))
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, REQUEST_CAMERA);
                }
                //choose a picture from the gallery
                else if (options[item].equals("Choose from Gallery"))
                {
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, SELECT_IMAGE);
                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {

        //add the image to an imageview to see the picture before uploading
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SELECT_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){

           uripp = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uripp);
                imageview.setImageBitmap(bitmap);

                UploadImageToFireBaseStorage();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK && data != null && data.getData() != null){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

        }

    }

    private void UploadImageToFireBaseStorage() {



        if(uripp != null)
        {
            //add the image to firebase storage and set the url to the users display picture when 'SaveUserInfo() is called
             profileRef = mStorageRef.child("ProfilePictures/" + mAuth.getUid() + ".jpg");

            profileRef.putFile(uripp).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    profileimageUrl = profileRef.getDownloadUrl().toString();
                    saveUserInfo();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void saveUserInfo() {

        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null && profileimageUrl != null){
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(profileimageUrl)).build();


            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ProfileActivity.this, "Profile Picture Updated", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

    }


    public void Choose_Date(View view) {


    }

    private void updateLabel() {
        String myFormat = "dd/MM/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        Datetxt.setText(sdf.format(myCalender.getTime()));
    }

    //load all spinners (dropdown selection)
    private void LoadSpinners(){

        //Gender spinner

        List<String> list = new ArrayList<String>();
        list.add("Male");
        list.add("Female");
        list.add("Other");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinGender.setAdapter(dataAdapter);






    }


    //update users profile in firestore with any updated information
    public void update_Profile(View view) {

        int weightgoal = Integer.parseInt(txtWeightGoal.getText().toString());
        int weight = Integer.parseInt(txtWeight.getText().toString());
        int height = Integer.parseInt(txtHeight.getText().toString());
        int stepgoal = Integer.parseInt(txtStepGoal.getText().toString());
        String birthday = Datetxt.getText().toString();
        String gender = spinGender.getSelectedItem().toString();

        String userid = mAuth.getUid();

        Map<String, Object> user = new HashMap<>();

        user.put("step_goal", stepgoal);
        user.put("gender", gender);
        user.put("weight", weight);
        user.put("height", height);
        user.put("weight_goal", weightgoal);
        user.put("birthday", birthday);

// Add a new document with a generated ID
        db.collection("users").document(userid)
                .update(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(ProfileActivity.this, "DB Insert success", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error adding document", e);
                        Toast.makeText(ProfileActivity.this, "DB Insert failed: " + e, Toast.LENGTH_LONG).show();
                    }
                });


    }


    //load users profile information from firestore into a local object
    private void LoadProfileInfo()
    {

        //get current firebase user
        FirebaseUser user = mAuth.getCurrentUser();

        //get user Id from user
        String strUid = user.getUid();


        //set a document reference with the userID
        DocumentReference docRef = db.collection("users").document(strUid);


        //get all data in the document and store into a local object
        docRef
                .get()
        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String strFullName = document.get("firstName").toString() + " " + document.get("surname").toString();
                        txtfullname.setText(strFullName);

                        if(document.get("weight") != null){
                            txtWeight.setText(document.get("weight").toString());
                            lblWeight.setText(document.get("weight").toString());
                        }
                        if(document.get("height") != null){
                            txtHeight.setText(document.get("height").toString());
                        }
                        if(document.get("weight_goal") != null){
                            txtWeightGoal.setText(document.get("weight_goal").toString());
                        }
                        if(document.get("step_goal") != null){
                            txtStepGoal.setText(document.get("step_goal").toString());
                        }
                        if(document.get("birthday") != null){
                            Datetxt.setText(document.get("birthday").toString());
                        }
                        if(document.get("gender") != null){

                            int index = 0;
                            String compareValue = document.get("gender").toString();

                            for(int i = 0; i < spinGender.getCount(); i++){
                                if(spinGender.getItemAtPosition(i).equals(compareValue)){
                                    index = i;
                                }
                            }
                            spinGender.setSelection(index);
                        }

                    } else {
                        //Log.d(TAG, "No such document");
                    }
                } else {
                   // Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

}