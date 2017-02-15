package com.hand.my.myfacebookapp;

import android.Manifest;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ListNews extends AppCompatActivity {

    public static final int RESULT_DELETE = 666;
    final String LOG_TAG = "myLogs";
    int EDIT_ID = 1;
    ListView newsList;
    CallbackManager callbackManager;
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);
        callbackManager = CallbackManager.Factory.create();

        List<String> permissionNeeds = Arrays.asList("publish_actions");
        final LoginManager loginManager = LoginManager.getInstance();
        loginManager.logInWithPublishPermissions(this, permissionNeeds);
        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(LOG_TAG, "Авторизация прошла успешно");
            }

            @Override
            public void onCancel() {
                Log.d(LOG_TAG, "отменен");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(LOG_TAG, "ошибочка вышла:" + error);
            }
        });
        newsList = (ListView) findViewById(R.id.listNews);
        newsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        iNeedMorePower();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter = new MyArrayAdapter(
                this, getFbNews());
        newsList.setAdapter(adapter);
        newsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ListNews.this, EditNews.class);
                intent.putExtra("filename", adapter.getItem(position));
                startActivityForResult(intent, EDIT_ID);
            }
        });
        adapter.notifyDataSetChanged();
    }

    public void onClickMain(View view) {
        Intent intent = new Intent(this, EditNews.class);
        startActivityForResult(intent, EDIT_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        callbackManager.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_DELETE) {
            deleteFile(intent.getStringExtra("filename"));
        }
    }

    private void iNeedMorePower() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
    }

    String[] getFbNews() {
        ArrayList<String> newsList = new ArrayList<>();
        for (String name :
                fileList()) {
            if (findKeyWord(name)) {
                newsList.add(name);
            };
        }
        String[] array = newsList.toArray(new String[newsList.size()]);
        System.out.println("arrayLISTfbNEWS:" + array);
        return array;
    }

    boolean findKeyWord(String nameFile) {
        Pattern pattern = Pattern.compile("_facebook_");
        Matcher matcher = pattern.matcher(nameFile);
        return matcher.find();
    }

}
