package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; //авторизация. оф документация с сайта firebase

    private static final String TAG = "SignInActivity"; //ключ для logcat

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;
    private EditText nameEditText;
    private TextView toggleLoginSignUpTextView;
    private Button loginSignUpButton;

    private boolean loginModeActive;

    FirebaseDatabase database;
    DatabaseReference usersDataBaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersDataBaseReference = database.getReference().child("users");

        repeatPasswordEditText = findViewById(R.id.repeatPassEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passEditText);
        nameEditText = findViewById(R.id.nameEditText);
        toggleLoginSignUpTextView = findViewById(R.id.toggleLoginSignUpTextView);
        loginSignUpButton = findViewById(R.id.loginSignUpButton);

        //слушает нажатие кнопки Sign Up
        loginSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginSignUpUser(emailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim()); //передаёт в метод mail и pas с эдиттекстов
            }
        });

        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(SignInActivity.this, MainActivity.class));     //запускает MainActivity если пользователь залогинен (проверка стандартным методом getCurrentUser)
        }

    }

    private void loginSignUpUser(String email, String password) {

        if (loginModeActive){ //режим лог ина

            if(passwordEditText.getText().toString().trim().length() < 7){
                Toast.makeText(this, "Passwords must be at least 7 characters", Toast.LENGTH_SHORT).show();
            }
            else if(emailEditText.getText().toString().trim().equals("")){
                Toast.makeText(this, "Please, input your email", Toast.LENGTH_SHORT).show();
            }
            else {
                mAuth.signInWithEmailAndPassword(email, password) //методы из документации по Firebase, на офф сайте раздел Android -> авторизация по паролю
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                    intent.putExtra("Username", nameEditText.getText().toString().trim());
                                    startActivity(intent);
                                    //updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(SignInActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    //updateUI(null);
                                }
                            }
                        });
            }


        } else { //режим регистрации

            if(!passwordEditText.getText().toString().trim().equals(repeatPasswordEditText.getText().toString().trim())){ //если пароли не совпадают, тост
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            }
            else if(passwordEditText.getText().toString().trim().length() < 7){
                Toast.makeText(this, "Passwords must be at least 7 characters", Toast.LENGTH_SHORT).show();
            }
            else if(emailEditText.getText().toString().trim().equals("")){
                Toast.makeText(this, "Please, input your email", Toast.LENGTH_SHORT).show();
            }
            else {
                mAuth.createUserWithEmailAndPassword(email, password)   //методы из документации по Firebase, на офф сайте раздел Android -> авторизация по паролю
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    createUser(user);
                                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                    intent.putExtra("Username", nameEditText.getText().toString().trim());
                                    startActivity(intent);
                                    //updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(SignInActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    //updateUI(null);
                                }
                            }
                        });
            }

        }
    }

    private void createUser(FirebaseUser firebaseUser) { //создание пользователя

        User user = new User();
        user.setId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setName(nameEditText.getText().toString().trim());
        usersDataBaseReference.push().setValue(user);
    }

    // проверка кнопки Log in или Sign Up, переключается тапом по TextView
    public void toggleLoginMode(View view) {

        if (loginModeActive) {
            loginModeActive = false;
            loginSignUpButton.setText("Sign Up");
            toggleLoginSignUpTextView.setText("Or Log In");
            repeatPasswordEditText.setVisibility(View.VISIBLE);
        } else {
            loginModeActive = true;
            loginSignUpButton.setText("Log In");
            toggleLoginSignUpTextView.setText("Or Sign Up");
            repeatPasswordEditText.setVisibility(View.GONE);
        }

    }
}