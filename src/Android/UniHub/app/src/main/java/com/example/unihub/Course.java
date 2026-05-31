package com.example.unihub;

public class Course {
    private String name;
    private double grade;
    private String courseCode;
    private int semesterId;
    private String courseType;
    private Double ects;
    private Boolean passed;

    private String direction;

    private Boolean declared = true;

    //Constructor
    public Course(String name, double grade) {
        this.name = name;
        this.grade = grade;
    }

    public Course(String name,
                  double grade,
                  String courseCode,
                  int semesterId,
                  String courseType,
                  Double ects,
                  Boolean passed,
                  String direction) {

        this.name = name;
        this.grade = grade;
        this.courseCode = courseCode;
        this.semesterId = semesterId;
        this.courseType = courseType;
        this.ects = ects;
        this.passed = passed;
        this.direction = direction;
    }

    //Setters and Getters
    public String getName() { return name; }
    public double getGrade() { return grade; }

    public String getDirection() {return direction;}

    public String getCourseCode() { return courseCode; }
    public int getSemesterId() { return semesterId; }
    public String getCourseType() { return courseType; }
    public Double getEcts() { return ects; }
    public Boolean getPassed() { return passed; }

    public Boolean getDeclared() { return declared; }

    public void setDeclared(Boolean declared) { this.declared = declared; }

}
