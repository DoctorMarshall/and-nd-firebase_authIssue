/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.udacity.friendlychat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.BuildConfig;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final int RC_SIGN_IN = 1;
    private static final int RC_PHOTO_PICKER = 2;
    public static final String FRIENDLY_MSG_LENGTH_KEY = "friendly_msg_length";

    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;

    private String mUsername;

    //Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessageDatabaseReference;
    private FirebaseAnalytics mFirebaseAnalytics;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    public GetTiming mGetTiming;
    private DatabaseReference mDatabase_Measure;
    public int i;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        TEST
        final LogHandling logHandling = new LogHandling();
        final JSONObject newObj = logHandling.newEntry(1, 1);

        Log.d("Json: ", newObj.toString());

        //pool executor?
        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        final GetTiming getTiming = new GetTiming();

        mUsername = ANONYMOUS;

        //Initialize Firebase components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        mMessageDatabaseReference = mFirebaseDatabase.getReference().child("messages");
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");


        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);


        // Initialize message ListView and its adapter
        final List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_message, friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });
        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mUsername, null);
//                mMessageDatabaseReference.push().setValue(friendlyMessage);
                i = 1;
                final String testName = mMessageEditText.getText().toString();
                mMessageDatabaseReference.child(testName).setValue("1");
                attachDatabaseReadListener();
                scheduler.scheduleAtFixedRate
                        (new Runnable() {
                            public void run() {
                                long timed = getTiming.giveTiming();
                                double seconds = timed / 1000000000.0;

                                Log.d("Timing nano: ", String.valueOf(timed));
                                Log.d("Timing seconds: ", String.valueOf(seconds));

                                FriendlyMessage friendlyMessage = new FriendlyMessage(String.valueOf(seconds), "", null);
                                mMessageDatabaseReference.child(testName).child(String.valueOf(i)).setValue(friendlyMessage);

                                i = i + 1;

                            }
                        }, 0, 1000000000, TimeUnit.NANOSECONDS);

                // Clear input box
                mMessageEditText.setText("");
//                mMessageEditText.setActivated(false);
            }
        });

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //user signed in
                    onSignedInInitialize(user.getDisplayName());
                } else {
                    //user not signed
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))

                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put(FRIENDLY_MSG_LENGTH_KEY, DEFAULT_MSG_LENGTH_LIMIT);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);
        fetchConfig();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Signed in CANCELLED!", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectImageUri = data.getData();
            StorageReference photoRef = mChatPhotosStorageReference.child(selectImageUri.getLastPathSegment());

            photoRef.putFile(selectImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    FriendlyMessage friendlyMessage = new FriendlyMessage(null, mUsername, downloadUri.toString());
                    mMessageDatabaseReference.push().setValue(friendlyMessage);
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                Toast.makeText(MainActivity.this, "Signed out!", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return onOptionsItemSelected(item);
        }

    }

    private void onSignedInInitialize(String username) {
        mUsername = username;
//        attachDatabaseReadListener();

    }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
        mMessageAdapter.clear();
        detachDatabaseReadListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseReadListener();
        mMessageAdapter.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }


    private void attachDatabaseReadListener() {
        final GetTiming getTiming = new GetTiming();

        if (mChildEventListener == null) {
            final String testName = mMessageEditText.getText().toString();
            mChildEventListener = new ChildEventListener() {
                @Override

                //TODO - receive messages withour crashing the app!
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
//                    mMessageAdapter.add(friendlyMessage);

                    //"real time" data fetching
                    String lastAddedID = dataSnapshot.getKey().toString();
                    String lastAdded_Data = dataSnapshot.toString();
                    Log.d("Snap_ID: ", lastAddedID);
                    Log.d("Snap_Data: ", lastAdded_Data);

//                    push received time to the database
                    long receiveTime = getTiming.giveTiming();
                    double secondsReceived = receiveTime / 1000000000.0;
                    mMessageDatabaseReference.child(testName).child(String.valueOf(lastAddedID)).child("timeReceived").setValue(secondsReceived);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
//            mMessageDatabaseReference.addChildEventListener(mChildEventListener);
//            final String testName = mMessageEditText.getText().toString();
            mMessageDatabaseReference.child(testName).addChildEventListener(mChildEventListener);


        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessageDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    public void fetchConfig() {
        long cacheExpiration = 3600;
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFirebaseRemoteConfig.activateFetched();
                        applyRetrievedLengthLimit();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error fetching config", e);
                        applyRetrievedLengthLimit();
                    }
                });
    }

    private void applyRetrievedLengthLimit() {
        Long friendly_msg_length = mFirebaseRemoteConfig.getLong(FRIENDLY_MSG_LENGTH_KEY);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(friendly_msg_length.intValue())});
        Log.d(TAG, FRIENDLY_MSG_LENGTH_KEY + "=" + friendly_msg_length);
    }
}
