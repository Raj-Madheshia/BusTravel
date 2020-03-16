package com.example.bustravel;

public class SourceDestinationData {
    private String place;
    private String distance;
    private String busno;

    public SourceDestinationData(String place, String distance, String busno) {
        this.place = place;
        this.distance = distance;
        this.busno = busno;
    }

    public String getPlace() {
        return place;
    }

    public String getDistance() {
        return distance;
    }

    public String getBusno() {
        return busno;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setBusno(String busno) {
        this.busno = busno;
    }

    @Override
    public String toString(){
        return place;
    }
}
