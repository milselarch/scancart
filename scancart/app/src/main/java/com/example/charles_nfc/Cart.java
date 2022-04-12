package com.example.charles_nfc;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class Cart extends Fragment {
    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        Log.d("FRAGMENT_CREATE", "cart");

        return inflater.inflate(
            R.layout.fragment_cart, container, false
        );
    }
}