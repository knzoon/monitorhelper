package se.knzoon.knzoonmonitor.model;

import com.google.gson.annotations.SerializedName;

public class TurfEffort {
    @SerializedName("username")
    private String username;
    @SerializedName("points")
    private int points;
    @SerializedName("timeSpent")
    private String timeSpent;
    @SerializedName("takes")
    private int takes;
    @SerializedName("routes")
    private int routes;
    @SerializedName("takesInRoutes")
    private int takesInRoutes;
    @SerializedName("pointsByPph")
    private int pointsByPph;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(String timeSpent) {
        this.timeSpent = timeSpent;
    }

    public int getTakes() {
        return takes;
    }

    public void setTakes(int takes) {
        this.takes = takes;
    }

    public int getRoutes() {
        return routes;
    }

    public void setRoutes(int routes) {
        this.routes = routes;
    }

    public int getTakesInRoutes() {
        return takesInRoutes;
    }

    public void setTakesInRoutes(int takesInRoutes) {
        this.takesInRoutes = takesInRoutes;
    }

    public int getPointsByPph() {
        return pointsByPph;
    }

    public void setPointsByPph(int pointsByPph) {
        this.pointsByPph = pointsByPph;
    }

    @Override
    public String toString() {
        return "TurfEffort{" +
                "username='" + username + '\'' +
                ", points=" + points +
                ", timeSpent='" + timeSpent + '\'' +
                ", takes=" + takes +
                ", routes=" + routes +
                ", takesInRoutes=" + takesInRoutes +
                ", pointsByPph=" + pointsByPph +
                '}';
    }
}
