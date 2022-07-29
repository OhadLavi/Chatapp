package com.example.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ChatActivity;
import com.example.Model.ChatModel;
import com.example.Model.UserModel;
import com.example.Utils;
import com.example.project3.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final String chatID, imageURL;
    private Context context;
    private List<ChatModel> mChat;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    public static final int MSG_TYPE_LEFT = 0; //used to choose the chat bubble: 0 - left_bubble_layout.xml , 1 - right_bubble_layout.xml
    public static final int MSG_TYPE_RIGHT = 1;

    public ChatAdapter(Context context, List<ChatModel> mChat, String imageURL, String chatID) {
        this.context = context;
        this.mChat = mChat;
        this.imageURL = imageURL;
        this.chatID = chatID;
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int resource = viewType == MSG_TYPE_RIGHT ? R.layout.right_bubble_layout : R.layout.left_bubble_layout;
        return new ChatAdapter.ViewHolder(LayoutInflater.from(context).inflate(resource, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        ChatModel chat = mChat.get(position);
        holder.showMessage.setText(chat.getMessage());
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH-mm-ss", Locale.FRENCH);
        Date date = null;
        try {
            date = (Date) Utils.sdf().parse(chat.getDate());
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.FRENCH);
            holder.messageDate.setText(sdf.format(date));
        } catch(Exception e) {
            Toast.makeText(context, "Error setting message times", Toast.LENGTH_SHORT).show();
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setMessage("Do you want to delete this message?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                databaseReference = FirebaseDatabase.getInstance().getReference("Chat").child(chatID);
                                Query query = databaseReference.orderByChild("message").equalTo(chat.getMessage());
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot messageSnapshot: snapshot.getChildren()) {
                                            messageSnapshot.getRef().removeValue();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                                dialog.cancel();
                            }
                        });
                builder1.setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());
                AlertDialog alert11 = builder1.create();
                alert11.show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView showMessage, messageDate, onlineStatus;
        public ImageView friendImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            showMessage = itemView.findViewById(R.id.txtMessage);
            messageDate = itemView.findViewById(R.id.messageDate);
            friendImage = itemView.findViewById(R.id.userImage);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        return mChat.get(position).getSender().equals(firebaseUser.getUid()) ? MSG_TYPE_RIGHT : MSG_TYPE_LEFT;
    }
}