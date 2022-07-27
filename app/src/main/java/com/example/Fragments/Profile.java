package com.example.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.Adapter.UserAdapter;
import com.example.ChatActivity;
import com.example.Dashboard;
import com.example.Model.UserModel;
import com.example.Utils;
import com.example.project3.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Map;
import java.util.Objects;

public class Profile extends Fragment {
    private TextView profileName, profilePhoneNumber, profileLastSeen;
    private EditText  profileFirstNameEditText, profileLastNameEditText, profileStatusEditText;
    private ImageView profileImage, imgProfile, editProfileImage, doneEditProfileImage, uploadPhoto;
    private String storagePath;
    private UserModel user;
    private UserAdapter userAdapter;
    private boolean myProfile;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private Utils utils;
    private Uri imageUri;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false); //inflate fragment_profile.xml
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
//        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
//        LayoutInflater inflator = (LayoutInflater) (LocationManager)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View v = inflator.inflate(R.layout.custom_actionbar, null);
//        actionBar.setCustomView(v);
        //((AppCompatActivity) getActivity()).getSupportActionBar().hide();
//        ((AppCompatActivity) getActivity()).setSupportActionBar(view.findViewById(R.id.toolbar));
        //((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("");
        firebaseAuth = FirebaseAuth.getInstance(); //receive an instance to fire base
        storageReference = FirebaseStorage.getInstance().getReference();
        utils = new Utils();
        storagePath = firebaseAuth.getUid() + "Media/Profile_Image/profile"; //get user profile image path
        sharedPreferences = getContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        if(getActivity().findViewById(R.id.card) != null)
            getActivity().findViewById(R.id.card).setVisibility(View.GONE);
        profileName = view.findViewById(R.id.profileName);
        profilePhoneNumber = view.findViewById(R.id.profilePhoneNumber);
        profileLastSeen = view.findViewById(R.id.profileLastSeen);
        profileImage = view.findViewById(R.id.profileImage);
        imgProfile = view.findViewById(R.id.imgProfile);
        editProfileImage = view.findViewById(R.id.editProfileDetails);
        doneEditProfileImage = view.findViewById(R.id.DoneEditingProfileDetails);
        uploadPhoto = view.findViewById(R.id.uploadPhoto);

        profileFirstNameEditText = view.findViewById(R.id.profileFirstNameEdit);
        profileLastNameEditText = view.findViewById(R.id.profileLastNameEdit);
        profileStatusEditText = view.findViewById(R.id.profileStatusEdit);
        profileFirstNameEditText.setInputType(InputType.TYPE_NULL);
        profileLastNameEditText.setInputType(InputType.TYPE_NULL);
        profileStatusEditText.setInputType(InputType.TYPE_NULL);

        uploadPhoto.setVisibility(View.GONE);
        doneEditProfileImage.setVisibility(View.GONE);
        editProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editProfileImage.setVisibility(View.GONE);
                doneEditProfileImage.setVisibility(View.VISIBLE);
                uploadPhoto.setVisibility(View.VISIBLE);
                profileFirstNameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                profileLastNameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                profileStatusEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                profileFirstNameEditText.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.black), PorterDuff.Mode.SRC_IN);
                profileLastNameEditText.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.black), PorterDuff.Mode.SRC_IN);
                profileStatusEditText.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.black), PorterDuff.Mode.SRC_IN);
                profileFirstNameEditText.setTextAppearance(R.style.EditTextStyleEdited);
                profileLastNameEditText.setTextAppearance(R.style.EditTextStyleEdited);
                profileStatusEditText.setTextAppearance(R.style.EditTextStyleEdited);
                uploadPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(utils.isStorageOk(getContext()))
                            pickImage();
                        else {
                            utils.requestStorage(getActivity());
                            if(utils.isStorageOk(getContext()))
                                pickImage();
                        }
                    }
                });
            }
        });

        doneEditProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileFirstNameEditText.setInputType(InputType.TYPE_NULL);
                profileLastNameEditText.setInputType(InputType.TYPE_NULL);
                profileStatusEditText.setInputType(InputType.TYPE_NULL);
                profileFirstNameEditText.setTextAppearance(R.style.EditTextStyle);
                profileLastNameEditText.setTextAppearance(R.style.EditTextStyle);
                profileStatusEditText.setTextAppearance(R.style.EditTextStyle);
                profileFirstNameEditText.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.transparent), PorterDuff.Mode.SRC_IN);
                profileLastNameEditText.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.transparent), PorterDuff.Mode.SRC_IN);
                profileStatusEditText.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.transparent), PorterDuff.Mode.SRC_IN);
                editProfileImage.setVisibility(View.VISIBLE);
                doneEditProfileImage.setVisibility(View.GONE);
                uploadPhoto.setVisibility(View.GONE);
                if (!profileStatusEditText.getText().toString().equals(user.getStatus())       ||
                    !profileFirstNameEditText.getText().toString().equals(user.getFirstName()) ||
                    !profileLastNameEditText.getText().toString().equals(user.getLastName())) {
                    if (checkImage()) {
                        Toast.makeText(getContext(), "Updating your profile...", Toast.LENGTH_SHORT).show();
                        storageReference.child(storagePath).putFile(imageUri).addOnSuccessListener(taskSnapshot -> { //upload user image to fire base and receive URL to access it
                            Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                            task.addOnCompleteListener(new OnCompleteListener<Uri>() { //listener for image URL received successfully
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    user.setImage(task.getResult().toString());
                                    updateData();
                                }
                            });
                        });
                    } else
                        updateData();
                }
            }
        });

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            getUserDetail(bundle.getString("userID")); //call method getUserDetail with userID string
            myProfile = bundle.getBoolean("myProfile");
            if(!myProfile) {
                view.findViewById(R.id.callFriend).setOnClickListener(view1 -> startActivity(new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + profilePhoneNumber.getText())))); //listener for click on the call button - if clicked a dial action to the friend number will be started
                view.findViewById(R.id.sendMessage).setOnClickListener(new View.OnClickListener() { //listener for click on the send message button - if clicked a chat with the friend will be started
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(getContext(), ChatActivity.class);
                        i.putExtra("userID", user.getuID());
                        getContext().startActivity(i);
                    }
                });
                view.findViewById(R.id.cardPhoneNumber).setVisibility(View.VISIBLE);
                view.findViewById(R.id.cardClearConversation).setVisibility(View.VISIBLE);
                view.findViewById(R.id.cardFirstName).setVisibility(View.GONE);
                view.findViewById(R.id.cardLastName).setVisibility(View.GONE);
                editProfileImage.setVisibility(View.GONE);
            }
            else {
                view.findViewById(R.id.cardPhoneNumber).setVisibility(View.GONE);
                view.findViewById(R.id.cardClearConversation).setVisibility(View.GONE);
                view.findViewById(R.id.cardFirstName).setVisibility(View.VISIBLE);
                view.findViewById(R.id.cardLastName).setVisibility(View.VISIBLE);
            }
        }
        return view;
    }

    private void updateData() {
        user.setFirstName(profileFirstNameEditText.getText().toString());
        user.setLastName(profileLastNameEditText.getText().toString());
        user.setStatus(profileStatusEditText.getText().toString());
        Map<String, Object> values = user.toMap();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(Objects.requireNonNull(user.getuID())).updateChildren(values).addOnCompleteListener(new OnCompleteListener<Void>() { //listener for get reference to user's fields in fire base successfully
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                utils.hideKeyBoard(getActivity(), getView());
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Your profile has been updated", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", user.getFirstName() + " " + user.getLastName()).apply(); //save user's first and last name in Shared Preferences
                    editor.putString("userImage", user.getImage()).apply(); //save user's image url in Shared Preferences
                }
                else
                    Toast.makeText(getContext(), "Failed to update your profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static final int PICK_IMAGE = 1;
    private void pickImage() { //create new implicit intent to get image from user
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    private boolean checkImage() { //return true if image uri is valid (not null)
        return imageUri != null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) { //after user choose image, set the image uri and disable the image picker button
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            if (checkImage()) {
                imgProfile.setImageURI(imageUri);
                profileImage.setImageURI(imageUri);
            }
        }
    }

    private void getUserDetail(String uID) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uID); //reference to user's information in fire base
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) { //get the information from fire base and update the widgets with it
                    user = dataSnapshot.getValue(UserModel.class);
                    profileName.setText(user.getFirstName() + " " + user.getLastName());
                    profileFirstNameEditText.setText(user.getFirstName());
                    profileLastNameEditText.setText(user.getLastName());
                    profilePhoneNumber.setText(user.getNumber());
                    profileStatusEditText.setText(user.getStatus());
                    if(!myProfile) {
                        String lastSeen = null;
                        if (user.getOnline().equals("Online"))
                            lastSeen = Utils.getTimeAgo(Long.parseLong(user.getOnline()));
                        profileLastSeen.setText(lastSeen == null ? "Online" : "Last seen " + lastSeen);
                        if (!user.getImage().equals("")) {
                            Picasso.get().load(user.getImage()).fit().into(profileImage);
                            Picasso.get().load(user.getImage()).into(imgProfile);
                        }
                    }
                    else {
                        profileLastSeen.setText(profilePhoneNumber.getText().toString());
                        String d = null;
                        Picasso.get().load(sharedPreferences.getString("userImage", d)).into(imgProfile);
                        Picasso.get().load(sharedPreferences.getString("userImage", d)).fit().into(profileImage);
                        //Toast.makeText(getContext(), sharedPreferences.getString("userImage", d), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
}