package com.example.charles_nfc;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class ShoppingCartItemModel implements Parcelable {
    String name;
    Long quantity;
    Double cost;
    String tag_id;
    String image_url;

    public ShoppingCartItemModel (String tag_id, String name, Long quantity, Double cost, String image_url) {
        this.tag_id = tag_id;
        this.name = name;
        this.quantity = quantity;
        this.cost = cost;
        this.image_url = image_url;
    }

    protected ShoppingCartItemModel(Parcel in) {
        name = in.readString();
        tag_id = in.readString();
        image_url = in.readString();
        quantity = in.readLong();
        cost = in.readDouble();
    }

    @Override
    public String toString() {
        return name + " " + String.valueOf(quantity) + " " + String.valueOf(cost);
    }

    public Map<String,Object> toMap() {
        Map<String,Object> map = new HashMap<>();
        map.put("name", name);
        map.put("tag_id", tag_id);
        map.put("image_url", image_url);
        map.put("quantity", quantity);
        map.put("cost", cost);
        return map;
    }

    public static final Creator<ShoppingCartItemModel> CREATOR = new Creator<ShoppingCartItemModel>() {
        @Override
        public ShoppingCartItemModel createFromParcel(Parcel in) {
            return new ShoppingCartItemModel(in);
        }

        @Override
        public ShoppingCartItemModel[] newArray(int size) {
            return new ShoppingCartItemModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(tag_id);
        parcel.writeString(image_url);
        parcel.writeLong(quantity);
        parcel.writeDouble(cost);
    }

    @Override
    public boolean equals(Object other) {
        ShoppingCartItemModel other_item = (ShoppingCartItemModel) other;
        if (this.name.equals(other_item.name)) {
            return true;
        }
        return false;
    }
}
