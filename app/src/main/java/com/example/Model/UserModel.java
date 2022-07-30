package com.example.Model;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

//user POJO includes the data of a user (name,picture,number etc).
public class UserModel {
    String firstName, lastName, image, number, uID, online, status;

    public UserModel() {
    }

    public UserModel(String firstName, String lastName, String image, String number, String uID, String online, String typing, String status, String token) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.image = image;
        this.number = number;
        this.uID = uID;
        this.online = online;
        this.status = status;
    }

    @BindingAdapter("bind:imageUrl")
    public static void loadImage(@NonNull CircleImageView view, @NonNull String image) {
        Picasso.get().load(image).into(view);
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    } //TODO: delete (?)

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    } //TODO: delete (?)

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("firstName", firstName);
        map.put("lastName", lastName);
        map.put("status", status);
        map.put("image", image);
        return map;
    }
}