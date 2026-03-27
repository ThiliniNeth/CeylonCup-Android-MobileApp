package lk.javainstitute.ceyloncup;

public class DataClass {
    private String imageURL, caption;

    public DataClass(){

    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public DataClass(String imageURL, String caption) {
        this.imageURL = imageURL;
        this.caption = caption;
    }


    public int getProductName() {
        return 0;

    }

    public byte[] getImageUrl() {
        return null;
    }
}