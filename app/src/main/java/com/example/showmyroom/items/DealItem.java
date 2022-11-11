package com.example.showmyroom.items;

import android.net.Uri;

public class DealItem {
    String price;
    String title;
    Uri productImage;

    public DealItem(String price, String title, Uri productImage) {
        this.price = price;
        this.title = title;
        this.productImage = productImage;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Uri getProductImage() {
        return productImage;
    }

    public void setProductImage(Uri productImage) {
        this.productImage = productImage;
    }
}
