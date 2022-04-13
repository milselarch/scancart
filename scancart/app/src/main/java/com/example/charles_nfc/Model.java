package com.example.charles_nfc;

import java.util.ArrayList;
import java.util.List;

public class Model {
    public static class DataSource {
        List<Grocery> data = new ArrayList<Grocery>();
        public DataSource() {
        }
        public DataSource(List<Grocery> data) {
            this.data = data;
        }
        public int count() { return this.data.size(); }
        public Grocery get(int i) { return this.data.get(i); }
    }

    public static class Grocery {
        public String cost;
        public String image_url;
        public String name;
        public String quantity;
        public String tag_id;
        public Grocery(String cost, String image_url, String name, String quantity, String tag_id) {
            this.cost = cost;
            this.image_url = image_url;
            this.name = name;
            this.quantity = quantity;
            this.tag_id = tag_id;
        }

    }
}
