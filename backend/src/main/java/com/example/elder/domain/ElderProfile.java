package com.example.elder.domain;

public class ElderProfile {
    private final String elderId;
    private final String name;
    private final Integer age;
    private final String gender;
    private final String address;

    // Optional medical notes for display/demo.
    private final String keyHealthNotes;

    public ElderProfile(String elderId, String name, Integer age, String gender, String address, String keyHealthNotes) {
        this.elderId = elderId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.address = address;
        this.keyHealthNotes = keyHealthNotes;
    }

    public String getElderId() {
        return elderId;
    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getAddress() {
        return address;
    }

    public String getKeyHealthNotes() {
        return keyHealthNotes;
    }
}

