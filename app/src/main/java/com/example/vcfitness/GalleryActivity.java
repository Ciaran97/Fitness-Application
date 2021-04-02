package com.example.vcfitness;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.DiscretePathEffect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GalleryActivity extends AppCompatActivity {


    private static final int SELECT_IMAGE = 1;
    private static final int REQUEST_CAMERA= 2;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage;
    FirebaseAuth mAuth;

    String Uid;
    Uri downloadUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        storage = FirebaseStorage.getInstance();

mAuth = FirebaseAuth.getInstance();

Uid = mAuth.getUid();
    }



    private void InsertImage()
    {

    }

    private void selectImage() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {
                   // Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                   // File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                   // intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                   // startActivityForResult(intent, REQUEST_CAMERA);

                    dispatchTakePictureIntent();
                }
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA &&
                resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
               // mImageView.setImageBitmap(imageBitmap);
                uploadImage(imageBitmap);
            }
        }
        if (requestCode == SELECT_IMAGE &&
                resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                final Uri uri = data.getData();

                Bitmap imageBitmap = null;
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                } catch (IOException e) {
                    Toast.makeText(GalleryActivity.this, "No image " + e, Toast.LENGTH_LONG).show();

                    e.printStackTrace();
                }
                // mImageView.setImageBitmap(imageBitmap);
                uploadImage(imageBitmap);
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_CAMERA);
        }
    }

    public void Add_Image(View view) {

        selectImage();

    }

    public void uploadImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
       final long timemili = System.currentTimeMillis();


        //FirebaseStorage storageref = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference();
        final StorageReference imagesRef = storageRef.child("/images/" + Uid + "/" + timemili + ".jpg");

        UploadTask uploadTask = imagesRef.putBytes(data);



        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                final StorageReference imagesRef1 = storageRef.child("/images/" + Uid + "/" + timemili + ".jpg");
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
               imagesRef1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                   @Override
                   public void onSuccess(Uri uri) {
                       downloadUrl = uri;
                       InsertFirestoreImageUrl(downloadUrl.toString());
                   }
               }).addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       Toast.makeText(GalleryActivity.this, "DB Insert Failure: " + e, Toast.LENGTH_LONG).show();
                   }
               });
                //String downloadUrl = taskSnapshot.getMetadata().getReference().getStorage().
                // Store Download URL to firestore (Collection(gallery) - document (Uid) - collection



            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(GalleryActivity.this, "DB Insert Failure: " + exception, Toast.LENGTH_LONG).show();
            }
        });
    }

    //gs://vcfitness-e2b81.appspot.com/ProfilePictures/9jOSuX7GEEXJad8fbABYGyQRzJi2.jpg

    private void InsertFirestoreImageUrl(String URL) {



        String Time = String.valueOf(System.currentTimeMillis());


        Map<String, Object> user = new HashMap<>();


        user.put(Time, URL);

        DocumentReference docRef = db.collection("StoredImages/").document(Uid);


        // Add a new document with a generated ID
       docRef
                .set(user, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        Toast.makeText(GalleryActivity.this, "DB Insert Success: ", Toast.LENGTH_LONG).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        //Log.w(TAG, "Error adding document", e);
                        Toast.makeText(GalleryActivity.this, "DB Insert failed: " + e, Toast.LENGTH_LONG).show();
                    }
                });

    }

    public void Show_Pictures(View view) {
        OpenImagesActivity();
    }

    private void OpenImagesActivity() {
        Intent intent = new Intent(this, ImagesActivity.class);
        startActivity(intent);
    }
}
