package com.example.mahmoud.ambulance;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mahmoud.ambulance.manageUser.manageUser;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
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

    private RadioGroup radioAccidentTypeGroup;
    private RadioButton radioAccidentTypeButton;
    EditText descriptionText;
    Button requestButton, acceptButton, rejectButton;
    int markerIcon = R.drawable.ic_accident;
    int markerIconOthers = R.drawable.ic_ambulance;
    Marker markerss;
    DatabaseReference otherUsers;

    Button record, stop, play;
    private MediaRecorder myAudioRecorder;
    private String outputFile = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (servicesOK()) {
            setContentView(R.layout.activity_map);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            radioAccidentTypeGroup = (RadioGroup) findViewById(R.id.type_radio_group);
            descriptionText = (EditText) findViewById(R.id.description_editText);
            requestButton = (Button) findViewById(R.id.request);
            acceptButton = (Button) findViewById(R.id.accept_button);
            rejectButton = (Button) findViewById(R.id.reject_button);
            record = (Button) findViewById(R.id.button1);
            stop = (Button) findViewById(R.id.button2);
            play = (Button) findViewById(R.id.button3);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                /* Get mEncodedEmail and mProvider from SharedPreferences, use null as default value */
            final String userId = sp.getString(Constants.KEY_USER_ID, null);
            Constants.user = userId;
            database = FirebaseDatabase.getInstance();
            final DatabaseReference users = database.getReference();

            users.child("user").child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        userType = user.getType();
                        if (userType.equals(Constants.FIREBASE_LOCATION_PATIENT)) {
                            radioAccidentTypeGroup.setVisibility(View.VISIBLE);
                            descriptionText.setVisibility(View.VISIBLE);
                            requestButton.setVisibility(View.VISIBLE);
                            markerIcon = R.drawable.ic_accident;
                            markerIconOthers = R.drawable.ic_ambulance;
                            markeCurrentLocation();
                        } else {
                            rejectButton.setVisibility(View.VISIBLE);
                            acceptButton.setVisibility(View.VISIBLE);
                            markerIcon = R.drawable.ic_ambulance;
                            markerIconOthers = R.drawable.ic_accident;
                            markeCurrentLocation();
                            Ambulance ambulance = new Ambulance(currentLat, currentLng);
                            (users.child(Constants.FIREBASE_LOCATION_AMBULANCE).child(Constants.user)).setValue(ambulance)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(MainActivity.this, "New User Add Successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(MainActivity.this, "Error : post not add 🙁 ", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                        showOtherUsers(userType);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }

            });

            if (initMap()) {

                // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
                // See https://g.co/AppIndexing/AndroidStudio for more information.
                mLocationClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(AppIndex.API).build();
                mLocationClient.connect();
//                to get current location easy way
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);
            } else {
                Toast.makeText(MainActivity.this, "map not connected!", Toast.LENGTH_SHORT).show();
            }
        } else {
            setContentView(R.layout.activity_main);
        }

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String description = descriptionText.getText().toString().trim();
                if (description.equals("")) {
                    Toast.makeText(MainActivity.this, "Please Write Description!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                int selectedId = radioAccidentTypeGroup.getCheckedRadioButtonId();
                radioAccidentTypeButton = (RadioButton) findViewById(selectedId);
                String accidentType = radioAccidentTypeButton.getText() + "";
                if (accidentType.equals("")) {
                    Toast.makeText(getApplicationContext(), "Choose user Type!", Toast.LENGTH_SHORT).show();
                    return;
                }
                DatabaseReference patients = database.getReference();
                Patient patient = new Patient(description, accidentType, currentLat, currentLng);
                (patients.child(Constants.FIREBASE_LOCATION_PATIENT).child(Constants.user)).push().setValue(patient).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Post Add Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Error : post not add 🙁 ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Accident accident = new Accident(description, accidentType, currentLat, currentLng, Constants.user);
                (patients.child(Constants.FIREBASE_LOCATION_ACCIDENT)).push().setValue(accident).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Post Add Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Error : post not add 🙁 ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Accepted", Toast.LENGTH_SHORT).show();
                repeat();
            }
        });
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                record.setVisibility(View.VISIBLE);
                play.setVisibility(View.VISIBLE);
                stop.setVisibility(View.VISIBLE);
            }
        });

        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";
        ;

        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myAudioRecorder.setOutputFile(outputFile);

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    myAudioRecorder.prepare();
                    myAudioRecorder.start();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                record.setEnabled(false);
                stop.setEnabled(true);

                Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAudioRecorder.stop();
                myAudioRecorder.release();
                myAudioRecorder = null;

                stop.setEnabled(false);
                play.setEnabled(true);

                Toast.makeText(getApplicationContext(), "Audio recorded successfully", Toast.LENGTH_LONG).show();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) throws IllegalArgumentException, SecurityException, IllegalStateException {
                MediaPlayer m = new MediaPlayer();

                try {
                    m.setDataSource(outputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    m.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                m.start();
                Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.map_type_none:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.map_type_satellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.map_type_hybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.map_type_normal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.map_type_terrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
        }

        return super.onOptionsItemSelected(item);
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
            Toast.makeText(MainActivity.this, "Can not Connect to mapping Service", Toast.LENGTH_SHORT).show();
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

            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    List<Address> list = null;
                    try {
                        list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                    Address add = list.get(0);
                    MainActivity.this.addMarker(latLng.latitude, latLng.longitude, add);

                }
            });
        }
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                String message = marker.getTitle() + " ( "
                        + marker.getPosition().latitude + " "
                        + marker.getPosition().longitude + " "
                        + marker.getPosition().latitude + ") ";
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (marker == MainActivity.marker) {
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    List<Address> list = null;
                    LatLng latLng = marker.getPosition();
                    try {
                        list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                    Address add = list.get(0);
                    marker.setTitle(add.getLocality());
                    marker.setSnippet(add.getCountryName());
                    marker.showInfoWindow();
                }
            }
        });

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

    public void geoLocate(View view) throws IOException {
        hideSoftKeyboard(view);
        TextView tv = (TextView) findViewById(R.id.serch_text);
        String searchString = tv.getText().toString();
        GeocodeAsyncTask task = new GeocodeAsyncTask();
        task.execute(searchString);
    }

    private void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    CircleOptions circleOptions;

    public void addMarker(Double lat, Double lng, Address add) {
        currentLng = lng;
        currentLat = lat;
        LatLng latLng = new LatLng(lat, lng);
        String locality = add.getLocality();
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title(locality)
                .icon(BitmapDescriptorFactory.fromResource(markerIcon));
        String country = add.getCountryName();
        if (country.length() > 0) {
            options.snippet(country);
        }
        if (marker == null) {
            marker = mMap.addMarker(options);
            circleOptions = new CircleOptions()
                    .strokeWidth(3)
                    .fillColor(R.color.colorAccent)
                    .strokeColor(R.color.colorPrimary)
                    .center(latLng)
                    .radius(300);


//        } else if (marker2 == null) {
//            marker2 = mMap.addMarker(options);
//            drawLine();
        } else {
            removeEvreyThing();
            marker = mMap.addMarker(options);
        }

    }

    public void useAddMarker(Double latitude, Double longitude) {
        Geocoder geocoder = new Geocoder(MainActivity.this);
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

    public void markeCurrentLocation() {
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean network_enabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location location;
        if (network_enabled) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                gotoLocation(location.getLatitude(), location.getLongitude(), 15);
            }
        }
    }

    public void showOtherUsers(final String userType) {
        if (userType.equals(Constants.FIREBASE_LOCATION_PATIENT)) {
            otherUsers = database.getReference().child(Constants.FIREBASE_LOCATION_AMBULANCE);
        } else {
            otherUsers = database.getReference().child(Constants.FIREBASE_LOCATION_ACCIDENT);
        }

        otherUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (userType.equals(Constants.FIREBASE_LOCATION_PATIENT)) {

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
                } else {
                    HashMap<String, Accident> results = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Accident>>() {
                    });
                    if (results.values() != null) {
                        List<Accident> Accident = new ArrayList<>(results.values());
                        if (markerss != null) {
                            markerss.remove();
                        }
                        for (Accident accident : Accident) {
                            MarkerOptions options = new MarkerOptions()
                                    .position(new LatLng(accident.getLat(), accident.getLng()))
                                    .draggable(false)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_accident));
                            markerss = mMap.addMarker(options);
                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(MainActivity.this, "Ready to map!", Toast.LENGTH_SHORT).show();
        mListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Toast.makeText(MainActivity.this, "Location changed "
                        + location.getLatitude() + " , " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                gotoLocation(location.getLatitude(), location.getLongitude(), 15);
            }
        };
    }

    public void repeat() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(5000);
        request.setFastestInterval(1000);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, request, mListener);

    }

    /*private void drawLine() {
        PolylineOptions polyline = new PolylineOptions()
                .add(marker.getPosition())
                .add(marker2.getPosition());
        line = mMap.addPolyline(polyline);
    }*/

    private void removeEvreyThing() {
        marker.remove();
        marker = null;
//        marker2.remove();
//        marker2 = null;
//        if (line != null) {
//            line.remove();
//            line = null;
//        }
    }

    public void showCurrentLocation(MenuItem item) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location currentLocation = LocationServices.FusedLocationApi
                .getLastLocation(mLocationClient);
        if (currentLocation == null) {
            Toast.makeText(MainActivity.this, "could not connect !", Toast.LENGTH_SHORT).show();
        } else {
            gotoLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 15);
            if (userType.equals(Constants.FIREBASE_LOCATION_AMBULANCE)) {
                Toast.makeText(MainActivity.this, "ambulance location " + currentLocation.getLatitude() + " " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void MyList(MenuItem item) {
        if (userType.equals(Constants.FIREBASE_LOCATION_PATIENT)) {
            startActivity(new Intent(this, MyListActivity.class));
        } else {
            Toast.makeText(MainActivity.this, "No list For Ambulance", Toast.LENGTH_SHORT).show();
        }
    }

    public void manegeUser(MenuItem item) {
        startActivity(new Intent(this, manageUser.class));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, mListener);

    }

    public void clickInfo(View view) {
        Toast.makeText(MainActivity.this, "clicked", Toast.LENGTH_SHORT).show();
    }

    class GeocodeAsyncTask extends AsyncTask<String, Void, Address> {

        String errorMessage = "";

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Address doInBackground(String... searchString) {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

            try {
                List<Address> list = geocoder.getFromLocationName(searchString[0], 1);
                if (list.size() > 0) {
                    Address add = list.get(0);
                    return add;
                }
            } catch (IOException ioException) {
                errorMessage = "Service Not Available";
            } catch (IllegalArgumentException illegalArgumentException) {
                errorMessage = "Invalid Latitude or Longitude Used";
            }

            return null;
        }

        protected void onPostExecute(Address add) {
            if (add == null) {
                Toast.makeText(MainActivity.this, "Not Found", Toast.LENGTH_SHORT).show();
            } else {
                double lat = add.getLatitude();
                double lng = add.getLongitude();
                gotoLocation(lat, lng, 15);
                addMarker(lat, lng, add);
            }
        }
    }

}
