package com.vcb.vcb.bean;

import java.io.Serializable;
import java.util.List;

public class GuideBean {

    private String success;
    private String success_message;
    private List<Guide> datalist;

    public class Guide implements Serializable {
        private int status;
        private String status_title;
        private String name;
        private int id;
        private String content;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getStatus_title() {
            return status_title;
        }

        public void setStatus_title(String status_title) {
            this.status_title = status_title;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }


    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getSuccess_message() {
        return success_message;
    }

    public void setSuccess_message(String success_message) {
        this.success_message = success_message;
    }

    public List<Guide> getDatalist() {
        return datalist;
    }

    public void setDatalist(List<Guide> datalist) {
        this.datalist = datalist;
    }
}
