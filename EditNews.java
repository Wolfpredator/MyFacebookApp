package com.hand.my.myfacebookapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONException;
import org.json.JSONObject;

import static com.hand.my.myfacebookapp.ListNews.RESULT_DELETE;

public class EditNews extends Activity {

    private static final String JPEG_FILE_PREFIX = "_facebook_";
    private static final int GALLERY_REQUEST = 2;
    private static final String KEY_WORD_FOR_URI = "_!_image_uri_!_";

    final String LOG_TAG = "myLogs";
    private EditText nameFile;
    private EditText fileText;
    private ImageView mImageView;
    FileOutputStream outputStream;
    SharedPreferences mupOfFiles;
    Uri selectedImage;
    String messageToFb;
    Bitmap image;
    String imageID;
    String fullNameFile;


    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mImageView = (ImageView) findViewById(R.id.imageView);
        nameFile = (EditText) findViewById(R.id.fileName);
        fileText = (EditText) findViewById(R.id.fileText);
        Intent intent = getIntent();
        if (intent.getStringExtra("filename") != null) {
            Button btn = (Button) findViewById(R.id.buttonDelete);
            btn.setEnabled(true);
            fullNameFile = intent.getStringExtra("filename");
            readFile(fullNameFile);
        }
    }


    public void onClickEdit(View view) {
        switch (view.getId()) {
            case R.id.btnWrite:
                saveFile();
                break;
            case R.id.btnRead:
                finish();
                break;
            case R.id.button_photo:
                saveFile();
                postNews();
                break;
            case R.id.button_image:
                imageGallery();
                break;
            case R.id.buttonDelete:
                readyForDelete();
                finish();
                break;
        }
    }

    public void imageGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }


    void saveFile() {

        if (fullNameFile != null) {
            readyForDelete();}
        try {
            fullNameFile = createNameFile();
            outputStream = openFileOutput(fullNameFile, Context.MODE_PRIVATE);
            String textToSave = (fileText.getText().toString());
            if (selectedImage != null) {
                textToSave += ("\n" + KEY_WORD_FOR_URI + "\n" + selectedImage.toString());
            }
            outputStream.write(textToSave.getBytes());
            messageToFb = fileText.getText().toString();
            outputStream.close();
            setShortFileName(fullNameFile);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage().toString());
        }
        setBoolFacebook(false);
        notifyUser("Файл сохранен");
        Log.d(LOG_TAG, "Файл сохранен" + nameFile.toString());
    }


    void readFile(String fileName) {
        String shortNameFile = getShortFileName(fileName);
        nameFile.setText(shortNameFile);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(fileName)));
            String str = "\n";
            String temp = "";
            messageToFb = "";
            while ((temp = br.readLine()) != null) {
                if (temp.equals(KEY_WORD_FOR_URI)) {
                    selectedImage = Uri.parse(br.readLine());
                    workWithImage(selectedImage);
                    mImageView.setImageURI(selectedImage);
                    break;
                }
                ;
                messageToFb = messageToFb + temp + str;
            }
            fileText.setText(messageToFb);
        } catch (FileNotFoundException e) {
            Log.d(LOG_TAG, e.getMessage().toString());
        } catch (IOException e) {
            Log.d(LOG_TAG, e.getMessage().toString());
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case GALLERY_REQUEST:
                if (resultCode == RESULT_OK) {
                    selectedImage = intent.getData();
                    workWithImage(selectedImage);
                }
        }

    }

    private void workWithImage(Uri uriImg) {
        mImageView.setImageURI(uriImg);
        try {

            image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriImg);
        } catch (IOException e) {
            Log.d(LOG_TAG, e.getMessage().toString());
        }

    }


    private String createNameFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = nameFile.getText().toString() + JPEG_FILE_PREFIX + timeStamp + "_";
        return imageFileName;
    }

    private void postNews() {
        String jsonText = ("{\"message\":\"" + messageToFb + "\"");
        if (image != null) {
            postPhotoToFb(image, jsonText);
        } else {
            newsToFb(jsonText);
        }
    }


    private void postPhotoToFb(Bitmap bitmap, final String jsonText) {
        byte[] byteImage;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byteImage = baos.toByteArray();
        final String jsonImagePart = (",\"attached_media[0]\":\"{\\\"media_fbid\\\":\\\"");
        final String jsonImageEnd = ("\\\"}\",\"\":\"\"");

        Bundle params = new Bundle();
        params.putByteArray("picture", byteImage);
        params.putString("published", "false");

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/photos",
                params,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            imageID = response.getJSONObject().get("id").toString();
                            Log.d(LOG_TAG, imageID);
                        } catch (JSONException e) {
                            Log.d(LOG_TAG, e.getMessage().toString());
                        }
                        newsToFb(jsonText + jsonImagePart + imageID + jsonImageEnd);
                    }
                }
        ).executeAsync();

    }

    private void newsToFb(String jsonText) {
        jsonText += "}";
        GraphRequest request = null;
        try {
            request = GraphRequest.newPostRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/me/feed",
                    new JSONObject(jsonText),
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response) {
                            if (response.getError() == null) {
                                Log.d(LOG_TAG, "Новость добавленна");
                                notifyUser("Новость опубликована");
                            } else if (response.getError() != null) {
                                Log.d(LOG_TAG, response.getError().toString());
                            }
                        }
                    });

        } catch (JSONException e) {
            e.printStackTrace();
        }
        request.executeAsync();
        setBoolFacebook(true);
    }


    private void notifyUser(String textMessage) {
        Toast toast = Toast.makeText(getApplicationContext(),
                textMessage,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void setBoolFacebook(Boolean isItPostToFacebook) {
        mupOfFiles = getSharedPreferences("ChekBoxPref", MODE_PRIVATE);
        SharedPreferences.Editor ed = mupOfFiles.edit();
        ed.putBoolean(fullNameFile, isItPostToFacebook);
        ed.commit();
    }

    private void setShortFileName(String fullFileName) {
        mupOfFiles = getSharedPreferences("FileNamePref", MODE_PRIVATE);
        SharedPreferences.Editor ed = mupOfFiles.edit();
        ed.putString(fullFileName, nameFile.getText().toString());
        ed.commit();
    }

    private String getShortFileName(String fullNameFile) {
        mupOfFiles = getSharedPreferences("FileNamePref", MODE_PRIVATE);
        return mupOfFiles.getString(fullNameFile, "errorName");
    }

    private void readyForDelete() {
        Intent intent = getIntent();
        intent.putExtra("filename", fullNameFile);
        setResult(RESULT_DELETE, getIntent());
    }


}
