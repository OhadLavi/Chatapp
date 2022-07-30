package com.example.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.Adapter.UserAdapter;
import com.example.ChatActivity;
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
    public static final int PICK_IMAGE = 1;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    pickImage();
                } else {
                    Toast.makeText(getActivity(), "You must grant storage permission", Toast.LENGTH_LONG).show();
                }
            });
    private TextView profileName, profilePhoneNumber, profileLastSeen, clearConversation;
    private EditText profileFirstNameEditText, profileLastNameEditText, profileStatusEditText;
    private ImageView profileImage, imgProfile, editProfileImage, doneEditProfileImage, uploadPhoto;
    private String storagePath, myID, chatID;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false); //inflate fragment_profile.xml
        firebaseAuth = FirebaseAuth.getInstance(); //receive an instance to fire base
        myID = firebaseAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        utils = new Utils();
        storagePath = firebaseAuth.getUid() + "Media/Profile_Image/profile"; //get user profile image path
        sharedPreferences = getContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        if (getActivity().findViewById(R.id.card) != null)
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
        clearConversation = view.findViewById(R.id.clearConversation);
        clearConversation.setOnClickListener(new View.OnClickListener() { //listener for clicking the clear conversation button
            @Override
            public void onClick(View view) {
                AlertDialog.Builder confirmDialog = new AlertDialog.Builder(new ContextThemeWrapper(view.getContext(), R.style.AlertDialogCustom));
                confirmDialog
                        .setTitle("Chat deleted")
                        .setMessage(String.format("Your chat with %s deleted successfully", user.getFirstName(), user.getLastName()))
                        .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(view.getContext(), R.style.AlertDialogCustom));
                alertDialogBuilder.setTitle("Deleting chat");
                alertDialogBuilder.setCancelable(true);
                alertDialogBuilder
                        .setMessage(String.format("Are you sure that you want to delete the chat with %s %s?", user.getFirstName(), user.getLastName()))
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myID); //reference to fire base chat list that include the current logged in user id
                                    Query query = databaseReference.orderByChild("member").equalTo(user.getuID()); //order the chat list by friend id
                                    query.addValueEventListener(new ValueEventListener() { //listener for data changes
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChildren()) { //find the chat between current logged in user and its friend and read the messages form it
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    String id = snapshot.child("member").getValue().toString();
                                                    if (id.equals(user.getuID())) {
                                                        chatID = snapshot.getKey();
                                                        snapshot.getRef().removeValue();
                                                        FirebaseDatabase.getInstance().getReference("ChatList").child(user.getuID()).child(chatID).removeValue();
                                                        FirebaseDatabase.getInstance().getReference("ChatList").child(myID).child(chatID).removeValue();
                                                        FirebaseDatabase.getInstance().getReference("Chat").child(chatID).removeValue();
                                                        chatID = null;
                                                        if (getContext() != null)
                                                            confirmDialog.show();
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) { }
                                    });
                                } catch (Exception e) {
                                    Log.e("Error", e.toString());
                                }
                            }
                        })
                        .setNegativeButton("No", (dialog, id) -> dialog.cancel());
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                alertDialog.show();
            }
        });
        editProfileImage.setOnClickListener(new View.OnClickListener() { //listener for clicking the profile image (if user want change his image)
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
                uploadPhoto.setOnClickListener(new View.OnClickListener() { //listener for clicking the upload photo
                    @Override
                    public void onClick(View view) {
                        if (utils.isStorageOk(getContext()))
                            pickImage();
                        else
                            checkPermissions(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                });
            }
        });

        doneEditProfileImage.setOnClickListener(new View.OnClickListener() { //listener for clicking the done editing button
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
                if (!profileStatusEditText.getText().toString().equals(user.getStatus()) ||
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
            if (!myProfile) {
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
            } else {
                view.findViewById(R.id.cardPhoneNumber).setVisibility(View.GONE);
                view.findViewById(R.id.cardClearConversation).setVisibility(View.GONE);
                view.findViewById(R.id.cardFirstName).setVisibility(View.VISIBLE);
                view.findViewById(R.id.cardLastName).setVisibility(View.VISIBLE);
            }
        }
        return view;
    }

    private void checkPermissions(String permission) { //check if SMS permission generated
        if (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_GRANTED) {
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission))
            Toast.makeText(getActivity(), "You must grant storage permission", Toast.LENGTH_LONG).show();
        else
            requestPermissionLauncher.launch(permission);
    }

    private void updateData() { //upload users date to fire base
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
                } else
                    Toast.makeText(getContext(), "Failed to update your profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

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
                    if (!myProfile) {
                        String lastSeen = null;
                        try {
                            lastSeen = Utils.getTimeAgo(Long.parseLong(user.getOnline()));
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage().toString());
                        }
                        profileLastSeen.setText(lastSeen == null ? "Online" : "Last seen " + lastSeen);
                        if (!user.getImage().equals("")) {
                            Picasso.get().load(user.getImage()).fit().into(profileImage);
                            Picasso.get().load(user.getImage()).into(imgProfile);
                        }
                    } else {
                        profileLastSeen.setText(profilePhoneNumber.getText().toString());
                        String d = null;
                        if (!sharedPreferences.getString("userImage", d).equals("")) {
                            Picasso.get().load(sharedPreferences.getString("userImage", d)).into(imgProfile);
                            Picasso.get().load(sharedPreferences.getString("userImage", d)).fit().into(profileImage);
                        }
                        else {
                            Picasso.get().load(user.getImage()).fit().into(profileImage);
                            Picasso.get().load(user.getImage()).into(imgProfile);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
}