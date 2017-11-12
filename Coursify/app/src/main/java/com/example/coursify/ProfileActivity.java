package com.example.coursify;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = ProfileActivity.class.getSimpleName();
    private static final String FIREBASE_FBUSERS_ENDPOINT = "FacebookUsers";
    private static final String FIREBASE_USERS_ENDPOINT = "Users";
    private static final String FIREBASE_USERS_FRIENDS_ENDPOINT = "FacebookFriends";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FBFriendsFragment friendsFragment = new FBFriendsFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.activity_profile, friendsFragment);
        transaction.commit();
    }

    public static class FBFriendsFragment extends Fragment {
        List<User> userList = new ArrayList<>();
        FriendListAdapter friendListAdapter;
        DatabaseReference ref;
        private FirebaseAuth mAuth;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState){
            View fragmentView = inflater.inflate(R.layout.fragment_fbfriends, container, false);

            RecyclerView recyclerView;
            recyclerView = (RecyclerView) fragmentView.findViewById(R.id.friendsRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            ref = FirebaseDatabase.getInstance().getReference();
            mAuth = FirebaseAuth.getInstance();

            this.friendListAdapter = new FriendListAdapter(userList);
            recyclerView.setAdapter(friendListAdapter);

            ref.child(FIREBASE_USERS_ENDPOINT)
                    .child(Utils.processEmail(mAuth.getCurrentUser().getEmail()))
                    .child("facebookID")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String fbID = dataSnapshot.getValue(String.class);
                            Log.v(TAG, "fb id is: " + fbID);
                            showFriends(fbID);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, databaseError.toString());
                        }
                    });




            return fragmentView;
        }

        private void getFriendInfo(String fbID) {
            ref.child(FIREBASE_FBUSERS_ENDPOINT)
                    .child(fbID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String email = dataSnapshot.getValue(String.class);
                            Log.v(TAG, "email is " + email);
                            if(email != null) {
                                ref.child(FIREBASE_USERS_ENDPOINT)
                                        .child(email)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                String friendName = "";
                                                String friendMajor = "";
                                                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if(snapshot.getKey().equals("major")) {
                                                        friendMajor = snapshot.getValue(String.class);
                                                    }
                                                    else if (snapshot.getKey().equals("name")) {
                                                        friendName = snapshot.getValue(String.class);
                                                    }
                                                }
                                                userList.add(new User(friendName, friendMajor));
                                                friendListAdapter.notifyItemInserted(userList.size() - 1);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

        private void showFriends(String mFBUserId) {
            /* make the API call */
            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/" + mFBUserId + "/friends",
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            JSONObject object = response.getJSONObject();
                            try {
                                JSONArray arrayOfUsersInFriendList = object.getJSONArray("data");
                                Log.v(TAG, "User friend list length: " + arrayOfUsersInFriendList.length());
                                List<String> fbFriends = new ArrayList<>();

                                for(int i = 0; i < arrayOfUsersInFriendList.length(); i++) {
                                    JSONObject user = arrayOfUsersInFriendList.getJSONObject(i);
                                    String userId = user.getString("id");
                                    Log.v(TAG, "user id: " + userId);
                                    Log.v(TAG, "user name is " + user.getString("name"));
                                    getFriendInfo(userId);
                                    fbFriends.add(userId);
                                }
                                ref.child(FIREBASE_USERS_ENDPOINT)
                                        .child(Utils.processEmail(mAuth.getCurrentUser().getEmail()))
                                        .child(FIREBASE_USERS_FRIENDS_ENDPOINT)
                                        .setValue(fbFriends);

                            } catch (JSONException e) {
                                Log.v(TAG, e.toString());
                                e.printStackTrace();
                            }
                        }
                    }
            ).executeAsync();

        }
    }
}