package com.ab.hicarerun.network.models.OffersModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Arjun Bhatt on 3/26/2020.
 */
public class RewardDetailHistorySummary {

    @SerializedName("EventName")
    @Expose
    private String eventName;
    @SerializedName("ScoreDescription")
    @Expose
    private String scoreDescription;
    @SerializedName("ScoreName")
    @Expose
    private String scoreName;
    @SerializedName("PointsEarned")
    @Expose
    private Integer pointsEarned;
    @SerializedName("IsMissedEvent")
    @Expose
    private Boolean IsMissedEvent;

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getScoreDescription() {
        return scoreDescription;
    }

    public void setScoreDescription(String scoreDescription) {
        this.scoreDescription = scoreDescription;
    }

    public String getScoreName() {
        return scoreName;
    }

    public void setScoreName(String scoreName) {
        this.scoreName = scoreName;
    }

    public Integer getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(Integer pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public Boolean getMissedEvent() {
        return IsMissedEvent;
    }

    public void setMissedEvent(Boolean missedEvent) {
        IsMissedEvent = missedEvent;
    }
}
