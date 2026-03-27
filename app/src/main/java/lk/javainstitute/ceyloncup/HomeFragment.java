package lk.javainstitute.ceyloncup;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.javainstitute.ceyloncup.model.Product;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView1;
    private FirebaseFirestore firebaseFirestore;
    private ArrayList<Product> productList;
    private ProductsAdapter productsAdapter;
    private SearchView searchView;

    @Override
    public void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(getContext(), SigninActivity.class));
            requireActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        //load product
        productList = new ArrayList<>();

        recyclerView1 = rootView.findViewById(R.id.productRecyclerView);

        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getActivity());
        recyclerView1.setLayoutManager(layoutManager1);

        productsAdapter = new ProductsAdapter(getActivity(),productList);
        recyclerView1.setAdapter(productsAdapter);

        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("product")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot productSnapshots) {
                        if (!productSnapshots.isEmpty()) {
                            for (DocumentSnapshot productDoc : productSnapshots) {
                                String documentId = productDoc.getId();
                                Product product = productDoc.toObject(Product.class);
                                if (product != null) {
                                    product.setId(documentId);

                                    productList.add(product);
                                }
                            }
                            productsAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error");
                    }
                });

        //search product
        searchView = rootView.findViewById(R.id.searchView);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        return rootView;
    }

    private void filterList(String newText) {
        List<Product> filteredList = new ArrayList<>();

        if (newText.isEmpty()) {
            filteredList.addAll(productList); // Reset to full list when search is cleared
        } else {
            for (Product product : productList) {
                if (product.getProductTitle().toLowerCase().contains(newText.toLowerCase())) {
                    filteredList.add(product);
                }
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(getContext(), "No Products found.", Toast.LENGTH_SHORT).show();
        }

        productsAdapter.setFilteredList(filteredList);
    }
}

//Product Adapter
class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ProductsViewHolder>{

    private Context context;

    static class ProductsViewHolder extends RecyclerView.ViewHolder{

        TextView textViewProductName;
        TextView textViewProductPrice;
        ImageView imageViewProduct;

        public ProductsViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName1);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductProduct);
            imageViewProduct = itemView.findViewById(R.id.productImage1);
        }
    }

    private List<Product> productsArrayList;

    public ProductsAdapter(Context context,List<Product> productsList) {
        this.context = context;
        this.productsArrayList = productsList;
    }

    public void setFilteredList(List<Product> filteredList){
        this.productsArrayList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductsAdapter.ProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_itemad, parent, false);
        return new ProductsAdapter.ProductsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductsAdapter.ProductsViewHolder holder, int position) {
        Product product = productsArrayList.get(position);

        Glide.with(context)
                .load(product.getImageURL())
                .placeholder(R.drawable.image)
                .error(R.drawable.image)
                .into(holder.imageViewProduct);

        Log.d("ImageURL", "Image URL: " + product.getImageURL());

        //go to single product view
        holder.imageViewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SingleProductViewActivity.class);
                intent.putExtra("PRODUCT_ID", product.getId());

                Log.i("Product_id",product.getId());

                context.startActivity(intent);
            }
        });

        holder.textViewProductName.setText(product.getProductTitle());
        holder.textViewProductPrice.setText("Rs. "+product.getProductPrice());
    }

    @Override
    public int getItemCount() {
        return productsArrayList.size();
    }
}
