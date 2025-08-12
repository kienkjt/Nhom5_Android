package com.nhom5.healthtracking.auth;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.nhom5.healthtracking.MainActivity;
import com.nhom5.healthtracking.R;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import androidx.credentials.Credential;
import androidx.credentials.GetCredentialRequest;

import com.nhom5.healthtracking.util.FirebaseModule;


public class LoginTabFragment extends Fragment {
    private static final String TAG = "LoginTabFragment";
    TextInputEditText emailEditText, passwordEditText;
    TextView forgotPasswordTextView;
    Button loginButton;
    LoginTabViewModel mViewModel;
    AppCompatButton googleSignInButton;
    LinearLayout orSeparator;
    private CredentialManager credentialManager;
    private CancellationSignal cancellationSignal;
    private GetCredentialRequest googleRequest;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_login_tab, container, false);
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
        animateViews();
        setupClickListeners();

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(LoginTabViewModel.class);

        observeViewModel();
        checkIfAlreadyLoggedIn();
    }

    void initViews(ViewGroup root) {
        emailEditText = root.findViewById(R.id.email_edit_text);
        passwordEditText = root.findViewById(R.id.password_edit_text);
        loginButton = root.findViewById(R.id.login_button);
        forgotPasswordTextView = root.findViewById(R.id.forgot_password_text);
        googleSignInButton = root.findViewById(R.id.google_sign_in_button);
        orSeparator = root.findViewById(R.id.or_separator);
    }

    void setupClickListeners() {
        loginButton.setOnClickListener(v -> performLogin());

        forgotPasswordTextView.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Forgot password functionality will be implemented soon", Toast.LENGTH_SHORT).show();
        });

        googleSignInButton.setOnClickListener(v -> beginGoogleSignIn());
    }

    void observeViewModel() {
        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                loginButton.setEnabled(!isLoading);
                loginButton.setText(isLoading ? "Logging in..." : "Login");
            }
        });

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                mViewModel.clearError();
            }
        });

        mViewModel.getLoginSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                clearForm();
                navigateToMainActivity();
            }
        });
    }

    void checkIfAlreadyLoggedIn() {
        if (mViewModel.isUserLoggedIn()) {
            navigateToMainActivity();
        }
    }

    void performLogin() {
        String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";

        mViewModel.loginUser(email, password);
    }

    void navigateToMainActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    void clearForm() {
        emailEditText.setText("");
        passwordEditText.setText("");
    }

    void animateViews() {
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
            mViewModel.loginWithGoogle(googleIdToken);
        } else {
            Log.w(TAG, "Credential is not of type Google ID!");
            Toast.makeText(getContext(), "Đăng nhập Google thất bại: Không lấy được thông tin đăng nhập", Toast.LENGTH_SHORT).show();
        }
    }
}
