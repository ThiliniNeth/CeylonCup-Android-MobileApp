package lk.javainstitute.ceyloncup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AddProductActivity extends AppCompatActivity {

    private ImageView uploadImage;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private Cloudinary cloudinary;
    static Uri selectedImageUri;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        //image picker
        uploadImage = findViewById(R.id.imageView26);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    galleryLauncher.launch(intent);
                } catch (Exception e) {
                    Toast.makeText(AddProductActivity.this,"Error.",Toast.LENGTH_LONG).show();
                    Log.e("add-product", "Error opening gallery: " + e.getMessage());
                }
            }
        });

        // Register the launcher for the image picker
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        // Set the selected image to the ImageView
                        ImageView imageView = findViewById(R.id.imageView26);
                        imageView.setImageURI(selectedImageUri);
                    }
                }
        );

        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dm2lqc3cf",
                "api_key", "124447939989442",
                "api_secret", "3wi8QMpaJpFbJ75eQeUhbTNRb9s"));


        Button addNewProductButton = findViewById(R.id.button6);
        addNewProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText productTitle = findViewById(R.id.Text1);
                EditText productDescription = findViewById(R.id.Text2);
                EditText productPrice = findViewById(R.id.Text3);

                // Save the image URL after uploading
                if (selectedImageUri != null) {
                    try {
                        // Get the InputStream from the Uri
                        InputStream inputStream = AddProductActivity.this.getContentResolver().openInputStream(selectedImageUri);

                        // Convert InputStream to ByteArrayOutputStream
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = inputStream.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, len);
                        }
                        byte[] imageData = byteArrayOutputStream.toByteArray();

                        // Upload the byte array to Cloudinary
                        new Thread(() -> {
                            try {
                                // Upload image and get the result
                                Map<String, Object> uploadResult = cloudinary.uploader().upload(imageData, ObjectUtils.emptyMap());
                                String imageUrl = (String) uploadResult.get("url");

                                // Call the method to save product data to Firestore on the main thread
                                AddProductActivity.this.runOnUiThread(() -> {
                                    saveProductDataToFireStore(productTitle,  productPrice, productDescription, imageUrl);
                                });
                            } catch (IOException e) {
                                AddProductActivity.this.runOnUiThread(() -> Log.e("add-product", "Image upload failed: " + e.getMessage()));
                            }
                        }).start();

                    } catch (IOException e) {
                        Log.e("add-product", "Error opening InputStream: " + e.getMessage());
                    }
                }
            }
        });
    }

    // Helper method to get the real file path from the URI
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = AddProductActivity.this.getContentResolver().query(contentUri, proj, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        String path = cursor.getString(columnIndex);
        cursor.close();
        return path;
    }

    // Helper method to save product data to FireStore
    private void saveProductDataToFireStore(EditText productTitle, EditText productPrice, EditText productDescription, String imageURL) {

        String title = productTitle.getText().toString();
        String price = productPrice.getText().toString();
        String description = productDescription.getText().toString();

        if (title.isBlank()) {
            Toast.makeText(AddProductActivity.this, "Please enter your Product Title.", Toast.LENGTH_LONG).show();

        } else if (price.isBlank()) {
            Toast.makeText(AddProductActivity.this, "Please enter your Product Price.", Toast.LENGTH_LONG).show();

        } else if (Integer.parseInt(price) == 0) {
            Toast.makeText(AddProductActivity.this, "Invalid Price.", Toast.LENGTH_LONG).show();

        } else if (description.isBlank()) {
            Toast.makeText(AddProductActivity.this, "Please enter your Product Description.", Toast.LENGTH_LONG).show();

        } else {

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            Map<String, Object> data = new HashMap<>();
            data.put("product_name", title);
            data.put("price", price);
            data.put("description", description);
            data.put("status", 1);
            data.put("image_url", imageURL);  // Save the image URL

            firestore.collection("product")
                    .add(data)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.i("add-product", "DocumentSnapshot added with ID: " + documentReference.getId());
                            Toast.makeText(AddProductActivity.this, "Product added Successfully", Toast.LENGTH_LONG).show();

                            ImageView imageView = findViewById(R.id.imageView26);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddProductActivity.this, "Please try aging.", Toast.LENGTH_LONG).show();
                            Log.i("add-product", "Error adding document: " + e.getMessage());
                        }
                    });

            productTitle.setText("");
            productPrice.setText("");
            productDescription.setText("");
            ImageView imageView = findViewById(R.id.imageView26);
            imageView.setImageResource(R.drawable.image);
        }
    }
}