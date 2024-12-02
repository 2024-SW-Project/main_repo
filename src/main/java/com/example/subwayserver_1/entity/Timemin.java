package com.example.subwayserver_1.entity;

import jakarta.persistence.*;

@Entity
public class Timemin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "line")
    private String line;

    @Column(name = "departure")
    private String departure;

    @Column(name = "arrival")
    private String arrival;

    @Column(name = "weight")
    private int weight;

    @Column(name = "express")
    private boolean express;

    public boolean isExpress() {
        return express;
    }

    public void setExpress(boolean express) {
        this.express = express;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLine() { return line; }
    public void setLine(String line) { this.line = line; }

    public String getDeparture() { return departure; }
    public void setDeparture(String departure) { this.departure = departure; }

    public String getArrival() { return arrival; }
    public void setArrival(String arrival) { this.arrival = arrival; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
}
