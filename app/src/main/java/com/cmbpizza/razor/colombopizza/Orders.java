package com.cmbpizza.razor.colombopizza;

/**
 * Created by RazR on 2/15/2018.
 */

public class Orders {
    private String orderId;
    private int memberId;
    private String[] productIdList;
    private String[] productQuantityList;
    private int netTotalPrice;
    private int orderStatus;

    public Orders(String orderId, int memberId, String[] productIdList, String[] productQuantityList, int netTotalPrice, int orderStatus) {
        this.orderId = orderId;
        this.memberId = memberId;
        this.productIdList = productIdList;
        this.productQuantityList = productQuantityList;
        this.netTotalPrice = netTotalPrice;
        this.orderStatus = orderStatus;

    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }



    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String[] getProductIdList() {
        return productIdList;
    }

    public void setProductIdList(String[] productIdList) {
        this.productIdList = productIdList;
    }

    public String[] getProductQuantityList() {
        return productQuantityList;
    }

    public void setProductQuantityList(String[] productQuantityList) {
        this.productQuantityList = productQuantityList;
    }

    public int getNetTotalPrice() {
        return netTotalPrice;
    }

    public void setNetTotalPrice(int netTotalPrice) {
        this.netTotalPrice = netTotalPrice;
    }
}
