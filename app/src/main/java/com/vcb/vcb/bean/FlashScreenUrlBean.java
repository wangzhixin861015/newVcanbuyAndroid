package com.vcb.vcb.bean;

import java.io.Serializable;

public class FlashScreenUrlBean implements Serializable {

    private String imageUrl;
    private String backgroundColor;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
