package com.example.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.Utils;
import com.example.project3.R;
import com.example.project3.databinding.FragmentGetNumberBinding;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import android.widget.Toast;
import java.util.concurrent.TimeUnit;

public class GetNumber extends Fragment {
    private FragmentGetNumberBinding binding;
    private FirebaseAuth firebaseAuth;
    private String phoneNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_get_number, container, false); //inflate fragment_get_number.xml
        View view = binding.getRoot();
        firebaseAuth = FirebaseAuth.getInstance();
        Utils utils = new Utils();
        view.findViewById(R.id.registerButton).setOnClickListener(new View.OnClickListener() { //listener for click on the register button
            @Override
            public void onClick(View view) {
                phoneNumber = "+972" + binding.phoneNum.getText().toString();
                if (checkPhoneNumber(phoneNumber)) {
                    binding.phoneNumLayout.setVisibility(View.GONE);
                    binding.registerButton.setVisibility(View.GONE);
                    binding.spinKit.setVisibility(View.VISIBLE);
                    sendOTPcode(phoneNumber);
                }
                else {

                }
            }
        });

        view.findViewById(R.id.phoneNum).setOnKeyListener(new View.OnKeyListener() { //listener for enter pressed
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    utils.hideKeyBoard(getActivity(),view);
                    binding.registerButton.performClick(); //call the registerButton on click listener
                    return true;
                }
                return false;
            }
        });

        view.findViewById(R.id.phoneNum).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                //if(!hasFocus) utils.hideKeyBoard(getActivity()); //TODO: delete (?)
            }
        });
        return view;
    }

    private boolean checkPhoneNumber(String phoneNumber) { //return true if phone number is valid
        return android.util.Patterns.PHONE.matcher(phoneNumber).matches();
    }

    private void sendOTPcode(String phoneNum) { //send otp code to users phone number and start the fragment VerifyNumber to check if its valid
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                binding.phoneNumLayout.setVisibility(View.VISIBLE);
                binding.registerButton.setVisibility(View.VISIBLE);
                binding.spinKit.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Sending OTP code failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationID, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Fragment f = new VerifyNumber();
                Bundle bundle = new Bundle();
                bundle.putString("VERIFICATION_CODE", verificationID);
                bundle.putString("phoneNumber", phoneNumber);
                f.setArguments(bundle);
                ft.replace(R.id.LoginContainer, f).addToBackStack("verifyNumberFragment").commit();
            }
        };
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNum)       // Phone number to verify
                        .setTimeout(30L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(getActivity())                 // Activity (for callback binding)
                        .setCallbacks(mCallBack)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}