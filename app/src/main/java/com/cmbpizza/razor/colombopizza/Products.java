package com.cmbpizza.razor.colombopizza;

import android.support.annotation.NonNull;

/**
 * Created by RazR on 1/31/2018.
 */

public class Products implements Comparable<Products>{
    private int productId;
    private String productTitle;
    private String productCategory;
    private int productPrice;
    private String productDescription;
    private byte[] productImage;

    Products(int productId, String productTitle, String productCategory, int productPrice, String productDescription, byte[] productImage) {
        setProductId(productId);
        setProductTitle(productTitle);
        setProductCategory(productCategory);
        setProductPrice(productPrice);
        setProductDescription(productDescription);
        setProductImage(productImage);
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public int getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(int productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public byte[] getProductImage() {
        return productImage;
    }

    public void setProductImage(byte[] productImage) {
        this.productImage = productImage;
    }

    //this method will sort out the product by its product price, the smallest values first (ascending)
    @Override
    public int compareTo(@NonNull Products products) {
        if(productPrice > products.productPrice){
            return 1;
        } else if(productPrice < products.productPrice){
            return -1;
        } else {
            return 0;
        }
    }
}
