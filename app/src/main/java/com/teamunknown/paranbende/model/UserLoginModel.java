package com.teamunknown.paranbende.model;

/**
 * Created by halitogunc on 17.02.2018.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserLoginModel {
    @SerializedName("error")
    @Expose
    private Boolean error;
    @SerializedName("data")
    @Expose
    private Data data;

    @SerializedName("message")
    @Expose
    private String message;

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
