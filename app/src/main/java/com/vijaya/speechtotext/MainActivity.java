package com.vijaya.speechtotext;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView mVoiceInputTv;
    private ImageButton mSpeakBtn;

    TextToSpeech textToSpeech;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVoiceInputTv = findViewById(R.id.voiceInput);
        mSpeakBtn = findViewById(R.id.btnSpeak);
        mSpeakBtn.setOnClickListener(v -> startVoiceInput());

        preferences = getSharedPreferences("namePrefs",0);
        editor = preferences.edit();

        // Say "Hello!" on page load
        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if(status != TextToSpeech.ERROR) {
                // set locale
                textToSpeech.setLanguage(Locale.US);
                textToSpeech.speak("Hello!", TextToSpeech.QUEUE_FLUSH, null);
                mVoiceInputTv.setText(Html.fromHtml("<h4>Medical Assistant: Hello</h4>"));
            }
        });
    }


    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    if(result != null && result.size() > 0) {
                        mVoiceInputTv.append(Html.fromHtml("<p>Me: " + result.get(0) + "</p>"));

                        if(result.get(0).equalsIgnoreCase("hello")) {
                            textToSpeech.speak("What is your name", TextToSpeech.QUEUE_FLUSH, null);
                            mVoiceInputTv.append(Html.fromHtml("<p>Medical Assistant: What is your name?</p>"));

                        } else if(result.get(0).contains("name")){
                            String name = result.get(0).substring(result.get(0).lastIndexOf(' ') + 1);
                            editor.putString("name", name).apply();
                            textToSpeech.speak("Hello, " + name, TextToSpeech.QUEUE_FLUSH, null);
                            mVoiceInputTv.append(Html.fromHtml("<p>Medical Assistant: Hello, " + name + "</p>"));

                        } else if(result.get(0).contains("I am not feeling well. What should I do?")){
                            textToSpeech.speak("I can understand. Please tell your symptoms in short", TextToSpeech.QUEUE_FLUSH, null);
                            mVoiceInputTv.append(Html.fromHtml("<p>Medical Assistant: I can understand. Please tell your symptoms in short</p>"));

                        } else if(result.get(0).contains("What time is it?")){
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");//dd/MM/yyyy
                            Date now = new Date();
                            String[] dateArray = simpleDateFormat.format(now).split(":");

                            if(dateArray[1].contains("00")) {
                                dateArray[1] = "o'clock";
                            }
                            textToSpeech.speak("The time is : " + simpleDateFormat.format(now), TextToSpeech.QUEUE_FLUSH, null);
                            mVoiceInputTv.append(Html.fromHtml("<p>Speaker : The time is : " + simpleDateFormat.format(now) + "</p>"));

                        } else if(result.get(0).contains("What medicines should I take?")){
                            textToSpeech.speak("I think you have fever. Please take this medicine.", TextToSpeech.QUEUE_FLUSH, null);
                            mVoiceInputTv.append(Html.fromHtml("<p>Medical Assistant: I think you have fever. Please take this medicine.</p>"));

                        } else {
                            textToSpeech.speak("Sorry, I don't understand. Please try another way.", TextToSpeech.QUEUE_FLUSH, null);
                            mVoiceInputTv.append(Html.fromHtml("<p>Medical Assistant: Sorry, I don't understand. Please try another way.</p>"));

                        }
                    }
                }
                break;
            }
        }
    }
}