package com.nhom5.healthtracking.onboarding.step_two;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nhom5.healthtracking.R;

public class BodyMetricsFragment extends Fragment {

    private BodyMetricsViewModel mViewModel;

    public static BodyMetricsFragment newInstance() {
        return new BodyMetricsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_body_metrics, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(BodyMetricsViewModel.class);
        // TODO: Use the ViewModel
    }

}