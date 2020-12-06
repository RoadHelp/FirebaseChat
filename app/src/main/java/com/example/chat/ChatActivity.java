package com.example.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ListView messageListView;
    private MessageAdapter messageAdapter;
    private ProgressBar progressBar;
    private ImageButton sendImageButton;
    private Button sendMessageButtom;
    private EditText messageEditText;
    private String username;
    private static final int RC_IAMGE_PICKER = 123;

    FirebaseDatabase database;
    DatabaseReference messageDataBaseReference;
    ChildEventListener childEventListener;
    DatabaseReference usersDataBaseReference;
    ChildEventListener usersEventListener;

    FirebaseStorage storage;
    StorageReference chatImagesStorageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        messageDataBaseReference = database.getReference().child("chat");
        usersDataBaseReference = database.getReference().child("users");
        chatImagesStorageReference = storage.getReference().child("chat_images");

        progressBar = findViewById(R.id.progressBar);
        sendImageButton = findViewById(R.id.sendPhotoButton);
        sendMessageButtom = findViewById(R.id.sendMessageButtom);
        messageEditText = findViewById(R.id.editMessageText);

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("Username");
        } else {
            username = "Default user";
        }

        messageListView = findViewById(R.id.messageListView);
        List<MessageChat> messageChats = new ArrayList<>(); //создаёт массив объектов класса MessageChat
        messageAdapter = new MessageAdapter(this, R.layout.message_item, messageChats); //адаптер связывает массив объектов класса и слой разметки message_item
        messageListView.setAdapter(messageAdapter);

        progressBar.setVisibility(ProgressBar.INVISIBLE);

        // делает кпопку Send активной, если введённый текст без пробелов > 0
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().length() > 0) {
                    sendMessageButtom.setEnabled(true);
                }
                else {sendMessageButtom.setEnabled(false);}

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)}); //устанавливает фильтр для эдиттекста

        //отправка сообщения
        sendMessageButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MessageChat message = new MessageChat(); //создаёт объект класса
                message.setText(messageEditText.getText().toString()); //устанавливает поля сеттерами
                message.setName(username);
                message.setImageUrl(null);

                messageDataBaseReference.push().setValue(message);  //пушит в БД

                messageEditText.setText("");    //чистит эдиттекст

            }
        });

        sendImageButton.setOnClickListener(new View.OnClickListener() { // в случае клика по изображению вместо send
            @Override
            public void onClick(View v) {

                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                intent1.setType("image/*");
                intent1.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent1, "Choose an image"), RC_IAMGE_PICKER);

            }
        });


        usersEventListener = new ChildEventListener() { // изменения в бд, ветка юзеры
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                User user = snapshot.getValue(User.class);
                if (user.getId().equals(FirebaseAuth.getInstance().getUid())){
                    username = user.getName();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        usersDataBaseReference.addChildEventListener(usersEventListener);

        //реализация методов в случаях различных изменений в БД ветка чата
        childEventListener = new ChildEventListener() {
            @Override
            //когда добавляется текст, заполняем объект класса и кидаем его в адаптер
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MessageChat message = snapshot.getValue(MessageChat.class);
                messageAdapter.add(message);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        messageDataBaseReference.addChildEventListener(childEventListener);
    }

    @Override // создание меню и приявзка слоя
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override // реализация методов меню
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.signOut:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ChatActivity.this, SignInActivity.class));
                return true;
            default: return super.onOptionsItemSelected(item);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { // когда выбрана картинка
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_IAMGE_PICKER && resultCode == RESULT_OK){
            Uri selectedImageUri = data.getData(); // получаем uri картинки из data
            final StorageReference imageReference = chatImagesStorageReference.child(selectedImageUri.getLastPathSegment());
            UploadTask uploadTask;
            uploadTask = imageReference.putFile(selectedImageUri); // загружаем картинку на Firebase Storage

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult(); // скачиваем картинку с Firebase Storage
                        MessageChat message = new MessageChat();    //создаём объект класса со ссылкой на картинку
                        message.setImageUrl(downloadUri.toString());
                        message.setName(username);
                        messageDataBaseReference.push().setValue(message);
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });

        }
    }
}