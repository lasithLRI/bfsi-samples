package com.wso2.openbanking.models;

public class AddAccountBankInfo {
    String name;
    String image;

    public AddAccountBankInfo(String name, String image) {
        this.name = name;
        this.image = image;
    }

    public AddAccountBankInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
