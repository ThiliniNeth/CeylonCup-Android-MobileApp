package lk.javainstitute.ceyloncup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;

import lk.javainstitute.ceyloncup.model.Cart;

public class CartFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private ArrayList<Cart> cartList;
    private CartAdapter cartAdapter;

    private TextView textViewSubtotal, textViewDelivery, textViewTotalTax, textViewTotal;
    private static final double DELIVERY_FEE = 5.00; // Fixed delivery fee
    private static final double TAX_RATE = 0.08; // 8% tax

    private Button checkOutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_cart2, container, false);

        //total
        textViewSubtotal = rootView.findViewById(R.id.textViewSubtotalValue);
        textViewDelivery = rootView.findViewById(R.id.textViewDeliveryValue);
        textViewTotalTax = rootView.findViewById(R.id.textViewTaxValue);
        textViewTotal = rootView.findViewById(R.id.textViewTotalValue);

        //load cart
        cartList = new ArrayList<>();

        recyclerView = rootView.findViewById(R.id.recyclerViewCart);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        cartAdapter = new CartAdapter(getActivity(),cartList, this::loadCartItems);
        recyclerView.setAdapter(cartAdapter);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        loadCartItems();

        //check out button
        checkOutButton = rootView.findViewById(R.id.btnCheckout);
        checkOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToCheckout();
            }
        });

        return rootView;
    }

    //check out
    private void proceedToCheckout(){
        if (cartList.isEmpty()) {
            Toast.makeText(getContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate total amount
        double totalAmount = 0.0;
        for (Cart item : cartList) {
            totalAmount += item.getQuantity() * item.getProductPrice();
        }
        double totalTax = totalAmount * TAX_RATE;
        double grandTotal = totalAmount + totalTax + DELIVERY_FEE;

        // Convert cartList to JSON to pass via intent
        Gson gson = new Gson();
//        String cartJson = gson.toJson(cartList);

        Intent intent = new Intent(getActivity(), checkoutActivity.class);
        intent.putExtra("cartList", cartList);
        intent.putExtra("totalAmount", grandTotal);
        startActivity(intent);
    }

    //cart load items
    private void loadCartItems() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Please log in to view cart", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        firebaseFirestore.collection("user").document(userId).collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cartList.clear();
                    double subtotal = 0.0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Cart cartItem = document.toObject(Cart.class);

                        // Convert price if stored as String
                        Object priceObject = document.get("productPrice");
                        double productPrice = 0.0;
                        if (priceObject instanceof String) {
                            productPrice = Double.parseDouble((String) priceObject);
                        } else if (priceObject instanceof Number) {
                            productPrice = ((Number) priceObject).doubleValue();
                        }
                        cartItem.setProductPrice(productPrice);

                        // Calculate subtotal
                        subtotal += cartItem.getQuantity() + cartItem.getProductPrice();
                        cartList.add(cartItem);
                    }
                    cartAdapter.notifyDataSetChanged();

                    // Calculate totals
                    double totalTax = subtotal * TAX_RATE;
                    double grandTotal = subtotal + totalTax + DELIVERY_FEE;

                    // Update UI
                    textViewSubtotal.setText(String.format("Rs. %.2f", subtotal));
                    textViewDelivery.setText(String.format("Rs. %.2f", DELIVERY_FEE));
                    textViewTotalTax.setText(String.format("Rs. %.2f", totalTax));
                    textViewTotal.setText(String.format("Rs. %.2f", grandTotal));
                })
                .addOnFailureListener(e -> {
                    Log.e("Cart", "Error loading cart", e);
                    Toast.makeText(getContext(), "Failed to load cart", Toast.LENGTH_SHORT).show();
                });
    }
}

//Cart Adapter
class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder>{

    private Context context;

    public static class CartViewHolder extends RecyclerView.ViewHolder {

        TextView textViewProductName;
        TextView textViewProductPrice;
        TextView textViewProductQty;
        TextView textViewPlus;
        TextView textViewMinus;
        ImageButton imageButtonDelete;
        ImageView imageViewProduct;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName1);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductProduct);
            textViewProductQty = itemView.findViewById(R.id.textView53);
            imageViewProduct = itemView.findViewById(R.id.productImage1);

            textViewPlus = itemView.findViewById(R.id.textView54);
            textViewMinus = itemView.findViewById(R.id.textView52);

            imageButtonDelete = itemView.findViewById(R.id.imageButtonDeleteCart);
        }
    }

    // Define an interface to notify the fragment
    public interface CartUpdateListener {
        void onCartUpdated();
    }

    public ArrayList<Cart> cartArrayList;
    private CartUpdateListener cartUpdateListener;


    public CartAdapter(Context context,ArrayList<Cart> cartArrayList, CartUpdateListener cartUpdateListener) {
        this.cartArrayList = cartArrayList;
        this.context = context;
        this.cartUpdateListener = cartUpdateListener;
    }

    @NonNull
    @Override
    public CartAdapter.CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        return new CartAdapter.CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.CartViewHolder holder, int position) {
        Cart cart = cartArrayList.get(position);

        Glide.with(context)
                .load(cart.getProductImage())
                .placeholder(R.drawable.image)
                .error(R.drawable.image)
                .into(holder.imageViewProduct);

        holder.textViewProductName.setText(cart.getProductName());
        holder.textViewProductPrice.setText("Rs. "+String.valueOf(cart.getProductPrice()));
        holder.textViewProductQty.setText(String.valueOf(cart.getQuantity()));

        double unitPrice = cart.getProductPrice() / cart.getQuantity(); // Ensure product price is per unit

        holder.textViewPlus.setOnClickListener(v -> {
            int newQuantity = cart.getQuantity() + 1;
            double newPrice = unitPrice * newQuantity;

            cart.setQuantity(newQuantity);
            cart.setProductPrice(newPrice);

            updateCartItem(cart); // Update Firebase
            notifyItemChanged(position);
        });

        holder.textViewMinus.setOnClickListener(v -> {
            if (cart.getQuantity() > 1) {
                int newQuantity = cart.getQuantity() - 1;
                double newPrice = unitPrice * newQuantity;

                cart.setQuantity(newQuantity);
                cart.setProductPrice(newPrice);

                updateCartItem(cart); // Update Firebase
                notifyItemChanged(position);
            }
        });

        holder.imageButtonDelete.setOnClickListener(v -> {
            deleteCartItem(cart, position);
        });
    }

    @Override
    public int getItemCount() {
        return cartArrayList.size();
    }

    private void updateCartItem(Cart cart) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        DocumentReference cartRef = FirebaseFirestore.getInstance()
                .collection("user").document(userId)
                .collection("cart").document(cart.getProductId());

        cartRef.update("quantity", cart.getQuantity(), "productPrice", cart.getProductPrice())
                .addOnSuccessListener(aVoid -> {
                    Log.d("Cart", "Cart updated successfully");

                    // Notify fragment to update totals
                    if (cartUpdateListener != null) {
                        cartUpdateListener.onCartUpdated();
                    }
                })
                .addOnFailureListener(e -> Log.e("Cart", "Error updating cart", e));
    }

    private void deleteCartItem(Cart cart, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        DocumentReference cartRef = FirebaseFirestore.getInstance()
                .collection("user").document(userId)
                .collection("cart").document(cart.getProductId());

        cartRef.delete()
                .addOnSuccessListener(aVoid -> {
                    cartArrayList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e("Cart", "Error deleting item", e));
    }
}