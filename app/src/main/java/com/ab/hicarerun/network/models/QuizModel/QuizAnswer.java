package com.ab.hicarerun.network.models.QuizModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Arjun Bhatt on 2/11/2021.
 */
public class QuizAnswer {
    @SerializedName("OptionId")
    @Expose
    private Integer optionId;
    @SerializedName("OptionType")
    @Expose
    private String optionType;
    @SerializedName("OptionTypeId")
    @Expose
    private Integer optionTypeId;
    @SerializedName("OptionTitle")
    @Expose
    private String optionTitle;
    @SerializedName("OptionDescription")
    @Expose
    private String optionDescription;
    @SerializedName("OptionValue")
    @Expose
    private String optionValue;
    @SerializedName("OptionUrl")
    @Expose
    private String optionUrl;
    @SerializedName("IsSelected")
    @Expose
    private Boolean isSelected;

    public Integer getOptionId() {
        return optionId;
    }

    public void setOptionId(Integer optionId) {
        this.optionId = optionId;
    }

    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }

    public Integer getOptionTypeId() {
        return optionTypeId;
    }

    public void setOptionTypeId(Integer optionTypeId) {
        this.optionTypeId = optionTypeId;
    }

    public String getOptionTitle() {
        return optionTitle;
    }

    public void setOptionTitle(String optionTitle) {
        this.optionTitle = optionTitle;
    }

    public String getOptionDescription() {
        return optionDescription;
    }

    public void setOptionDescription(String optionDescription) {
        this.optionDescription = optionDescription;
    }

    public String getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

    public String getOptionUrl() {
        return optionUrl;
    }

    public void setOptionUrl(String optionUrl) {
        this.optionUrl = optionUrl;
    }

    public Boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }
}
