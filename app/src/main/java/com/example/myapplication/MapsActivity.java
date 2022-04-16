package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements View.OnClickListener, LocationListener {


    Button rAnb, Lbtn, loadMarkersbtn, clbtn;
    FusedLocationProviderClient client;
    LocationManager locationManager;
    SupportMapFragment supportMapFragment;

    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm dd.MM.yy");
    String Timedate = simpleTimeFormat.format(calendar.getTime());
    //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
    //String Date = simpleDateFormat.format(calendar.getTime());
    LatLng latLng, matLng;

    static Location MyNewLoc;

    int locationStatusvalue = 0;

    Marker currmarker, redMarker, greenMarker;

    DBassist dBassist2;




    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

       // client = LocationServices.getFusedLocationProviderClient(this);


        Lbtn = findViewById(R.id.lbtn);
        Lbtn.setOnClickListener(this);
        rAnb = findViewById(R.id.ranb);
        rAnb.setOnClickListener(this);
        clbtn = findViewById(R.id.clearbtn);
        clbtn.setOnClickListener(this);

        loadMarkersbtn = findViewById(R.id.loadMarks);
        loadMarkersbtn.setOnClickListener(this);

        dBassist2= new DBassist(this);

      /*  Lbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();


                } else {
                    ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
            }
        });

        Anb.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

            }
        });
*/
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.lbtn:
                locationStatusvalue=0;
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //getCurrentLocation();
                        getMyCurLoc();

                }else{
                    ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);

                }

                break;

            case R.id.loadMarks:

                loading();


                break;

                case R.id.ranb:

                Intent anintent = new Intent(this, analysisActivity2.class);
                startActivity(anintent);

                break;
            case R.id.clearbtn:
                clearing();

                break;
        }


    }

    private void clearing() {
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                //
                googleMap.clear();



            }
    });
    }

    private void loading() {
        SQLiteDatabase database = dBassist2.getReadableDatabase();
        //ContentValues contentValues = new ContentValues();
        Cursor cursor = database.query(DBassist.TABLE_CONTACTS, null, null,null,null,null,null);

        if (cursor.moveToFirst()){

            int idIndex = cursor.getColumnIndex(DBassist.KEY_ID);
            int lonIndex = cursor.getColumnIndex(DBassist.KEY_LONGITUDE);
            int latIndex = cursor.getColumnIndex(DBassist.KEY_LATITUDE);
            int timeIndex = cursor.getColumnIndex(DBassist.KEY_TIME);


            do {
                //matLng = new LatLng(cursor.getDouble(latIndex), cursor.getDouble(lonIndex));
                double lat=cursor.getDouble(latIndex), lon=cursor.getDouble(lonIndex);
                int ide = cursor.getInt(idIndex);
                String tiime =cursor.getString(timeIndex) ;

                supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull GoogleMap googleMap) {

                        matLng = new LatLng(lat, lon);
                        greenMarker = googleMap.addMarker(new MarkerOptions().position(matLng).title("ID: "+ide +" "+tiime)
                                .snippet(" " +"High noise level!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                    }

                });

            }while (cursor.moveToNext());

        }
        else {
        }

        cursor.close();

        dBassist2.close();
    }


    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Task<Location> task = client.getLastLocation();
                task.addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                                @Override
                                public void onMapReady(@NonNull GoogleMap googleMap) {

                                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                    //googleMap.addMarker(new MarkerOptions().position(latLng).title("ID: 1").snippet("Time: "+Time+" Date: "+Date).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                }
                            });

                        }else{
                            Toast toast = Toast.makeText(getApplicationContext(), "location unavailable", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
        } else {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);

        }
            }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //getCurrentLocation();
                getMyCurLoc();

            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getMyCurLoc() {

            try {
                if (latLng == null){
                    locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 5, (LocationListener) MapsActivity.this);
            }
                
                    if ((latLng != null) && (locationStatusvalue == 1)) {
                        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(@NonNull GoogleMap googleMap) {

                            if (currmarker != null){
                                currmarker.remove();
                            }

                                currmarker = googleMap.addMarker(new MarkerOptions().position(latLng).title("current position").snippet("Time: " + Timedate)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

                            }

                        });

                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "waiting for location data", Toast.LENGTH_SHORT);
                        toast.show();
                    }


            } catch (Exception e) {
                e.printStackTrace();
            }


    }


    @Override
    public void onLocationChanged(@NonNull Location location) {



if ((location != null) && (locationStatusvalue == 0))  {
    latLng = new LatLng(location.getLatitude(), location.getLongitude());
    Toast toast = Toast.makeText(getApplicationContext(), "location found", Toast.LENGTH_SHORT);
    toast.show();
    locationStatusvalue = 1;
    MyNewLoc=location;
    getMyCurLoc();
    }


    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}



