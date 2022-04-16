package com.example.charles_nfc;

import android.os.SystemClock;
import android.view.View;

//Overriding OnClickListener to only allow click event every 1 second
//Since Firebase Firestore incremental and decremental calls are limited at once per second
public class SingleClickListener implements View.OnClickListener {

    int defaultInterval;
    private long lastClicked = 0;

    public SingleClickListener() {
        this(1000);
    }

    public SingleClickListener(int Interval) {
        this.defaultInterval = Interval;
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - lastClicked < defaultInterval) {
            return;
        }
        lastClicked = SystemClock.elapsedRealtime();
        Click(v);
    }

    public void Click(View v) { };
}
