package com.example.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import com.example.MainActivity;
import com.example.Utils;
import com.example.project3.R;
import com.example.project3.databinding.FragmentUserDataBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserData extends Fragment {
    private FragmentUserDataBinding binding;
    private String firstName, lastName, status, storagePath;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri imageUri;
    private SharedPreferences sharedPreferences;
    private Utils utils;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    pickImage();
                } else {
                    Toast.makeText(getActivity(), "You must grant storage permission", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_user_data, container, false); //inflate fragment_user_data.xml
        View view = binding.getRoot();
        utils = new Utils();
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar); //set toolbar properties
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back_arrow));
        firebaseAuth = FirebaseAuth.getInstance(); //receive an instance to fire base
        databaseReference = FirebaseDatabase.getInstance().getReference("Users"); //get reference to users in fire base
        storageReference = FirebaseStorage.getInstance().getReference();
        storagePath = firebaseAuth.getUid() + "Media/Profile_Image/profile"; //get user profile image path
        sharedPreferences = getContext().getSharedPreferences("UserData", Context.MODE_PRIVATE); //for accessing and modifying preference data
        binding.imagePicker.setOnClickListener(new View.OnClickListener() { //listener for click on the image picker widget
            @Override
            public void onClick(View view) {
                if(utils.isStorageOk(getContext())) // If permission to read data from external storage allowed
                    pickImage();
                else { // Request permission
                    String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
                    if (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_GRANTED) {
                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission))
                        Toast.makeText(getActivity(), "You must grant storage permission", Toast.LENGTH_LONG).show();
                    else
                        requestPermissionLauncher.launch(permission);
                }
            }
        });

        binding.btnDataDone.setOnClickListener(new View.OnClickListener() { //listener for click on the done button
            @Override
            public void onClick(View v) {
                firstName = binding.firstName.getText().toString(); //get first name
                lastName = binding.lastName.getText().toString(); //get last name
                status = binding.status.getText().toString(); //get status
                if (checkTextFields(binding.firstName) && checkTextFields(binding.lastName) &&
                        checkTextFields(binding.status) && checkImage()) //check if data is valid
                    uploadData(); //upload to firebase
            }
        });
        return view;
    }

    private boolean checkTextFields(EditText e) { //return true if text field isn't empty
        if (e.getText().toString().trim().isEmpty()) {
            e.setError("Filed is required");
            return false;
        } else {
            e.setError(null);
            return true;
        }
    }

    private boolean checkImage() { //return true if image uri is valid (not null)
        if (imageUri == null) {
            Toast.makeText(getContext(), "Image is required", Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }

    public static final int PICK_IMAGE = 1;
    private void pickImage() { //create new implicit intent to get image from user
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) { //after user choose image, set the image uri and disable the image picker button
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            binding.imgUser.setImageURI(imageUri);
            binding.imagePicker.setVisibility(View.GONE);
        }
    }

    private void uploadData() { //method to upload data to fire base
        Toast.makeText(getContext(), "Uploading", Toast.LENGTH_SHORT).show();
        getView().findViewById(R.id.progressIndicator).setVisibility(View.VISIBLE);
        storageReference.child(storagePath).putFile(imageUri).addOnSuccessListener(taskSnapshot -> { //upload user image to fire base and receive URL to access it
            Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
            task.addOnCompleteListener(new OnCompleteListener<Uri>() { //listener for image URL received successfully
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    String url = task.getResult().toString(); //convert URL to string
                    Map<String, Object> map = new HashMap<>(); //save information (first and last name,status,image) in HashMap
                    map.put("firstName", firstName);
                    map.put("lastName", lastName);
                    map.put("status", status);
                    map.put("image", url);
                    databaseReference.child(Objects.requireNonNull(firebaseAuth.getUid())).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() { //listener for get reference to user's fields in fire base successfully
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("username", firstName + " " + lastName).apply(); //save user's first and last name in Shared Preferences
                                editor.putString("userImage", url).apply(); //save user's image url in Shared Preferences
                                Intent intent = new Intent(getContext(), MainActivity.class); //crate and start new intent with the dashboard
                                startActivity(intent);
                                getActivity().finish(); //finish current fragment
                            } else
                                Toast.makeText(getContext(), "Failed to upload", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });
    }
}