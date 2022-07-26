package com.example.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ChatActivity;
import com.example.Fragments.Profile;
import com.example.Model.UserModel;
import com.example.project3.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<UserModel> mUsers;

    public UserAdapter(Context context, List<UserModel> mUsers) {
        this.context = context;
        this.mUsers = mUsers;
    }

    public UserAdapter(Context context, UserModel mUsers) {
        this.context = context;
        this.mUsers = new ArrayList<>();
        this.mUsers.add(mUsers);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_item,parent,false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel users = mUsers.get(position);
        holder.userName.setText(users.getFirstName()+" " + users.getLastName());
        holder.status.setText(users.getStatus());
        if (!users.getImage().equals(""))
            Picasso.get().load(users.getImage()).into(holder.userImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, ChatActivity.class);
                i.putExtra("userID", users.getuID());
                context.startActivity(i);
            }
        });

        holder.userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createChatFragment(users, R.id.dashboardContainer);
            }
        });
    }

    public void createChatFragment(UserModel users, int container) {
        Fragment fragment;
        FragmentManager fragmentManager;
        fragment = new Profile();
        fragmentManager =   ((AppCompatActivity)context).getSupportFragmentManager();
        Bundle args = new Bundle();
        args.putBoolean("myProfile", false);
        args.putString("userID", users.getuID());
        fragment.setArguments(args);
        fragmentManager.beginTransaction().replace(container, fragment).addToBackStack("AAA").commit();
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView userName, status;
        public ImageView userImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.txtUserName);
            status = itemView.findViewById(R.id.txtUserStatus);
            userImage = itemView.findViewById(R.id.userImage);
        }
    }
}
