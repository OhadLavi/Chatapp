package com.example;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.example.Adapter.ChatAdapter;
import com.example.Adapter.UserAdapter;
import com.example.Model.ChatListModel;
import com.example.Model.ChatModel;
import com.example.project3.R;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private TextView friendNameTV, onlineStatus;
    private ImageView friendImage;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private String userID, friendNameString, date, myID, friendID, chatID;
    private RecyclerView recyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private UserModel friendModel;
    private Utils utils;
    private ChatAdapter chatAdapter;
    private List<ChatModel> mChat;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat); //inflate activity_chat.xml and set it to be the current view
        context = this;
        if (getSupportActionBar() != null) //hide action bar
            getSupportActionBar().hide();
        getWindow().setNavigationBarColor(getResources().getColor(R.color.app_background)); //set color fir navigation bar
        getWindow().setStatusBarColor(getResources().getColor(R.color.chatBackground)); //set color fir status bar
        friendNameTV = findViewById(R.id.freindName); //define friend name (text view)
        friendImage = findViewById(R.id.freindImage); //define friend image  (image view)
        messageEditText = findViewById(R.id.msgText); //define writing message line (edit text)
        sendButton = findViewById(R.id.btnSend); //define send button (image button)
        onlineStatus = findViewById(R.id.onlineStatus); //define online status (text view)
        recyclerView = findViewById(R.id.recyclerViewMessage); //define chat history recycle view
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true); //set recycle view default position to be from the bottom (allows user to see last messages received first)
        recyclerView.setLayoutManager(linearLayoutManager); //define recycle view to work with liner layout
        utils = new Utils();
        sendButton.setOnClickListener(sendButtonHandleClick); //listener for send button click
        Intent intent = getIntent(); //return the intent that started the activity
        findViewById(R.id.msgBack).setOnClickListener(view -> finish()); //listener for back button click, close the current activity and back to the activity that was before it called

        //if (getIntent().getData() != null)
        userID = intent.getStringExtra("userID"); //get the user id

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser(); //get current logged in user
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userID); //reference to the user in fire base by its id
        databaseReference.addValueEventListener(new ValueEventListener() { //listener for fire base data change
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myID = FirebaseAuth.getInstance().getUid(); //get current logged in user id from fire base
                friendModel = snapshot.getValue(UserModel.class); //get the friend information according the UserModel POJO
                if (friendModel != null) { //check that friend model isn't null
                    friendID = friendModel.getuID(); //get friend id using the getter of the UserModel
                    String lastSeen = null;
                    try {
                        lastSeen = Utils.getTimeAgo(Long.parseLong(friendModel.getOnline()));
                    } catch (Exception e) {
                    } //get the last seen information of the friend and save as string
                    onlineStatus.setText(lastSeen == null ? "Online" : "Last seen " + lastSeen); //set text to online status text view (null = friend online right now else set "last seen XX:XX")
                    friendNameString = friendModel.getFirstName() + " " + friendModel.getLastName(); //get string with the friend first and last name
                    friendNameTV.setText(friendNameString); //set name to be shown in the text view
                    if (!friendModel.getImage().equals(""))
                        Picasso.get().load(friendModel.getImage()).into(friendImage); //load friend's image

                    findViewById(R.id.callFriend).setOnClickListener(view1 -> startActivity(new Intent(Intent.ACTION_DIAL,
                            Uri.parse("tel:" + friendModel.getNumber())))); //define listener for the dial button that will take user to dial screen and set the number to be the friend number

                    findViewById(R.id.toolbar).setOnClickListener(new View.OnClickListener() { //listener for click on the toolbar while in chat with friend
                        @Override
                        public void onClick(View view) { //create profile fragment on the friend with its all details and show it on screen (using createChatFragment method in the UserAdapter)
                            UserAdapter userAdapter = new UserAdapter(context, friendModel);
                            userAdapter.createChatFragment(friendModel, R.id.chatContainer);
                        }
                    });
                    if (intent.hasExtra("chatID") && intent.getStringExtra("chatID") != null) {
                        chatID = intent.getStringExtra("chatID");
                        readMessages(chatID);
                    } else {
                        checkChat(friendID);
                    }
                }
                //checkChat(friendID); //call checkChat method that find the chat of current logged in user and his friend and load the message history from it
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Interface handler onClick for sending message button
    private View.OnClickListener sendButtonHandleClick = new View.OnClickListener(){
        public void onClick(View view) {
            String message = messageEditText.getText().toString(); //convert message to string
            if (!message.equals("")) { //if message not empty
                sendMessage(message); //send message by calling sendMessage method
                utils.hideKeyBoard(((AppCompatActivity)context), view); //hide key board
                messageEditText.setText(""); //initialize the message line to be ready for next message
            }
        }
    };

    private void sendMessage(String message) { //method for sending new message
        if (chatID == null) {
            createChat(message);
        }
        else {
            date =  utils.currentDate(); //get the date of today
            ChatModel messageModel = new ChatModel(myID, friendID, message, date, ""); //create new instance of a message according the ChatModel and initialize the fields: sender, receiver, message, date, type
            databaseReference = FirebaseDatabase.getInstance().getReference("Chat").child(chatID); //get reference to fire base specific chat according its id
            databaseReference.push().setValue(messageModel); //add the new message to the fire base specific chat

            //crate new map object with the last message sent and its date
            Map<String, Object> update = new HashMap<>();
            update.put("lastMessage", message);
            update.put("date", date);
            Toast.makeText(context, date, Toast.LENGTH_SHORT).show();
            //update in fire base both current user chat history and friend chat history with the last message
            databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myID).child(chatID);
            databaseReference.updateChildren(update);
            databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(friendID).child(chatID);
            databaseReference.updateChildren(update);
       }
    }

    private void readMessages(String chatID) { //method for read messages
        mChat = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Chat").child(chatID); //get reference to fire base specific chat according its id
        databaseReference.addValueEventListener(new ValueEventListener() { //listener for data changes
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChat.clear(); //initialize the array list
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) { //for each message in the chat
                    ChatModel chatModel = dataSnapshot.getValue(ChatModel.class);
                    if(chatModel != null && (chatModel.getReceiver().equals(myID) && chatModel.getSender().equals(friendID) ||
                            chatModel.getReceiver().equals(friendID) && chatModel.getSender().equals(myID))) { //check that the message is registered both for the current logged in user and its friend
                        mChat.add(chatModel); //add the message to the array list
                    }
                    chatAdapter = new ChatAdapter(ChatActivity.this, mChat, friendModel.getImage(), chatID); //crate new adapter and initialize it with the context,array list of messages and friend image
                    recyclerView.setAdapter(chatAdapter); //set the recycle view adapter
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void checkChat(final String hisID) {
        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myID); //reference to fire base chat list that include the current logged in user id
        Query query = databaseReference.orderByChild("member").equalTo(hisID); //order the chat list by friend id
        query.addValueEventListener(new ValueEventListener() { //listener for data changes
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) { //find the chat between current logged in user and its friend and read the messages form it
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String id = ds.child("member").getValue().toString();
                        if (id.equals(hisID)) {
                            chatID = ds.getKey();
                            readMessages(chatID);
                            break;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void createChat(String msg) {
        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myID);
        chatID = databaseReference.push().getKey();
        ChatListModel chatListModel = new ChatListModel(chatID, utils.currentDate(), msg, friendID);
        databaseReference.child(chatID).setValue(chatListModel);
        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(friendID);
        ChatListModel chatList = new ChatListModel(chatID, utils.currentDate(), msg, myID);
        databaseReference.child(chatID).setValue(chatList);

        databaseReference = FirebaseDatabase.getInstance().getReference("Chat").child(chatID);
        ChatModel messageModel = new ChatModel(myID, friendID, msg, utils.currentDate(), "text");
        databaseReference.push().setValue(messageModel);
    }

    @Override
    protected void onResume() {
        utils.updateOnlineStatus("online");
        super.onResume();
    }

    @Override
    protected void onPause() {
        utils.updateOnlineStatus(Utils.getTimeAgo(System.currentTimeMillis()));
        super.onPause();
    }
}