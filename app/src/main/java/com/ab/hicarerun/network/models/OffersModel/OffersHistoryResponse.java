package com.ab.hicarerun.network.models.OffersModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Arjun Bhatt on 3/26/2020.
 */
public class OffersHistoryResponse {

    @SerializedName("IsSuccess")
    @Expose
    private Boolean isSuccess;
    @SerializedName("Data")
    @Expose
    private RewardsHistoryData data;
    @SerializedName("ErrorMessage")
    @Expose
    private String errorMessage;
    @SerializedName("Param1")
    @Expose
    private Boolean param1;
    @SerializedName("ResponseMessage")
    @Expose
    private Object responseMessage;

    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public RewardsHistoryData getData() {
        return data;
    }

    public void setData(RewardsHistoryData data) {
        this.data = data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Boolean getParam1() {
        return param1;
    }

    public void setParam1(Boolean param1) {
        this.param1 = param1;
    }

    public Object getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(Object responseMessage) {
        this.responseMessage = responseMessage;
    }
}
