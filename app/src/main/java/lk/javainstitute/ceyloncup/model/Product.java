package lk.javainstitute.ceyloncup.model;

import com.google.firebase.firestore.PropertyName;

public class Product {

    private String id;
    @PropertyName("product_name")
    private String productTitle;
    @PropertyName("price")
    private String productPrice;
    @PropertyName("description")
    private String productDescription;
    @PropertyName("image_url")
    private String imageURL;

    public Product(String id, String productTitle, String productPrice, String productDescription, String imageURL) {
        this.id = id;
        this.productTitle = productTitle;
        this.productPrice = productPrice;
        this.productDescription = productDescription;
        this.imageURL = imageURL;
    }

    public Product() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @PropertyName("product_name")
    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    @PropertyName("price")
    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }
    @PropertyName("image_url")
    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
