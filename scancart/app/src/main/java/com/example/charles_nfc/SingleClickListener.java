package com.example.charles_nfc;

import android.os.SystemClock;
import android.view.View;

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
