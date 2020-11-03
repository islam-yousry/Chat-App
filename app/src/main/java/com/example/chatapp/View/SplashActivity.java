package com.example.chatapp.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import com.example.chatapp.Model.User;
import com.example.chatapp.Util.Utils;
import com.example.chatapp.ViewModel.MainViewModel;

import java.util.List;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Utils.checkAuthenticationState(SplashActivity.this)) {
            MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
            viewModel.getChatsUsers().observe(this, new Observer<List<User>>() {
                @Override
                public void onChanged(List<User> users) {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.putExtra("keepMeSigned", true);
                    startActivity(intent);
                    finish();
                }
            });
        }

    }

}