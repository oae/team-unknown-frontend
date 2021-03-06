package com.teamunknown.paranbende.model.Settings.UserSettings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by halitogunc on 17.02.2018.
 */

public class UserSettingsMaker {
    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("minAmount")
    @Expose
    private Integer minAmount;
    @SerializedName("maxAmount")
    @Expose
    private Integer maxAmount;
    @SerializedName("range")
    @Expose
    private Integer range;
    @SerializedName("online")
    @Expose
    private Boolean online;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Integer minAmount) {
        this.minAmount = minAmount;
    }

    public Integer getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Integer maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Integer getRange() {
        return range;
    }

    public void setRange(Integer range) {
        this.range = range;
    }


    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }
}
