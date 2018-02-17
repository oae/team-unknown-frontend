package com.teamunknown.paranbende.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by halitogunc on 17.02.2018.
 */

public class WithdrawalDataModel {
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("amount")
    @Expose
    private Integer amount;
    @SerializedName("taker")
    @Expose
    private WithdrawalTakerModel taker;
    @SerializedName("takerLocation")
    @Expose
    private List<Double> takerLocation = null;
    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("updatedAt")
    @Expose
    private String updatedAt;
    @SerializedName("createdAt")
    @Expose
    private String createdAt;
    @SerializedName("__v")
    @Expose
    private Integer v;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }


    public List<Double> getTakerLocation() {
        return takerLocation;
    }

    public void setTakerLocation(List<Double> takerLocation) {
        this.takerLocation = takerLocation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getV() {
        return v;
    }

    public void setV(Integer v) {
        this.v = v;
    }

    public WithdrawalTakerModel getTaker() {
        return taker;
    }

    public void setTaker(WithdrawalTakerModel taker) {
        this.taker = taker;
    }
}
