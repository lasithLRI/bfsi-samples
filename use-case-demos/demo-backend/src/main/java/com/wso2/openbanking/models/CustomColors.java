package com.wso2.openbanking.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomColors {
    public String primary;
    public String secondaryColor;
    public String button;
    public String backgroundColor;
    public String tableBackground;
    public String innerButtonBackground;
    public String bankColor1;
    public String bankColor2;
    public String bankColor3;
    public String fontWhite;
    public String formValidationError;

    public CustomColors() {
    }

    public CustomColors(String primary, String secondaryColor, String button, String backgroundColor, String tableBackground, String innerButtonBackground, String bankColor1, String bankColor2, String bankColor3, String fontWhite, String formValidationError) {
        this.primary = primary;
        this.secondaryColor = secondaryColor;
        this.button = button;
        this.backgroundColor = backgroundColor;
        this.tableBackground = tableBackground;
        this.innerButtonBackground = innerButtonBackground;
        this.bankColor1 = bankColor1;
        this.bankColor2 = bankColor2;
        this.bankColor3 = bankColor3;
        this.fontWhite = fontWhite;
        this.formValidationError = formValidationError;
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public String getButton() {
        return button;
    }

    public void setButton(String button) {
        this.button = button;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getTableBackground() {
        return tableBackground;
    }

    public void setTableBackground(String tableBackground) {
        this.tableBackground = tableBackground;
    }

    public String getInnerButtonBackground() {
        return innerButtonBackground;
    }

    public void setInnerButtonBackground(String innerButtonBackground) {
        this.innerButtonBackground = innerButtonBackground;
    }

    public String getBankColor1() {
        return bankColor1;
    }

    public void setBankColor1(String bankColor1) {
        this.bankColor1 = bankColor1;
    }

    public String getBankColor2() {
        return bankColor2;
    }

    public void setBankColor2(String bankColor2) {
        this.bankColor2 = bankColor2;
    }

    public String getBankColor3() {
        return bankColor3;
    }

    public void setBankColor3(String bankColor3) {
        this.bankColor3 = bankColor3;
    }

    public String getFontWhite() {
        return fontWhite;
    }

    public void setFontWhite(String fontWhite) {
        this.fontWhite = fontWhite;
    }

    public String getFormValidationError() {
        return formValidationError;
    }

    public void setFormValidationError(String formValidationError) {
        this.formValidationError = formValidationError;
    }
}
