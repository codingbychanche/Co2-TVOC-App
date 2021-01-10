package com.example.airqualityapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.airqualityapp.R;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    TextView co2View,airQualityView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        co2View=root.findViewById(R.id.co2_level_in_ppm);
        airQualityView=root.findViewById(R.id.air_quality);

        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        restoreDataAndUpdateViews(homeViewModel.getCo2In_PPM(),homeViewModel.getAirQuality());
    }

    private void restoreDataAndUpdateViews(int airQuality, int co2In_PPM){
        co2View.setText(co2In_PPM+"ppm");
        airQualityView.setText(airQuality+"");
    }
}