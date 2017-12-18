package com.google.firebase.udacity.friendlychat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.udacity.friendlychat.constant.Constants;
import com.google.firebase.udacity.friendlychat.messageList.ItemClickListener;
import com.google.firebase.udacity.friendlychat.messageList.MessageHolder;
import com.google.firebase.udacity.friendlychat.model.Message;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private String username;

    // Activity elements
    private ProgressBar progressBar;
    private ImageButton photoPickerButton;
    private EditText messageEditText;
    private Button sendButton;
    private Button updateButton;
    private RecyclerView messagesRecyclerView;

    // Firebase instance variables
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference messagesDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseStorage firebaseStorage;
    private StorageReference chatPhotosStorageReference;
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private FirebaseRecyclerAdapter recyclerAdapter;
    private int changedOrAddedMessagePosition;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = Constants.ANONYMOUS;

        // Initialize Firebase components
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // References to Firebase storage & DB
        messagesDatabaseReference = firebaseDatabase.getReference().child("messages");
        chatPhotosStorageReference = firebaseStorage.getReference().child("chat_photos");

        // Initialize references to views
        progressBar = findViewById(R.id.progressBar);
        messagesRecyclerView = findViewById(R.id.messageRecyclerView);
        photoPickerButton = findViewById(R.id.photoPickerButton);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        updateButton = findViewById(R.id.updateButton);

        // recycler adapter with FirebaseUI - automatically reacts on changes in database and refreshes messages view in RecyclerView
        Query query = messagesDatabaseReference;
        FirebaseRecyclerOptions<Message> recyclerOptions = new FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .build();

        recyclerAdapter = new FirebaseRecyclerAdapter<Message, MessageHolder>(recyclerOptions) {
            ItemClickListener clickListener = (clickedItemPosition, view) -> {
                Toast.makeText(MainActivity.this, clickedItemPosition + " Message clicked", Toast.LENGTH_SHORT).show();
                changedOrAddedMessagePosition = clickedItemPosition;
            };
            ItemClickListener longClickListener = (clickedItemPosition, view) -> {
                changedOrAddedMessagePosition = clickedItemPosition;
                Toast.makeText(MainActivity.this, clickedItemPosition + " Message long clicked", Toast.LENGTH_SHORT).show();
                openContextMenu(view);
            };

            @Override
            public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
                registerForContextMenu(view); // todo
                return new MessageHolder(view, clickListener, longClickListener);
            }

            @Override
            protected void onBindViewHolder(MessageHolder holder, int position, Message message) {
                holder.bind(message);
            }

