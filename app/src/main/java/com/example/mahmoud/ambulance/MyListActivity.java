package com.example.mahmoud.ambulance;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyListActivity extends AppCompatActivity {

    private static ListView mListView;
    private static ActiveListAdapter mActiveListAdapter;
    List<Patient> patients;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);


        /**
         * Create Firebase references
         *
         */
        mListView = (ListView) findViewById(R.id.list_view_active_lists);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference posts = database.getReference().child(Constants.FIREBASE_LOCATION_PATIENT).child(Constants.user);

        posts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                HashMap<String, Patient> results = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Patient>>() {});


                patients = new ArrayList<>(results.values());
                mActiveListAdapter = new ActiveListAdapter(getBaseContext(), patients);
                mListView.setAdapter(mActiveListAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        /**
         * Set interactive bits, such as click events and adapters
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Patient selectedList = patients.get(position);
                if (selectedList != null) {
                    Intent intent = new Intent(getBaseContext(), ActiveListDetailsActivity.class);

                    ActiveListDetailsActivity.patientList = patients.get(position);
                    /* Starts an active showing the details for the selected list */
                    startActivity(intent);
                }
            }
        });

    }
}
