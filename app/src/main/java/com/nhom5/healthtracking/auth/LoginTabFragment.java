package com.nhom5.healthtracking.auth;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.textfield.TextInputEditText;
import com.nhom5.healthtracking.MainActivity;
import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.onboarding.OnboardingActivity;
import com.nhom5.healthtracking.util.AuthState;

public class LoginTabFragment extends Fragment {
    private static final String TAG = "LoginTabFragment";
    
    private TextInputEditText emailEditText, passwordEditText;
    private TextView forgotPasswordTextView;
    private Button loginButton;
    private LoginTabViewModel mViewModel;
    private AppCompatButton googleSignInButton;
    private LinearLayout orSeparator;
    private CredentialManager credentialManager;
    private CancellationSignal cancellationSignal;
    private GetCredentialRequest googleRequest;
    private boolean isPerformingLogin = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_login_tab, container, false);
        
        setupGoogleSignIn();
        initViews(root);
        setupClickListeners();

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(LoginTabViewModel.class);
        observeViewModel();
    }

    private void setupGoogleSignIn() {
        credentialManager = CredentialManager.create(requireContext());
        cancellationSignal = new CancellationSignal();
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        googleRequest = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();
    }

    private void initViews(ViewGroup root) {
        emailEditText = root.findViewById(R.id.email_edit_text);
        passwordEditText = root.findViewById(R.id.password_edit_text);
        loginButton = root.findViewById(R.id.login_button);
        forgotPasswordTextView = root.findViewById(R.id.forgot_password_text);
        googleSignInButton = root.findViewById(R.id.google_sign_in_button);
        orSeparator = root.findViewById(R.id.or_separator);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performLogin());

        forgotPasswordTextView.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Forgot password functionality will be implemented soon", Toast.LENGTH_SHORT).show();
        });

        googleSignInButton.setOnClickListener(v -> beginGoogleSignIn());
    }

    private void observeViewModel() {
        // Observe loading state
        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                loginButton.setEnabled(!isLoading);
                googleSignInButton.setEnabled(!isLoading);
                forgotPasswordTextView.setEnabled(!isLoading);
                emailEditText.setEnabled(!isLoading);
                passwordEditText.setEnabled(!isLoading);
                loginButton.setText(isLoading ? "Logging in..." : "Login");
            }
        });

        // Observe error messages
        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                mViewModel.clearError();
            }
        });

        // Observe auth state changes - đây là điểm quan trọng
        mViewModel.getAuthState().observe(getViewLifecycleOwner(), authState -> {
            if (authState.isAuthenticated() && isPerformingLogin) {
                AuthState.Authenticated authenticated = (AuthState.Authenticated) authState;
                User profile = authenticated.getProfile();
                
                Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                clearForm();
                isPerformingLogin = false; // Reset flag
                
                if (profile.hasCompletedOnboarding()) {
                    navigateToMain();
                } else {
                    navigateToOnboarding();
                }
            } else if (authState.isError() && isPerformingLogin) {
                AuthState.Error error = (AuthState.Error) authState;
                Toast.makeText(getContext(), "Authentication error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                isPerformingLogin = false; // Reset flag
            }
        });
    }

    private void performLogin() {
        String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";

        isPerformingLogin = true;
        mViewModel.loginUser(email, password);
    }

    private void beginGoogleSignIn() {
        if (credentialManager == null || googleRequest == null) return;

        credentialManager.getCredentialAsync(
                requireActivity(),
                googleRequest,
                cancellationSignal,
                ContextCompat.getMainExecutor(requireContext()),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse response) {
                        Credential credential = response.getCredential();
                        handleSignIn(credential);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e(TAG, "Google sign-in failed", e);
                        Toast.makeText(getContext(),
                                "Google Sign-In lỗi: " + e.getClass().getSimpleName(),
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
            isPerformingLogin = true;
            mViewModel.loginWithGoogle(googleIdToken);
        } else {
            Log.w(TAG, "Credential is not of type Google ID!");
            Toast.makeText(getContext(), "Đăng nhập Google thất bại: Không lấy được thông tin đăng nhập", Toast.LENGTH_SHORT).show();
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

    private void clearForm() {
        emailEditText.setText("");
        passwordEditText.setText("");
    }

    private void animateViews() {
        float translationX = 800;
        View[] views = {
                emailEditText,
                passwordEditText,
                forgotPasswordTextView,
                loginButton,
                orSeparator,
                googleSignInButton
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
