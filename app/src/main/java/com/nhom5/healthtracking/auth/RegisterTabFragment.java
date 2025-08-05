package com.nhom5.healthtracking.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.nhom5.healthtracking.R;

public class RegisterTabFragment extends Fragment {

    TextInputEditText emailEditText, passwordEditText, confirmPasswordEditText;
    CheckBox termsCheckBox;
    Button registerButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_register_tab, container, false);

        initViews(root);
        animateViews();

        return root;
    }

    void initViews(ViewGroup root) {
        emailEditText = root.findViewById(R.id.email_edit_text);
        passwordEditText = root.findViewById(R.id.password_edit_text);
        confirmPasswordEditText = root.findViewById(R.id.confirm_password_edit_text);
        termsCheckBox = root.findViewById(R.id.terms_checkbox);
        registerButton = root.findViewById(R.id.register_button);
    }

    void animateViews() {
        float translationX = -800;
        View[] views = {
                emailEditText,
                passwordEditText,
                confirmPasswordEditText,
                termsCheckBox,
                registerButton
        };
        for (View view : views) {
            view.setTranslationX(translationX);
            view.setAlpha(0);
        }
        int delay = 300;
        for (View view : views) {
            view.animate()
                    .translationX(0)
                    .alpha(1)
                    .setDuration(800)
                    .setStartDelay(delay)
                    .start();
            delay += 100;
        }   
    }
}
