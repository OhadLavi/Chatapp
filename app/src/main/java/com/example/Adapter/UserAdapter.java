package com.example.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ChatActivity;
import com.example.Fragments.Profile;
import com.example.Model.UserModel;
import com.example.ViewModel.ChatsViewModel;
import com.example.project3.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> implements Filterable {

    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private int selectedPos = RecyclerView.NO_POSITION;
    private final Context context;
    private final List<UserModel> mUsers;
    private final List<UserModel> filterArrayList;
    private String lastMsg;
    private String date;
    private final String myID = firebaseUser.getUid();
    private String chatID;
    private final ChatsViewModel chatsViewModel;

    private final Filter contactFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) { //initialize the list of users
            List<UserModel> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0)
                filteredList.addAll(filterArrayList);
            else {
                String filter = constraint.toString().toLowerCase().trim();
                for (UserModel userModel : filterArrayList) {
                    if (userModel.getFirstName().toLowerCase().contains(filter) ||
                            userModel.getLastName().toLowerCase().contains(filter))
                        filteredList.add(userModel);
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mUsers.clear();
            mUsers.addAll((Collection<? extends UserModel>) results.values);
            notifyDataSetChanged();
        }
    };

    public UserAdapter(Context context, List<UserModel> mUsers, ChatsViewModel chatsViewModel) {
        this.context = context;
        this.mUsers = mUsers;
        this.chatsViewModel = chatsViewModel;
        if (this.chatsViewModel.getSelected().getValue() != null) {
            selectedPos = this.chatsViewModel.getSelected().getValue();
            notifyItemChanged(selectedPos);
        }
        filterArrayList = new ArrayList<>();
        filterArrayList.addAll(mUsers);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //inflate a contact row for recycle view
        View view = LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel users = mUsers.get(position); //get user from mUsers list
        chatsViewModel.setItemsCount(mUsers.size()); //set the current number of registered users
        String username = users.getFirstName() + " " + users.getLastName(); //create string with first and last name
        holder.userName.setText(username); //set name to the row widget
        getLastMessage(users, holder); //call method getLastMessage
        if (!users.getImage().equals("")) //load user image to row widget
            Picasso.get().load(users.getImage()).into(holder.userImage);
        holder.itemView.setOnClickListener(new View.OnClickListener() { //listener for click on a contact row
            @Override
            public void onClick(View view) { //loads the chat with the contact clicked
                Intent i = new Intent(context, ChatActivity.class);
                i.putExtra("userID", users.getuID());
                i.putExtra("chatID", chatID);
                context.startActivity(i);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() { //set long click listener for clicking contact row to show user's profile
            @Override
            public boolean onLongClick(View view) { //open contact profile
                chatsViewModel.setPosition(selectedPos);
                if (selectedPos == holder.getAbsoluteAdapterPosition())
                    createProfileFragment(users, R.id.dashboardContainer);
                if (selectedPos > holder.getAbsoluteAdapterPosition())
                    selectedPos -= 1;
                chatsViewModel.setPosition(selectedPos);
                notifyItemChanged(selectedPos);
                notifyDataSetChanged();
                return false;
            }
        });
        holder.userImage.setOnClickListener(view -> createProfileFragment(users, R.id.dashboardContainer)); // On image click show the user's profile
    }

    public void createProfileFragment(UserModel users, int container) { //create and load fragment with contact profile
        Fragment fragment;
        FragmentManager fragmentManager;
        fragment = new Profile();
        fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        Bundle args = new Bundle();
        args.putBoolean("myProfile", false);
        args.putString("userID", users.getuID());
        fragment.setArguments(args);
        fragmentManager.beginTransaction().replace(container, fragment).addToBackStack("AAA").commit();
    }

    @Override
    public int getItemCount() { //return the number of objects of type UserModel in the list mUsers
        return mUsers.size();
    }

    private void getLastMessage(UserModel user, ViewHolder holder) { //get the last message and the time its sent
        lastMsg = date = chatID = null;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myID);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child("member").getValue() != null && dataSnapshot.child("member").getValue().toString().equals(user.getuID())) {
                        lastMsg = dataSnapshot.child("lastMessage").getValue().toString();
                        chatID = dataSnapshot.child("chatListID").getValue().toString();
                        LocalDateTime localTime = LocalDateTime.parse(dataSnapshot.child("date").getValue().toString(), DateTimeFormatter.ofPattern("dd/MM/yyyy HH-mm-ss"));
                        //date = Utils.getMessageDateTimeAgo(localDateTime); //TODO: delete (?)
                        date = String.format(Locale.FRENCH, "%02d:%02d", localTime.getHour(), localTime.getMinute());
                        break;
                    }
                }
                holder.status.setText(lastMsg == null ? ("Status: " + user.getStatus()) : lastMsg); //update contact row with the last message sent in the chat with him (default: if chat empty show contact status sentence)
                holder.dateTextView.setText(lastMsg == null ? "" : date); //update last received message timestamp in the contact row
                lastMsg = date = chatID = null;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public Filter getFilter() {
        return contactFilter;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView userName, status, dateTextView;
        public ImageView userImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.txtUserName);
            status = itemView.findViewById(R.id.txtUserStatus);
            userImage = itemView.findViewById(R.id.userImage);
            dateTextView = itemView.findViewById(R.id.txtChatDate);
        }
    }
}