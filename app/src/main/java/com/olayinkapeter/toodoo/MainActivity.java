package com.olayinkapeter.toodoo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.olayinkapeter.toodoo.adapters.ToodooListAdapter;
import com.olayinkapeter.toodoo.auth.AccountInfo;
import com.olayinkapeter.toodoo.auth.Welcome;
import com.olayinkapeter.toodoo.helper.DividerItemDecor;
import com.olayinkapeter.toodoo.helper.RecyclerTouchListener;
import com.olayinkapeter.toodoo.helper.SwipeableRecyclerViewTouchListener;
import com.olayinkapeter.toodoo.models.ToodooListModel;
import com.olayinkapeter.toodoo.toodooOptions.ToodooNote;
import com.olayinkapeter.toodoo.fabLibrary.FloatingActionMenu;
import com.olayinkapeter.toodoo.fabLibrary.FloatingActionButton;
import com.olayinkapeter.toodoo.toodooOptions.ToodooCamera;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mUserId, mUserDisplayName, mUserEmail;
    private GoogleApiClient mGoogleApiClient;

    private FloatingActionMenu fabNote;

    private FloatingActionButton fabVoice;
    private FloatingActionButton fabCamera;

    private ProgressBar progressBar;

    private List<ToodooListModel> toodooList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private LinearLayout mEmptyView;
    private ToodooListAdapter mAdapter;

    private final int REQ_CODE_SPEECH_INPUT = 100;

    String IS_EXISTING_KEY = "IS_EXISTING_KEY", TODO_ID_KEY = "TODO_ID_KEY", TODO_ITEM_KEY = "TODO_ITEM_KEY", TODO_LABEL_KEY = "TODO_LABEL_KEY", TODO_DUEDATE_KEY = "TODO_DUEDATE_KEY", TODO_REMINDER_KEY = "TODO_REMINDER_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Firebase Auth and Database Reference
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        fabNote = (FloatingActionMenu) findViewById(R.id.fabNote);

        fabVoice = (FloatingActionButton) findViewById(R.id.fabVoice);
        fabCamera = (FloatingActionButton) findViewById(R.id.fabCamera);

        fabNote.setClosedOnTouchOutside(true);
        fabNote.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabNote.isOpened()) {
                    launchToodooNoteActivity();
                }
                fabNote.toggle(true);
            }
        });

        fabVoice.setOnClickListener(clickListener);
        fabCamera.setOnClickListener(clickListener);

        if (mFirebaseUser == null) {
            // Not logged in, launch the Log In activity
            kickUserOut();
        } else {
            populateListWithFire();
        }

        createCustomAnimation();
    }

    // Populate list with Firebase database
    private void populateListWithFire() {
        progressBar.setVisibility(View.VISIBLE);

        //Get FirebaseUser details
        mUserId = mFirebaseUser.getUid();
        mUserDisplayName = mFirebaseUser.getDisplayName();
        mUserEmail = mFirebaseUser.getEmail();

        // Set up RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mEmptyView = (LinearLayout) findViewById(R.id.emptyView);

        mAdapter = new ToodooListAdapter(toodooList, MainActivity.this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecor(MainActivity.this, LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        initView();

        // User ChildEventListener to perform real time retrieving and removing of data from Firebase database.
        mDatabase.child("users").child(mUserId).child("items").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                toodooList.add(new ToodooListModel(
                        (String) dataSnapshot.child("id").getValue(),
                        (String) dataSnapshot.child("item").getValue(),
                        (String) dataSnapshot.child("label").getValue(),
                        (String) dataSnapshot.child("dueDate").getValue(),
                        (String) dataSnapshot.child("reminder").getValue()));
                mAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                fabNote.setVisibility(View.VISIBLE);
                checkIfEmpty();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                progressBar.setVisibility(View.GONE);
                fabNote.setVisibility(View.VISIBLE);
                checkIfEmpty();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                toodooList.remove(new ToodooListModel(
                        (String) dataSnapshot.child("id").getValue(),
                        (String) dataSnapshot.child("item").getValue(),
                        (String) dataSnapshot.child("label").getValue(),
                        (String) dataSnapshot.child("dueDate").getValue(),
                        (String) dataSnapshot.child("reminder").getValue()));
                mAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                fabNote.setVisibility(View.VISIBLE);
                checkIfEmpty();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                progressBar.setVisibility(View.GONE);
                fabNote.setVisibility(View.VISIBLE);
                checkIfEmpty();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                fabNote.setVisibility(View.VISIBLE);
                checkIfEmpty();
            }
        });

        // Listener for SingleValueEvent
        mDatabase.child("users").child(mUserId).child("items").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                fabNote.setVisibility(View.VISIBLE);
                checkIfEmpty();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                fabNote.setVisibility(View.VISIBLE);
                checkIfEmpty();
            }
        });

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(MainActivity.this, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
//                    ToodooListModel ToodooListModel = toodooList.get(position);
//
//                        Intent intent = new Intent(MainActivity.this, ToodooNote.class);
//                        intent.putExtra(IS_EXISTING_KEY, "true");
//                        intent.putExtra(TODO_ID_KEY, ToodooListModel.getTodoId());
//                        intent.putExtra(TODO_ITEM_KEY, ToodooListModel.getTodoItem());
//                        intent.putExtra(TODO_LABEL_KEY, ToodooListModel.getTodoLabel());
//                        intent.putExtra(TODO_DUEDATE_KEY, ToodooListModel.getTodoDueDate());
//                        intent.putExtra(TODO_REMINDER_KEY, ToodooListModel.getTodoReminder());
//
//                        startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    //OnClickListener for fabItems
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fabVoice:
                    if (fabNote.isOpened()) {
                        launchToodooVoiceIntent();
                    }
                    fabNote.toggle(true);
                    break;
                case R.id.fabCamera:
                    if (fabNote.isOpened()) {
                        launchToodooCameraActivity();
                    }
                    fabNote.toggle(true);
                    break;
            }
        }
    };

    private void checkIfEmpty() {
        if (mAdapter.getItemCount() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    //fabNote opens ToodooNote Activity
    private void launchToodooNoteActivity() {
        Intent intent = new Intent(MainActivity.this, ToodooNote.class);
        intent.putExtra(IS_EXISTING_KEY, "false");
        startActivity(intent);
    }

    //fabVoice opens ToodooVoice Intent
    private void launchToodooVoiceIntent() {
        promptSpeechInput();
    }

    //fabVision opens ToodooCamera Activity
    private void launchToodooCameraActivity() {
        Intent intent = new Intent(MainActivity.this, ToodooCamera.class);
        intent.putExtra(IS_EXISTING_KEY, "camera");
        startActivity(intent);
    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    String speechText = result.get(0);
                    speechText = speechText.substring(0,1).toUpperCase() + speechText.substring(1).toLowerCase();

                    Intent intent = new Intent(this, ToodooNote.class);
                    intent.putExtra(IS_EXISTING_KEY, "voice");
                    intent.putExtra(TODO_ITEM_KEY, speechText);
                    startActivity(intent);
                }
                break;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_account:
                Intent intent = new Intent(this, AccountInfo.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //Log out UserClass and go to LoginActivity
    private void kickUserOut() {
        Intent intent = new Intent(this, Welcome.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    //Animation for fabNote Toggle icons
    private void createCustomAnimation() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(fabNote.getMenuIconView(), "scaleX", 1.0f, 0.2f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(fabNote.getMenuIconView(), "scaleY", 1.0f, 0.2f);

        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(fabNote.getMenuIconView(), "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(fabNote.getMenuIconView(), "scaleY", 0.2f, 1.0f);

        scaleOutX.setDuration(50);
        scaleOutY.setDuration(50);

        scaleInX.setDuration(150);
        scaleInY.setDuration(150);

        scaleInX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                fabNote.getMenuIconView().setImageResource(fabNote.isOpened() ? R.mipmap.fab_add : R.mipmap.ic_edit);
            }
        });

        set.play(scaleOutX).with(scaleOutY);
        set.play(scaleInX).with(scaleInY).after(scaleOutX);
        set.setInterpolator(new OvershootInterpolator(2));

        fabNote.setIconToggleAnimatorSet(set);
    }

    private void initView() {
        SwipeableRecyclerViewTouchListener swipeTouchListener =
                new SwipeableRecyclerViewTouchListener(MainActivity.this, mRecyclerView, R.id.main_view,
                        R.id.main_background_view,                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
                    @Override
                    public void onDismissedBySwipe(RecyclerView recyclerView, int[] reverseSortedPositions) {
                        for (int position : reverseSortedPositions) {
                            ToodooListModel ToodooListModel = toodooList.get(position);

                            mDatabase.child("users").child(mUserId).child("items")
                                    .orderByChild("id").equalTo(ToodooListModel.getTodoId())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChildren()) {
                                                DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                                                firstChild.getRef().removeValue();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                            toodooList.remove(position);
                            mAdapter.notifyItemRemoved(position);
                        }
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public boolean canSwipe(int position) {
//                                if (position == toodooList.size() - 1) {
//                                    return false;
//                                }
                        return true;
                    }

                    @Override
                    public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                        Toast.makeText(MainActivity.this, "Swiped Left", Toast.LENGTH_SHORT).show();
                        for (int position : reverseSortedPositions) {
                            //change some data if you swipe left
                        }
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                        Toast.makeText(MainActivity.this, "Swiped Right", Toast.LENGTH_SHORT).show();
                        for (int position : reverseSortedPositions) {
                            //change some data if you swipe right
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });
        mRecyclerView.addOnItemTouchListener(swipeTouchListener);
    }
}