package com.teamunknown.paranbende.model.Settings.UserSettings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by halitogunc on 17.02.2018.
 */

public class UserSettingsModel {
    @SerializedName("error")
    @Expose
    private Boolean error;
    @SerializedName("data")
    @Expose
    private UserSettingsData data;
    @SerializedName("message")
    @Expose
    private String message;


    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserSettingsData getData() {
        return data;
    }

    public void setData(UserSettingsData data) {
        this.data = data;
    }
}
