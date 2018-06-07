package com.cmbpizza.razor.colombopizza;

/**
 * Created by RazR on 2/13/2018. for use as a class to store the checkout item details
 */

public class CartItems {
    private String cartId;
    private int productId;
    private int productQuantity;

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }

    CartItems(String cartId, int productId, int productQuantity) {
        this.cartId = cartId;
        this.productId = productId;
        this.productQuantity = productQuantity;
    }
}