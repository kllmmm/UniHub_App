package com.example.unihub;

public class Event {

    private String date;
    private String time;
    private String description;

    private String user_identifier;

    //Constructor
    public Event(String date, String time, String description,String user_identifier) {
        this.date = date;
        this.time = time;
        this.description = description;
        this.user_identifier = user_identifier;

    }

    //Getters and Setters
    public String getDate() {
        return date;
    }

    public String getUser_identifier(){return user_identifier;}

    public String getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setUser_identifier(String user_identifier){this.user_identifier=user_identifier;}

    public void setTime(String time) {
        this.time = time;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
