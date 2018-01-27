package com.example.coursify;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loginBtn = (Button) findViewById(R.id.login_button);
        Button registerBtn = (Button) findViewById(R.id.register_button);
        Button forgotPassBtn = (Button) findViewById(R.id.forgotPass);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent mIntent = new Intent(getApplicationContext(), LoginActivity.class);
                Log.v(TAG, "Proceeding to LoginActivity");
                startActivity(mIntent);
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent mIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                Log.v(TAG, "Proceeding to RegisterActivity");
                startActivity(mIntent);
            }
        });

        forgotPassBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent mIntent = new Intent(getApplicationContext(), EmailResendActivity.class);
                Log.v(TAG, "Proceeding to EmailResendActivity");
                startActivity(mIntent);
            }
        });
    }
}