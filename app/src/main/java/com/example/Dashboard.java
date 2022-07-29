package com.example;

import static androidx.appcompat.R.id.search_close_btn;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.Adapter.UserAdapter;
import com.example.Fragments.Profile;
import com.example.Model.ChatListModel;
import com.example.Model.UserModel;
import com.example.project3.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class Dashboard extends AppCompatActivity implements SearchView.OnQueryTextListener, LifecycleOwner {

    private Observer<ArrayList<ChatListModel>> userListUpdateObserver;
    private ChatsViewModel chatsViewModel;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<UserModel> mUsers;
    private String myID;
    private Utils utils;
    private Context context;

    @Override
    public void onBackPressed() { //methode to deal with back button press
        //Toast.makeText(this, "test", Toast.LENGTH_SHORT).show(); //TODO: delete
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            chatsViewModel.getItemsCount().observe(this, itemsNum -> { getSupportActionBar().setTitle("Messages (" + String.valueOf(itemsNum)+ ")"); });
            findViewById(R.id.card).setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard); //load the activity_dashboard.xml as view
        chatsViewModel = new ViewModelProvider(this).get(ChatsViewModel.class);
        chatsViewModel.getItemsCount().observe(this, itemsNum -> { getSupportActionBar().setTitle("Messages (" + String.valueOf(itemsNum)+ ")"); });
        getSupportActionBar().setTitle("Messages"); //change action bar title and preferences
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle(Html.fromHtml("<big><font color=\"white\">Messages</big>", Html.FROM_HTML_MODE_LEGACY));
        recyclerView = findViewById(R.id.recyclerViewContact); //define the recycle view for the contacts list
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); //define recycle view to use linear layout
        mUsers = new ArrayList<>(); //crete an array list of users according the UserModel POJO
        SearchView searchView = findViewById(R.id.contactSearchView); //define the search bar on the top of the dashboard view
        SearchView.SearchAutoComplete theTextArea = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchView.setOnQueryTextListener(this);
        theTextArea.setTextColor(getResources().getColor(R.color.white));
//        theTextArea.setTextCursorDrawable(R.color.white); //TODO: delete (?)
//        theTextArea.setTextCursorDrawable(Drawable.createFromPath("@drawable/chat_cursor"));
//        theTextArea.setCursorVisible(true);
        ImageView ivClose = searchView.findViewById(search_close_btn);
        ivClose.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
        utils = new Utils();
        //chatsViewModel.getUserMutableLiveData().observe(this, userListUpdateObserver); //TODO: delete (?)
        context = this;
        ReadUsers(context); //call ReadUsers methode
    }

    private void ReadUsers(Context context) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser(); //get the current connected user from FireBase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users"); //get reference to users saved in FireBase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear(); //initialize the list of the users
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserModel user = snapshot.getValue(UserModel.class); //save user data from FireBase in a pattern of UserModel POJO
                    if (user.getuID() != null) {
                        if (!user.getuID().equals(firebaseUser.getUid())) //if the user id is different then current connected user
                            mUsers.add(user); //add user to the list
                        else
                            myID = user.getuID(); //else set myID string value
                        userAdapter = new UserAdapter(Dashboard.this, mUsers, chatsViewModel); //define adapter for the recycle view
                        recyclerView.setAdapter(userAdapter); //set adapter for recycle view
                        chatsViewModel.getSelected().observe((LifecycleOwner) context, item -> { //update the userAdapter about change in date and refresh the view
                            userAdapter.notifyDataSetChanged();
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //create menu using inflater
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //define the menu functionality
        Fragment fragment;
        FragmentManager fragmentManager;
        //findViewById(R.id.card).setVisibility(View.INVISIBLE); //TODO: delete (?)
        switch (item.getItemId()) {
            case R.id.Exit:
                finish(); // Close the app
                //fragment = new Settings(); //TODO: delete (?)
                //fragmentManager = getSupportFragmentManager();
                //fragmentManager.beginTransaction().replace(R.id.dashboardContainer, fragment).addToBackStack("BBB").commit();
                break;
            case R.id.profile:
                fragment = new Profile();
                fragmentManager = getSupportFragmentManager();
                Bundle args = new Bundle();
                args.putBoolean("myProfile", true);
                args.putString("userID", myID);
                fragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.dashboardContainer, fragment).addToBackStack("AAA").commit();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onResume() { //if user resume to the app update status to online
        utils.updateOnlineStatus("online");
        super.onResume();
    }

    @Override
    protected void onPause() { //if user pause the app set the last seen time to current time
        utils.updateOnlineStatus(String.valueOf(System.currentTimeMillis()));
        super.onPause();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (userAdapter != null)
            userAdapter.getFilter().filter(newText);
        return false;
    }

}