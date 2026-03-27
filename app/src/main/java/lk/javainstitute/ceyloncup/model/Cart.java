package lk.javainstitute.ceyloncup.model;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class Cart implements Serializable {

    @PropertyName("productId")
    private String productId;
    @PropertyName("productName")
    private String productName;
    @PropertyName("productPrice")
    private double productPrice;
    @PropertyName("productImage")
    private String productImage;
    @PropertyName("quantity")
    private int quantity;

    public Cart(String productId, String productName, double productPrice, String productImage, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productImage = productImage;
        this.quantity = quantity;
    }

    public Cart() {
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
