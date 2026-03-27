package lk.javainstitute.ceyloncup;

import com.google.firebase.firestore.Exclude;

public class Producthal {
    
    private String id;
    private String productname;
    private String price;
    private String description;
    private String imageURL;
    private boolean active;
    
    // Empty constructor required for Firestore
    public Producthal() {
    }
    
    public Producthal(String productname, String price, String description, 
                      String imageURL, boolean active) {
        this.productname = productname;
        this.price = price;
        this.description = description;
        this.imageURL = imageURL;
        this.active = active;
    }
    
    @Exclude
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getProductname() {
        return productname;
    }
    
    public void setProductname(String productname) {
        this.productname = productname;
    }
    
    public String getPrice() {
        return price;
    }
    
    public void setPrice(String price) {
        this.price = price;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImageURL() {
        return imageURL;
    }
    
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}