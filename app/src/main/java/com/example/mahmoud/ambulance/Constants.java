package com.example.mahmoud.ambulance;

import android.content.Context;

import java.text.SimpleDateFormat;

/**
 * Created by Mahmoud on 6/22/2016.
 */
public class Constants {
    final static String Firebase_URL= BuildConfig.UNIQUE_FIREBASE_ROOT_URL;
    public static final String FIREBASE_PROPERTY_TIMESTAMP = "timestamp";
    public static final String FIREBASE_LOCATION_ACTIVE_LISTS = "activeLists";
    public static final String FIREBASE_URL_ACTIVE_LISTS = Firebase_URL + "/" + FIREBASE_LOCATION_ACTIVE_LISTS;
    public static final String FIREBASE_LOCATION_AMBULANCE = "ambulances";
    public static final String FIREBASE_LOCATION_PATIENT = "patients";
    public static final String FIREBASE_LOCATION_ACCIDENT = "accident";


    public static final String FIREBASE_PROPERTY_LIST_STATUS = "status" ;
    public static final String FIREBASE_PROPERTY_USER_POINT = "point";
    public static String user="";
    public static String userType="";
    public static String LeaderId="no";
    public static final String KEY_LIST_ID = "LIST_ID";
    public static final String KEY_USER_ID = "user_id";
    public static final String FIREBASE_LOCATION_SHOPPING_LIST_ITEMS = "comments";
    public static final String FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED = "timestampLastChanged";
    public static final String FIREBASE_URL_SHOPPING_LIST_ITEMS = Firebase_URL + "/" + FIREBASE_LOCATION_SHOPPING_LIST_ITEMS;



    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final String [] POINTS= {"1","3","4","2","5"};
    public static final String [] status= {"append","approved","rejected"};
    }
