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
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN = 1;

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final String FRIENDLY_MSG_LENGTH_KEY = "friendly_msg_length";


    private static final int RC_PHOTO_PICKER = 2;
    private ListView mMessageListView;
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
    public static ArrayList<Double> latencies = new ArrayList<>();
    public static ArrayList<Double> twoWayLatencies = new ArrayList<>();
    public static ArrayList<Double> phone2Received = new ArrayList<>();
    final GetTiming getTiming = new GetTiming();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //pool executor?
        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        final Logged Logged = new Logged();
        final AnalyzeData analyzeData = new AnalyzeData();

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
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

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
                i = 1;

                //Get experiment title from the text field
                final String testName = mMessageEditText.getText().toString();
                mMessageDatabaseReference.child(testName).setValue("1");

                //Attach database listener right after getting the test name from the user
                attachDatabaseReadListener();
                //Generate test data
                scheduler.scheduleAtFixedRate
                        (new Runnable() {
                            public void run() {
                                long timed = getTiming.giveTiming();
                                double seconds = timed / 1000000000.0;

                                Logged logged = new Logged(i, seconds, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
                                mMessageDatabaseReference.child(testName).child(String.valueOf(i)).setValue(logged);

                                i = i + 1;
                            }
                        }, 0, 1000000000, TimeUnit.NANOSECONDS);

                // Clear input box
                mMessageEditText.setText("");
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
            case R.id.graph_latest_data:
                attachDataListenerSingleGet();
            case R.id.receiver_activate:
                attachDataListenerReceiverGet();
                return true;
            default:
                return onOptionsItemSelected(item);
        }

    }

    private void onSignedInInitialize(String username) {
        mUsername = username;
    }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
        detachDatabaseReadListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }


    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            final String testName = mMessageEditText.getText().toString();
            mChildEventListener = new ChildEventListener() {
                @Override
                //updating time for self-ping
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    final GetTiming getTiming = new GetTiming();
                    //received time for the database
                    long receiveTime = getTiming.giveTiming();
                    double secondsReceived = receiveTime / 1000000000.0;
                    //"real time" data assign
                    Logged logged = dataSnapshot.getValue(Logged.class);
                    String lastAddedID = dataSnapshot.getKey().toString();
                    if (logged.timeReceived == 0.0) {
                        mMessageDatabaseReference.child(testName).child(String.valueOf(lastAddedID)).child("timeReceived").setValue(secondsReceived);
                    }
                }
                @Override
                //updating time for listener mode
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    final GetTiming getTiming = new GetTiming();
                    long receiveTime = getTiming.giveTiming();
                    double secondsReceived = receiveTime / 1000000000.0;
                    Logged logged = dataSnapshot.getValue(Logged.class);
                    String lastAddedID = dataSnapshot.getKey().toString();
                    if (logged.timeSent != 0.0 & logged.timeReceived != 0.0 & logged.receivedAt != 0.0 & logged.timeThroughReceiver == 0.0) {
                        mMessageDatabaseReference.child(testName).child(String.valueOf(lastAddedID)).child("timeThroughReceiver").setValue(secondsReceived);
                    }
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
            mMessageDatabaseReference.child(testName).limitToLast(2).addChildEventListener(mChildEventListener);
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

    private void attachDataListenerSingleGet() {
        final String testName = mMessageEditText.getText().toString();
        final AnalyzeData analyze = new AnalyzeData();

        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    //"real time" data fetching
                    Logged logged = dataSnapshot.getValue(Logged.class);
                    double latency = logged.timeReceived - logged.timeSent;
                    Log.d("Latency", String.valueOf(latency));

                    if (logged.timeThroughReceiver != 0) {
                        double twoWayLatency = logged.timeThroughReceiver - logged.timeSent;
                        Log.d("Two Way Latency", String.valueOf(twoWayLatency));
                        Log.d("Phone 2 received", String.valueOf(logged.receivedAt));
                        mMessageDatabaseReference.child(testName).child(String.valueOf(logged.itemID)).child("twoWayLatency").setValue(twoWayLatency);
                        twoWayLatencies.add(twoWayLatency);
                        phone2Received.add(logged.receivedAt);
                    }
                    mMessageDatabaseReference.child(testName).child(String.valueOf(logged.itemID)).child("latency").setValue(latency);
                    latencies.add(latency);


                    String averageLatency = String.valueOf(analyze.getAverage(latencies));
                    TextView mDataDisplayAverage = (TextView) findViewById(R.id.data_display_average);
                    mDataDisplayAverage.setText(averageLatency);

                    String min = String.valueOf(analyze.getMin(latencies));
                    TextView mDataDisplayMin = (TextView) findViewById(R.id.data_display_min);
                    mDataDisplayMin.setText(min);

                    String max = String.valueOf(analyze.getMax(latencies));
                    TextView mDataDisplayMax = (TextView) findViewById(R.id.data_display_max);
                    mDataDisplayMax.setText(max);

                    String twoWayAverage = String.valueOf(analyze.getAverage(twoWayLatencies));
                    TextView mDataDisplayTwoWayAverage = (TextView) findViewById(R.id.data_display_twoWayLatencyAverage);
                    mDataDisplayTwoWayAverage.setText(twoWayAverage);

                    String receivedDifference = String.valueOf(analyze.getReceivedTimeDifference(phone2Received));
                    TextView mDataDisplayReceivedDifferenceAverage = (TextView) findViewById(R.id.data_display_receivedTimeDifferenceAverage);
                    mDataDisplayReceivedDifferenceAverage.setText(receivedDifference);

                    String receivedMax = String.valueOf(analyze.getMax(analyze.phone2ReceivedDifferences));
                    TextView mDataDisplayReceivedDifferenceMax = (TextView) findViewById(R.id.data_display_receivedTimeDifferenceMax);
                    mDataDisplayReceivedDifferenceMax.setText(receivedMax);


                    String receivedMin = String.valueOf(analyze.getMin(analyze.phone2ReceivedDifferences));
                    TextView mDataDisplayReceivedDifferenceMin = (TextView) findViewById(R.id.data_display_receivedTimeDifferenceMin);
                    mDataDisplayReceivedDifferenceMin.setText(receivedMin);

                    Log.d("AVERAGE", String.valueOf(averageLatency));
                    Log.d("min", String.valueOf(min));
                    Log.d("max", String.valueOf(max));
                    Log.d("twoWayAverage", String.valueOf(twoWayAverage));
                    Log.d("receivedDifference", String.valueOf(receivedDifference));

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
            mMessageDatabaseReference.child(testName).addChildEventListener(mChildEventListener);


        }
    }

    private void attachDataListenerReceiverGet() {
        final String testName = mMessageEditText.getText().toString();
        final AnalyzeData analyze = new AnalyzeData();

        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    //sending time after receiving a child update
                    Logged logged = dataSnapshot.getValue(Logged.class);

                    long timed = getTiming.giveTiming();
                    double seconds = timed / 1000000000.0;
                    String lastAddedID = dataSnapshot.getKey();
                    Log.d("KEY:", String.valueOf(lastAddedID));
                    Log.d("SECONDS SENT:", String.valueOf(seconds));
                    mMessageDatabaseReference.child(testName).child(String.valueOf(lastAddedID)).child("receivedAt").setValue(seconds);

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
            mMessageDatabaseReference.child(testName).addChildEventListener(mChildEventListener);


        }
    }
}
