package lk.javainstitute.ceyloncup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.javainstitute.ceyloncup.model.Cart;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;

public class checkoutActivity extends AppCompatActivity {

    private static final String TAG = "PayHereDemo";

    private TextView textViewTotal;
    private double totalAmount;

    private  ArrayList<Cart> cartList;

    private final ActivityResultLauncher<Intent> payHereLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null){
                    Intent data = result.getData();
                    if (data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)){
                        Serializable serializable = data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
                        if (serializable instanceof PHResponse){
                            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) serializable;
                            if (response.isSuccess()) {
                                String transactionId = response.getData().toString(); // Get Transaction ID
                                Log.i(TAG, "Payment Success: " + transactionId);
                                Toast.makeText(checkoutActivity.this, "Payment Success: " + transactionId, Toast.LENGTH_LONG).show();

                                // Call confirmOrder() with the transaction ID
                                confirmOrder(transactionId);
                            } else {
                                Log.e(TAG, "Payment Failed: " + response);
                                Toast.makeText(checkoutActivity.this, "Payment Failed: " + response, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }else if (result.getResultCode() == Activity.RESULT_CANCELED){
                    Toast.makeText(checkoutActivity.this,"Canceled the request.",Toast.LENGTH_LONG).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textViewTotal = findViewById(R.id.textView42);

        Intent intent = getIntent();
        totalAmount = intent.getDoubleExtra("totalAmount", 0.0);
        cartList = (ArrayList<Cart>) intent.getSerializableExtra("cartList"); // Retrieve cart data

        if (cartList == null) {
            cartList = new ArrayList<>(); // Prevent null reference errors
        }

        textViewTotal.setText(String.format("Total: Rs. %.2f", totalAmount));

        Button buttonPay = findViewById(R.id.button7);
        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiatePayment();
            }
        });

    }

    private void initiatePayment(){
        InitRequest req = new InitRequest();
        req.setMerchantId("1221223");       // Merchant ID
        req.setCurrency("LKR");             // Currency code LKR/USD/GBP/EUR/AUD
        req.setAmount(totalAmount);             // Final Amount to be charged
        req.setOrderId("230000123" + 1);        // Unique Reference ID
        req.setItemsDescription("Door bell wireless");  // Item description title
        req.setCustom1("This is the custom message 1");
        req.setCustom2("This is the custom message 2");
        req.getCustomer().setFirstName("Saman");
        req.getCustomer().setLastName("Perera");
        req.getCustomer().setEmail("samanp@gmail.com");
        req.getCustomer().setPhone("+94771234567");
        req.getCustomer().getAddress().setAddress("No.1, Galle Road");
        req.getCustomer().getAddress().setCity("Colombo");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        //Optional Params
        req.getCustomer().getDeliveryAddress().setAddress("No.90/8, Liyanagemulla, Seeduwa.");
        req.getCustomer().getDeliveryAddress().setCity("Gampaha");
        req.getCustomer().getDeliveryAddress().setCountry("Sri Lanka");
        req.getItems().add(new Item(null, "Door bell wireless", 1, 1000.0));

        req.setNotifyUrl("PEACOCK");           // Notify Url

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);

        payHereLauncher.launch(intent);

    }

    private void confirmOrder(String transactionId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(checkoutActivity.this, "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert cart items to a list of maps
        List<Map<String, Object>> orderItems = new ArrayList<>();
        for (Cart cartItem : cartList) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("productId", cartItem.getProductId());
            itemMap.put("productName", cartItem.getProductName());
            itemMap.put("productImage", cartItem.getProductImage());
            itemMap.put("quantity", cartItem.getQuantity());
            itemMap.put("productPrice", cartItem.getProductPrice());

            orderItems.add(itemMap);
        }

        // Create order data
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userId", user.getUid());
        orderData.put("items", orderItems);
        orderData.put("totalAmount", totalAmount);
        orderData.put("transactionId", transactionId);
        orderData.put("status", "Completed");
        orderData.put("timestamp", System.currentTimeMillis());

        FirebaseFirestore.getInstance().collection("orders")
                .add(orderData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(checkoutActivity.this, "Order placed successfully", Toast.LENGTH_SHORT).show();
                    clearCart(); // Clear the cart after successful order

                    // Go to tracking activity with order ID
                    Intent intent = new Intent(checkoutActivity.this, TrackingActivity.class);
                    intent.putExtra("orderId", documentReference.getId());
                    startActivity(intent);

                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(checkoutActivity.this, "Error placing order", Toast.LENGTH_SHORT).show());
    }

    private void clearCart() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("user").document(userId).collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> Log.d("Cart", "Item deleted"))
                                .addOnFailureListener(e -> Log.e("Cart", "Error deleting item", e));
                    }
                    Toast.makeText(checkoutActivity.this, "Cart cleared successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e("Cart", "Failed to clear cart", e));
    }
}