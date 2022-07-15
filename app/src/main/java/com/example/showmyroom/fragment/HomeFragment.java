package com.example.showmyroom.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.showmyroom.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        long curTime = System.currentTimeMillis();
        Date date = new Date(curTime);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String getDate = simpleDateFormat.format(date);
        Date mDate = null;
        try {
            mDate = simpleDateFormat.parse(getDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long timeInMilliseconds = mDate.getTime();
        Log.d(TAG, String.valueOf(timeInMilliseconds/1000));

        return v;
    }
}