package lk.javainstitute.ceyloncup;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import lk.javainstitute.ceyloncup.model.Product;

public class SingleProductViewActivity extends AppCompatActivity {

    private ImageView productImage;
    private TextView productName;
    private TextView productPrice;
    private TextView productDescription;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String productId; // Firestore Document ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_single_product_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //data load in textView
        productImage = findViewById(R.id.detailImage);
        productName = findViewById(R.id.detailTitle);
        productPrice = findViewById(R.id.textView29);
        productDescription = findViewById(R.id.Desc);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        productId = getIntent().getStringExtra("PRODUCT_ID");

        if (productId != null) {
            loadProductDetails(productId);
        } else {
            Toast.makeText(this, "Product ID or Seller ID is missing!", Toast.LENGTH_SHORT).show();
        }

        //Add to cart
        Button addToCartButton = findViewById(R.id.buttoncart);
        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Toast.makeText(SingleProductViewActivity.this, "Please log in to add items to cart", Toast.LENGTH_SHORT).show();
                    return;
                }

                String userId = user.getUid();
                DocumentReference cartRef = firebaseFirestore
                        .collection("user")
                        .document(userId)
                        .collection("cart")
                        .document(productId);

                // Get product image URL from ImageView (Glide stores it in a tag)
                String imageUrl = (String) productImage.getTag();

                // Cart item data
                HashMap<String, Object> cartItem = new HashMap<>();
                cartItem.put("productId", productId);
                cartItem.put("productName", productName.getText().toString());
                cartItem.put("productPrice", Double.parseDouble(productPrice.getText().toString().replace("Rs. ","")));
                cartItem.put("productImage",imageUrl);
                cartItem.put("quantity", 1);  // Default quantity

                cartRef.set(cartItem)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(SingleProductViewActivity.this, "Added to cart!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SingleProductViewActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void loadProductDetails(String productId){
        // Retrieve the product from the seller's collection
        firebaseFirestore.collection("product")
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Product product = documentSnapshot.toObject(Product.class);

                        if (product != null) {
                            // Set data to the UI
                            productName.setText(product.getProductTitle());
                            productPrice.setText("Rs. " + product.getProductPrice());
                            productDescription.setText(product.getProductDescription());

                            // Load the product image using Glide
                            Glide.with(SingleProductViewActivity.this)
                                    .load(product.getImageURL())
                                    .placeholder(R.drawable.image)  // Placeholder image
                                    .error(R.drawable.image)  // Error image
                                    .into(productImage);

                            // Store the image URL in the tag for later use
                            productImage.setTag(product.getImageURL());
                        }
                    } else {
                        Toast.makeText(SingleProductViewActivity.this, "Product not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("single-product", "Error fetching product details", e);
                    Toast.makeText(SingleProductViewActivity.this, "Failed to load product", Toast.LENGTH_SHORT).show();
                });
    }
}