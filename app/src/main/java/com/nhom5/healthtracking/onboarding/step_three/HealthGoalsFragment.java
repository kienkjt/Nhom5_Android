package com.nhom5.healthtracking.onboarding.step_three;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.nhom5.healthtracking.HealthTrackingApp;
import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.onboarding.OnboardingActivity;

public class HealthGoalsFragment extends Fragment {

    private HealthGoalsViewModel mViewModel;
    private MaterialButton btnPrevious;
    private MaterialButton btnFinish;

    public static HealthGoalsFragment newInstance() {
        return new HealthGoalsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_health_goals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupListeners();
        
        mViewModel = new ViewModelProvider(this).get(HealthGoalsViewModel.class);
    }

    private void initViews(View view) {
        btnPrevious = view.findViewById(R.id.btn_previous);
        btnFinish = view.findViewById(R.id.btn_finish);
    }

    private void setupListeners() {
        // Both skip and finish buttons complete the onboarding
        btnPrevious.setOnClickListener(v -> completeOnboarding());
        btnFinish.setOnClickListener(v -> completeOnboarding());
    }

    private void completeOnboarding() {
        // Update user onboarding step to complete (step 4)
        HealthTrackingApp app = (HealthTrackingApp) requireActivity().getApplication();
        
        // Update onboarding step in background thread
        new Thread(() -> {
            try {
                User currentUser = app.getUserRepository().getCurrentUser();
                if (currentUser != null) {
                    currentUser.onboardingStep = 4L; // Completed
                    currentUser.updatedAt = new java.util.Date();
                    currentUser.isSynced = false;
                    app.getUserRepository().upsertSync(currentUser);
                    
                    // Show success message on UI thread
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Hoàn thành onboarding thành công!", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Lỗi khi hoàn thành: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }

}