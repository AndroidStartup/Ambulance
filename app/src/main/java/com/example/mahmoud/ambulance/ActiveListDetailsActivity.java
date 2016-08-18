package com.example.mahmoud.ambulance;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActiveListDetailsActivity extends AppCompatActivity {
    GoogleMap mMap;
    private static final int ERROR_DIALOG_REQUSET = 9001;
    private GoogleApiClient mLocationClient;
    private LocationListener mListener;
    static Marker marker;
    //            , marker2;
    Polyline line;

    String userType = "";
    Double currentLat, currentLng;
    FirebaseDatabase database;

    int markerIcon = R.drawable.ic_accident;
    int markerIconOthers = R.drawable.ic_ambulance;
    Marker markerss;
    DatabaseReference otherUsers;
    public static Patient patientList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (servicesOK()) {
            setContentView(R.layout.activity_active_list_details);
            database = FirebaseDatabase.getInstance();
            userType = Constants.FIREBASE_LOCATION_PATIENT;

            if (initMap()) {
                useAddMarker(patientList.getLat(), patientList.getLng());
                showOtherUsers(userType);
            } else {
                Toast.makeText(ActiveListDetailsActivity.this, "map not connected!", Toast.LENGTH_SHORT).show();
            }
        } else {
            setContentView(R.layout.activity_main);
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }


    public boolean servicesOK() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int isAvailable = googleAPI.isGooglePlayServicesAvailable(this);
//        int isAvailable = GoogleApiAvailability.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, ERROR_DIALOG_REQUSET);
            dialog.show();
        } else {
            Toast.makeText(ActiveListDetailsActivity.this, "Can not Connect to mapping Service", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public boolean initMap() {
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mMap = mapFragment.getMap();
        }
        if (mMap != null) {
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    View v = getLayoutInflater().inflate(R.layout.info_window, null);
                    ImageView image = (ImageView) v.findViewById(R.id.image);
                    TextView tvLocality = (TextView) v.findViewById(R.id.locality_txt);
                    TextView tvLat = (TextView) v.findViewById(R.id.lat_txt);
                    TextView tvLng = (TextView) v.findViewById(R.id.lng_txt);
                    TextView tvsnippet = (TextView) v.findViewById(R.id.snippet_txt);


                    LatLng latLng = marker.getPosition();
                    tvLocality.setText(marker.getTitle());
                    image.setImageResource(R.drawable.flag);
                    tvLat.setText(latLng.latitude + "");
                    tvLng.setText(latLng.longitude + "");
                    tvsnippet.setText(marker.getSnippet());

                    return v;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    return null;
                }
            });


        }


        return (mMap != null);
    }

    public void gotoLocation(double lat, double lng, float zoom) {
        currentLng = lng;
        currentLat = lat;
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);

        useAddMarker(lat, lng);
//        MarkerOptions options = new MarkerOptions()
//                .position(latLng)
//                .title("Location " + lat + lng)
//                .icon(BitmapDescriptorFactory.fromResource(markerIcon));
//        if (marker != null) {
//            marker.remove();
//        }
//        marker = mMap.addMarker(options);

    }


    public void addMarker(Double lat, Double lng, Address add) {
        currentLng = lng;
        currentLat = lat;
        LatLng latLng = new LatLng(lat, lng);
        String locality = add.getLocality();
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title(locality)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_accident));
        String country = add.getCountryName();
        if (country.length() > 0) {
            options.snippet(country);
        }
        if (marker == null) {
            marker = mMap.addMarker(options);
        } else {
            removeEvreyThing();
            marker = mMap.addMarker(options);
        }

    }

    public void useAddMarker(Double latitude, Double longitude) {
        Geocoder geocoder = new Geocoder(ActiveListDetailsActivity.this);
        List<Address> list = null;
        try {
            list = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Address add = list.get(0);
        addMarker(latitude, longitude, add);
    }

    public void showOtherUsers(final String userType) {
        otherUsers = database.getReference(Constants.FIREBASE_LOCATION_AMBULANCE);

        otherUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Ambulance> results = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Ambulance>>() {
                });
                if (results.values() != null) {
                    List<Ambulance> Ambulances = new ArrayList<>(results.values());
                    if (markerss != null) {
                        markerss.remove();
                    }
                    for (Ambulance ambulance : Ambulances) {
                        MarkerOptions options = new MarkerOptions()
                                .position(new LatLng(ambulance.getLat(), ambulance.getLng()))
                                .draggable(false)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ambulance));

                        markerss = mMap.addMarker(options);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void removeEvreyThing() {
        marker.remove();
        marker = null;
    }



}
