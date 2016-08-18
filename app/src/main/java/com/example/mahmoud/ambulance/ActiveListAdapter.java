package com.example.mahmoud.ambulance;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.google.android.gms.maps.model.LatLng;

import java.util.List;


/**
 * Created by Mahmoud on 6/22/2016.
 */
public class ActiveListAdapter  extends BaseAdapter {
    Context mContext;
    List<Patient> patients;

    /**
     * Public constructor that initializes private instance variables when adapter is created
     */
    public ActiveListAdapter(Context activity, List<Patient> patients) {
        this.mContext = activity;
        this.patients = patients;
    }

    @Override
    public int getCount() {
        return patients.size();
    }

    @Override
    public Object getItem(int i) {
        return patients.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null){
            view = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.single_active_list,viewGroup,false);
        }
        if (!Constants.user.equals("")) {

            TextView textViewListName = (TextView) view.findViewById(R.id.text_view_list_name);
            TextView timeText = (TextView) view.findViewById(R.id.text_view_edit_time);
            TextView pointText = (TextView) view.findViewById(R.id.points);
            TextView status = (TextView) view.findViewById(R.id.status);

        /* Set the list name and owner */
            LatLng latLng = new LatLng(patients.get(i).getLat(), patients.get(i).getLng());
            textViewListName.setText(patients.get(i).getDescription()+"");
//
//            timeText.setText(Constants.SIMPLE_DATE_FORMAT.format(
//                    new Date(list.getTimestampLastChangedLong())));
//            pointText.setText(list.getPoint());
            status.setText(patients.get(i).getAccidentType()+"");
        }
        return view;
    }
}