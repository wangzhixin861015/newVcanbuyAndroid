package com.vcb.vcb.bean;

import java.io.Serializable;
import java.util.List;

public class ImageUrlBean implements Serializable {

    private List<ImageItem> imageUrl;
    private String backgroundColor;

    public List<ImageItem> getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(List<ImageItem> imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public class ImageItem implements Serializable{
        private String uid;
        private String name;
        private String url;
        private String status;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

}
