package com.example.a1d_shoppingcart_ryan;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class SelectTiming extends AppCompatActivity implements View.OnClickListener{
    Button button_time, button_date, return_checkout, confirm_timing;
    TextView text_time,text_date;
    private int year, month, day, hour, minute;
    public String str_date,str_time;
    public final static String date_key="date", time_key ="time";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_timing);
        button_date=(Button) findViewById(R.id.choose_date);
        button_time=(Button) findViewById(R.id.choose_time);
        return_checkout=(Button) findViewById(R.id.return_checkout);
        text_date = (TextView) findViewById(R.id.date);
        text_time = (TextView) findViewById(R.id.time);
        confirm_timing = (Button) findViewById(R.id.set_time);
        button_date.setOnClickListener(this);
        button_time.setOnClickListener(this);
        return_checkout.setOnClickListener(this);
        confirm_timing.setOnClickListener(this);

        text_date.setText("Date: ");
        text_time.setText("Time: ");
    }

    @Override
    public void onClick(View view) {
        if (view == button_date){
            final Calendar date = Calendar.getInstance();
            year = date.get(Calendar.YEAR);
            month = date.get(Calendar.MONTH);
            day = date.get(Calendar.DATE);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDateSet(DatePicker view, int text_year, int text_month, int text_day){
                    text_date.setText("Date: " + String.format("%02d",text_day) + "-" + String.format("%02d",(text_month+1)) + "-" + String.format("%04d",text_year));
                    str_date=text_date.getText().toString();
                }

            },year, month,day);
            datePickerDialog.show();
        }
        if (view == button_time){
            final Calendar time = Calendar.getInstance();
            hour = time.get(Calendar.HOUR_OF_DAY);
            minute = time.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onTimeSet(TimePicker view, int text_hour, int text_minute){
                    text_time.setText("Time: " + String.format("%02d",text_hour) + ":" + String.format("%02d",text_minute));
                    str_time=text_time.getText().toString();

                }

            },hour, minute,false);
            timePickerDialog.show();
        }

        if (view == return_checkout){
            Intent returnIntent = new Intent(SelectTiming.this, Checkout.class);
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
        }

        if (view == confirm_timing){
            if (str_date == null && str_time == null) {
                Toast.makeText(
                        getApplicationContext(),
                        "Date and Time not selected",
                        Toast.LENGTH_SHORT
                ).show();
            } else if (str_date == null)  {
                Toast.makeText(
                        getApplicationContext(),
                        "Date not selected",
                        Toast.LENGTH_SHORT
                ).show();
            } else if (str_time == null)  {
                Toast.makeText(
                        getApplicationContext(),
                        "Time not selected",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                Intent returnIntent = new Intent(SelectTiming.this, Checkout.class);
                returnIntent.putExtra(date_key, str_date);
                returnIntent.putExtra(time_key, str_time);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        }
    }

}