//            @Override
//            public void onDataChanged() {
//                super.onDataChanged();
//                messagesRecyclerView.scrollToPosition(recyclerAdapter.getItemCount() - 1);
//            }

            @Override
            public void onError(DatabaseError error) {
                super.onError(error);
                Toast.makeText(MainActivity.this, "Error while getting data", Toast.LENGTH_SHORT).show();
            }
        };

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setHasFixedSize(true);
        messagesRecyclerView.setAdapter(recyclerAdapter);

        // Initialize progress bar
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        photoPickerButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), Constants.RC_PHOTO_PICKER);
        });

        // Enable Send button when there's text to send
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    sendButton.setEnabled(true);
                    updateButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                    updateButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.DEFAULT_MSG_LENGTH_LIMIT)}); // can be set from firebase

        // Send button sends a message and clears the EditText
        sendButton.setOnClickListener(view -> {
            Message message = new Message(messageEditText.getText().toString(), username, null);
            messagesDatabaseReference.push().setValue(message);
            messagesRecyclerView.scrollToPosition(recyclerAdapter.getItemCount() - 1);
            changedOrAddedMessagePosition = recyclerAdapter.getItemCount() - 1;
            messageEditText.setText("");
        });

        authStateListener = firebaseAuth -> { // FirebaseAuth.AuthStateListener()
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                currentUser = user;
                onSignedInInitialize(user.getDisplayName());
            } else {
                // User is signed out
                onSignedOutCleanup();
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false)
                                .setAvailableProviders(
                                        Arrays.asList(
                                                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                .setTheme(R.style.LoginTheme)
                                .setLogo(R.mipmap.logo)
                                .build(),
                        Constants.RC_SIGN_IN);
            }
        };

        // Create Remote Config Setting to enable developer mode.
        // Fetching configs from the server is normally limited to 5 requests per hour.
        // Enabling developer mode allows many more requests to be made per hour, so developers
        // can test different config values during development.
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        firebaseRemoteConfig.setConfigSettings(configSettings);

        // Define default config values. Defaults are used when fetched config values are not
        // available. Eg: if an error occurred fetching values from the server.
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put(Constants.FRIENDLY_MSG_LENGTH_KEY, Constants.DEFAULT_MSG_LENGTH_LIMIT);
        firebaseRemoteConfig.setDefaults(defaultConfigMap);
        fetchConfig();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // Sign-in succeeded, set up the UI
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) { // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == Constants.RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Get a reference to store file at chat_photos/<FILENAME>
                StorageReference photoRef = chatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());
                // Upload file to Firebase Storage
                photoRef.putFile(selectedImageUri)
                        .addOnSuccessListener(this, taskSnapshot -> {
                            // When the image has successfully uploaded, we get its download URL
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            if (downloadUrl != null) {
                                // Set the download URL to the message box, so that the user can send it to the database
                                Message message = new Message(null, username, downloadUrl.toString());
                                messagesDatabaseReference.push().setValue(message);
                                messagesRecyclerView.scrollToPosition(recyclerAdapter.getItemCount() - 1);
                                changedOrAddedMessagePosition = recyclerAdapter.getItemCount() - 1;
                            } else {
                                Toast.makeText(this, "Image wasn't uploaded", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        messagesRecyclerView.scrollToPosition(recyclerAdapter.getItemCount() - 1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
        recyclerAdapter.stopListening();
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onSignedInInitialize(String username) {
        this.username = username;
        recyclerAdapter.startListening();
    }

    private void onSignedOutCleanup() {
        username = Constants.ANONYMOUS;
        recyclerAdapter.stopListening();
    }

    // Fetch the config to determine the allowed length of messages.
    public void fetchConfig() {
        long cacheExpiration = 3600; // 1 hour in seconds
        // If developer mode is enabled reduce cacheExpiration to 0 so that each fetch goes to the
        // server. This should not be used in release builds.
        if (firebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        firebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(aVoid -> {
                    // Make the fetched config available via FirebaseRemoteConfig get<type> calls, e.g., getLong, getString.
                    firebaseRemoteConfig.activateFetched();
                    // Update the EditText length limit with the newly retrieved values from Remote Config.
                    applyRetrievedLengthLimit();
                })
                .addOnFailureListener(e -> {
                    // An error occurred when fetching the config.
                    Log.w(Constants.TAG, "Error fetching config", e);
                    // Update the EditText length limit with the newly retrieved values from Remote Config.
                    applyRetrievedLengthLimit();
                });
    }

    //    Apply retrieved length limit to edit text field. This result may be fresh from the server or it may be from cached values.
    private void applyRetrievedLengthLimit() {
        Long friendly_msg_length = firebaseRemoteConfig.getLong(Constants.FRIENDLY_MSG_LENGTH_KEY);
        messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(friendly_msg_length.intValue())});
        Log.d(Constants.TAG, Constants.FRIENDLY_MSG_LENGTH_KEY + " = " + friendly_msg_length);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Message");
        menu.add(0, Constants.MENU_EDIT_MESSAGE, 0, R.string.edit_message);
        menu.add(0, Constants.MENU_REMOVE_MESSAGE, 0, R.string.remove_message);
        menu.add(0, Constants.MENU_COPY_MESSAGE_TEXT, 0, R.string.copy_text);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        DatabaseReference referenceToMessage = recyclerAdapter.getRef(changedOrAddedMessagePosition);

        switch (item.getItemId()) {
            case Constants.MENU_EDIT_MESSAGE:
                updateMessage(referenceToMessage);
                break;
            case Constants.MENU_REMOVE_MESSAGE:
                referenceToMessage.removeValue();
                break;
            case Constants.MENU_COPY_MESSAGE_TEXT:
                copyMessageText();
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void updateMessage(DatabaseReference referenceToMessage) {
        sendButton.setVisibility(View.GONE);
        updateButton.setVisibility(View.VISIBLE);

        Message message = (Message) recyclerAdapter.getItem(changedOrAddedMessagePosition);
        messageEditText.setText(message.getText());

        updateButton.setOnClickListener(view -> {
            sendButton.setVisibility(View.VISIBLE);
            updateButton.setVisibility(View.GONE);
            referenceToMessage.setValue(new Message(messageEditText.getText().toString(), message.getName(), message.getPhotoUrl()));
            messageEditText.setText("");
        });

        referenceToMessage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void copyMessageText() {
        Message message = (Message) recyclerAdapter.getItem(changedOrAddedMessagePosition);
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (message.getText() != null) {
            ClipData clip = ClipData.newPlainText("text", message.getText());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "Message text saved to clipboard", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Error trying to save to clipboard", Toast.LENGTH_SHORT).show();
            }
        }
    }
}