package com.example.charles_nfc;

public class Grocery {

    private String cost;
    private String image_url;
    private String name;
    private String quantity;
    private String tag_id;


    public Grocery(){
    }

    public Grocery(String cost, String image_url, String name, String quantity, String tag_id) {
        this.cost = cost;
        this.image_url = image_url;
        this.name = name;
        this.quantity = quantity;
        this.tag_id = tag_id;
    }
}
