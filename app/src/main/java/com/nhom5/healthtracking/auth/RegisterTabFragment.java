package com.nhom5.healthtracking.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.nhom5.healthtracking.MainActivity;
import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.onboarding.OnboardingActivity;
import com.nhom5.healthtracking.util.AuthState;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import androidx.credentials.Credential;

public class RegisterTabFragment extends Fragment {
    private static final String TAG = "RegisterTabFragment";

    TextInputEditText emailEditText, passwordEditText, confirmPasswordEditText;
    CheckBox termsCheckBox;
    Button registerButton;
    RegisterTabViewModel mViewModel;
    AppCompatButton googleSignUpButton;
    LinearLayout orSeparator;
    private CredentialManager credentialManager;
    private CancellationSignal cancellationSignal;
    private GetCredentialRequest googleRequest;
    private boolean isPerformingRegistration = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_register_tab, container, false);
        credentialManager = CredentialManager.create(requireContext());
        cancellationSignal = new CancellationSignal();
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        googleRequest = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        initViews(root);
        setupClickListeners();

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(RegisterTabViewModel.class);
        
        observeViewModel();
    }

    void initViews(ViewGroup root) {
        emailEditText = root.findViewById(R.id.email_edit_text);
        passwordEditText = root.findViewById(R.id.password_edit_text);
        confirmPasswordEditText = root.findViewById(R.id.confirm_password_edit_text);
        termsCheckBox = root.findViewById(R.id.terms_checkbox);
        registerButton = root.findViewById(R.id.register_button);
        googleSignUpButton = root.findViewById(R.id.google_sign_up_button);
        orSeparator = root.findViewById(R.id.or_separator);
    }

    void setupClickListeners() {
        registerButton.setOnClickListener(v -> performRegistration());
        googleSignUpButton.setOnClickListener(v -> beginGoogleSignIn());
    }

    void observeViewModel() {
        // Observe loading state
        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                registerButton.setEnabled(!isLoading);
                googleSignUpButton.setEnabled(!isLoading);
                emailEditText.setEnabled(!isLoading);
                passwordEditText.setEnabled(!isLoading);
                confirmPasswordEditText.setEnabled(!isLoading);
                termsCheckBox.setEnabled(!isLoading);
                registerButton.setText(isLoading ? "Registering..." : "Register");
            }
        });

        // Observe error messages
        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                mViewModel.clearError();
            }
        });

        mViewModel.getAuthState().observe(getViewLifecycleOwner(), authState -> {
            if (authState.isAuthenticated() && isPerformingRegistration) {
                AuthState.Authenticated authenticated = (AuthState.Authenticated) authState;
                User profile = authenticated.getProfile();
                
                Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                clearForm();
                isPerformingRegistration = false; // Reset flag
                
                if (profile.hasCompletedOnboarding()) {
                    navigateToMain();
                } else {
                    navigateToOnboarding();
                }
            } else if (authState instanceof AuthState.Error && isPerformingRegistration) {
                AuthState.Error error = (AuthState.Error) authState;
                Toast.makeText(getContext(), "Authentication error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                isPerformingRegistration = false; // Reset flag
            }
        });
    }

    void performRegistration() {
        String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";
        String confirmPassword = confirmPasswordEditText.getText() != null ? confirmPasswordEditText.getText().toString() : "";
        boolean acceptedTerms = termsCheckBox.isChecked();

        isPerformingRegistration = true;
        mViewModel.registerUser(email, password, confirmPassword, acceptedTerms);
    }

    void clearForm() {
        emailEditText.setText("");
        passwordEditText.setText("");
        confirmPasswordEditText.setText("");
        termsCheckBox.setChecked(false);
    }

    void animateViews() {
        float translationX = -800;
        View[] views = {
                emailEditText,
                passwordEditText,
                confirmPasswordEditText,
                termsCheckBox,
                registerButton,
                orSeparator,
                googleSignUpButton
        };
        for (View view : views) {
            view.setTranslationX(translationX);
            view.setAlpha(0);
        }
        int delay = 0;
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

    private void navigateToOnboarding() {
        Intent intent = new Intent(getActivity(), OnboardingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void beginGoogleSignIn() {
        if (credentialManager == null || googleRequest == null) return;

        credentialManager.getCredentialAsync(
                /* activity */ requireActivity(),
                /* request  */ googleRequest,
                /* cancel   */ cancellationSignal,
                /* executor */ ContextCompat.getMainExecutor(requireContext()),
                /* callback */ new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse response) {
                        Credential credential = response.getCredential();
                        handleSignIn(credential); // bạn đã có sẵn hàm này
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e(TAG, "Google register failed", e);
                        Toast.makeText(getContext(),
                                "Đăng ký bằng Google thất bại: " + e.getClass().getSimpleName(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void handleSignIn(Credential credential) {
        if (credential instanceof CustomCredential &&
                credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {

            CustomCredential customCredential = (CustomCredential) credential;

            Bundle credentialData = customCredential.getData();
            GoogleIdTokenCredential googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credentialData);

            String googleIdToken = googleIdTokenCredential.getIdToken();
            isPerformingRegistration = true;
            mViewModel.registerWithGoogle(googleIdToken);
        } else {
            Toast.makeText(getContext(), "Đăng ký Google thất bại: Không lấy được thông tin đăng ký", Toast.LENGTH_SHORT).show();
        }
    }
}
