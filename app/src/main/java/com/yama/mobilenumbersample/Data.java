package com.yama.mobilenumbersample;

public class Data {
    private String name;
    private String number;

    public Data(String name, String number) {
        this.name =name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
