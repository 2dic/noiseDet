package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class analysisActivity2 extends AppCompatActivity implements View.OnClickListener {


    Button Anb, Rdb, Cbd;
    DBassist dBassist;
    TextView textView;

    FusedLocationProviderClient client;

    Location my_location;
    Date my_dateTime;

    SimpleDateFormat my_date_format = new SimpleDateFormat("dd.MM.yy HH:mm");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis2);

        Anb = findViewById(R.id.anb);
        Anb.setOnClickListener(this);

        Rdb = findViewById(R.id.rbd);
        Rdb.setOnClickListener(this);

        Cbd = findViewById(R.id.cbd);
        Cbd.setOnClickListener(this);

        dBassist = new DBassist(this);

        textView = findViewById(R.id.textView2);

        client = LocationServices.getFusedLocationProviderClient(this);

        //getLocandDate();

        my_location = MapsActivity.MyNewLoc;


    }

    @Override
    public void onClick(View v) {

        SQLiteDatabase database = dBassist.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        switch (v.getId()){

            case R.id.anb:



                my_dateTime = Calendar.getInstance().getTime();
                    if (my_location != null) {

                        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm dd.MM.yy");
                        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy");

                        String Time = simpleTimeFormat.format(my_dateTime);
                        //String Date = simpleDateFormat.format(my_dateTime);

                        //Date tmp_date = new Date(1648580941098L);



                        contentValues.put(DBassist.KEY_LONGITUDE, my_location.getLongitude());
                        contentValues.put(DBassist.KEY_LATITUDE, my_location.getLatitude());
                        contentValues.put(DBassist.KEY_TIME, Time);
                        contentValues.put(DBassist.KEY_VALUE, "High noise level");

                        database.insert(DBassist.TABLE_CONTACTS, null, contentValues);

                        Toast toast = Toast.makeText(getApplicationContext(), "added", Toast.LENGTH_SHORT);
                        toast.show();

                    }else{
                     Toast toast = Toast.makeText(getApplicationContext(), "location unavailable", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                break;
            case R.id.rbd:
                Cursor cursor = database.query(DBassist.TABLE_CONTACTS, null, null,null,null,null,null);

                if (cursor.moveToFirst()){

                    String logText = "";

                    int idIndex = cursor.getColumnIndex(DBassist.KEY_ID);
                    int lonIndex = cursor.getColumnIndex(DBassist.KEY_LONGITUDE);
                    int latIndex = cursor.getColumnIndex(DBassist.KEY_LATITUDE);
                    int timeIndex = cursor.getColumnIndex(DBassist.KEY_TIME);
                    int valueIndex = cursor.getColumnIndex(DBassist.KEY_VALUE);

                    do {   Log.d("mLog", "ID = " + cursor.getInt(idIndex)+
                                ", lat = "+cursor.getString(latIndex)+
                                ", long = "+cursor.getString(lonIndex)+
                                ", time = " + cursor.getString(timeIndex)+
                                ", date = "+ cursor.getString(valueIndex));

                        String formatlat = new DecimalFormat("#0.000").format(cursor.getDouble(latIndex));
                        String formatlong = new DecimalFormat("#0.000").format(cursor.getDouble(lonIndex));

                        logText += cursor.getInt(idIndex)+" | "
                                + formatlat+" | "
                                + formatlong+" | "
                                + cursor.getString(timeIndex)+" | "
                                + cursor.getString(valueIndex)+"\n";

                    }while (cursor.moveToNext());


                    textView.setText(logText);

                }
                else {
                    Log.d("mLog", "0 rows");
                    textView.setText("0 rows");
                }

                cursor.close();
                break;
            case R.id.cbd:
                database.delete(DBassist.TABLE_CONTACTS, null, null);
                Toast toast = Toast.makeText(getApplicationContext(), "DB cleared", Toast.LENGTH_SHORT);
                toast.show();
                break;
        }
        dBassist.close();


    }

    private void getLocandDate() {
        if (ActivityCompat.checkSelfPermission(analysisActivity2.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                    //if (location != null) {
                    my_location = location;

                    //latitude = location.getLatitude();
                    //longitude = location.getLongitude();

                    // }
                }
            });

            //   try {  task.wait(); } catch (Exception e) { e.printStackTrace(); }

        }else {
            ActivityCompat.requestPermissions(analysisActivity2.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }
}