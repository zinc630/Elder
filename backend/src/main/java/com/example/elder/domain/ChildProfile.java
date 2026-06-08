package com.example.elder.domain;

public class ChildProfile {
    private final String childId;
    private final String name;
    private final Integer age;
    private final String gender;
    private final String address;
    private final String relationDesc;

    public ChildProfile(
            String childId,
            String name,
            Integer age,
            String gender,
            String address,
            String relationDesc
    ) {
        this.childId = childId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.address = address;
        this.relationDesc = relationDesc;
    }

    public String getChildId() {
        return childId;
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

    public String getRelationDesc() {
        return relationDesc;
    }
}
