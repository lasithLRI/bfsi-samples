package com.wso2.openbanking.demo.models;


import com.wso2.openbanking.demo.utils.ConfigLoader;

/**
 * Represents the display information for a bank available in the add account flow.
 */
public class AddAccountBankInfo {
    String name;
    String image;
    boolean flag;



    /**
     * Constructs an AddAccountBankInfo with the specified name and image.
     *
     * @param name  the display name of the bank
     * @param image the image URL or path representing the bank's logo
     */
    public AddAccountBankInfo(String name, String image) {
        this.name = name;
        this.image = image;
        if (name.equalsIgnoreCase(ConfigLoader.getMockBankName())){
            flag = true;
        }
    }

    /**
     * Returns the display name of the bank.
     *
     * @return the bank name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of the bank.
     *
     * @param name the bank name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the image URL or path representing the bank's logo.
     *
     * @return the bank image
     */
    public String getImage() {
        return image;
    }

    /**
     * Returns whether this bank is supported for account addition.
     *
     * @return true if this bank is the configured mock bank
     */
    public boolean isFlag() {
        return flag;
    }
}
