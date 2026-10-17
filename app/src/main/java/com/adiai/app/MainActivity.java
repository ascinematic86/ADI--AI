
package com.adiai.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int SPEECH_REQUEST = 101;
    private EditText commandInput;
    private TextView response;
    private TextToSpeech tts;
    private boolean torchOn = false;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("mr", "IN"));
                if (result < 0) {
                    tts.setLanguage(Locale.forLanguageTag("hi-IN"));
                }
            }
        });

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 60, 32, 24);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("ADI AI");
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Your Personal Voice Assistant");
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        layout.addView(subtitle);

        commandInput = new EditText(this);
        commandInput.setHint("तुमची कमांड लिहा...");
        commandInput.setSingleLine(false);
        layout.addView(commandInput);

        Button send = new Button(this);
        send.setText("Send Command");
        layout.addView(send);

        Button mic = new Button(this);
        mic.setText("Speak to Adi AI");
        layout.addView(mic);

        response = new TextView(this);
        response.setText("नमस्कार! मी Adi AI आहे.");
        response.setTextSize(18);
        response.setPadding(0, 24, 0, 0);
        layout.addView(response);

        setContentView(layout);

        send.setOnClickListener(v ->
            executeCommand(commandInput.getText().toString())
        );

        mic.setOnClickListener(v -> startVoice());
    }

    private void startVoice() {
        if (android.os.Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{Manifest.permission.RECORD_AUDIO}, 10
            );
            return;
        }

        try {
            Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            );
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            );
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE, "mr-IN"
            );
            intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT, "Adi AI ऐकत आहे..."
            );
            startActivityForResult(intent, SPEECH_REQUEST);
        } catch (Exception e) {
            reply("Voice recognition उपलब्ध नाही.");
        }
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);

        if (request == SPEECH_REQUEST &&
            result == RESULT_OK && data != null) {
            ArrayList<String> words = data.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            );
            if (words != null && !words.isEmpty()) {
                commandInput.setText(words.get(0));
                executeCommand(words.get(0));
            }
        }
    }

    private void reply(String message) {
        response.setText(message);
        if (tts != null) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "adi");
        }
    }

    private void launch(Intent intent) {
        try {
            startActivity(intent);
        } catch (Exception e) {
            reply("ही सुविधा या फोनवर उपलब्ध नाही.");
        }
    }

    private void executeCommand(String input) {
        String cmd = input.toLowerCase(Locale.ROOT).trim();

        if (cmd.isEmpty()) {
            reply("कृपया कमांड सांगा.");
            return;
        }

        if (cmd.contains("camera") || cmd.contains("कॅमेरा")
                || cmd.contains("कैमरा")) {
            reply("कॅमेरा उघडत आहे.");
            launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));

        } else if (cmd.contains("dial") || cmd.contains("call")
                || cmd.contains("फोन") || cmd.contains("कॉल")) {
            reply("फोन डायलर उघडत आहे.");
            launch(new Intent(Intent.ACTION_DIAL));

        } else if (cmd.contains("torch") || cmd.contains("flashlight")
                || cmd.contains("टॉर्च") || cmd.contains("टॉर्च")) {
            toggleTorch();

        } else if (cmd.contains("alarm") || cmd.contains("अलार्म")
                || cmd.contains("अलार्म")) {
            reply("अलार्म सेटिंग उघडत आहे.");
            launch(new Intent(AlarmClock.ACTION_SET_ALARM));

        } else if (cmd.contains("volume") || cmd.contains("आवाज")
                || cmd.contains("व्हॉल्यूम")) {
            AudioManager audio = (AudioManager)
                getSystemService(AUDIO_SERVICE);
            if (audio != null) {
                audio.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE,
                    AudioManager.FLAG_SHOW_UI
                );
            }
            reply("आवाज वाढवला.");

        } else if (cmd.contains("settings") || cmd.contains("सेटिंग")
                || cmd.contains("सेटिंग्ज")) {
            reply("सेटिंग्स उघडत आहे.");
            launch(new Intent(Settings.ACTION_SETTINGS));

        } else if (cmd.contains("search") || cmd.contains("शोध")
                || cmd.contains("सर्च")) {
            String query = cmd.replaceFirst(
                "(search|शोध|सर्च)", ""
            ).trim();
            if (query.isEmpty()) {
                reply("काय शोधायचे ते सांगा.");
                return;
            }
            reply("वेबवर शोधत आहे.");
            launch(new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/search?q=" +
                    Uri.encode(query))
            ));

        } else if (cmd.contains("hello") || cmd.contains("hi")
                || cmd.contains("नमस्कार")) {
            reply("नमस्कार! मी तुमचा Adi AI Assistant आहे.");

        } else {
            reply("माफ करा, ही कमांड अजून समजली नाही.");
        }
    }

    private void toggleTorch() {
        if (android.os.Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{Manifest.permission.CAMERA}, 11
            );
            return;
        }

        try {
            CameraManager manager = (CameraManager)
                getSystemService(CAMERA_SERVICE);
            String cameraId = null;
            for (String id : manager.getCameraIdList()) {
                android.hardware.camera2.CameraCharacteristics c =
                    manager.getCameraCharacteristics(id);
                Boolean flash = c.get(
                    android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE
                );
                if (Boolean.TRUE.equals(flash)) {
                    cameraId = id;
                    break;
                }
            }
            if (cameraId == null) {
                reply("या फोनवर टॉर्च उपलब्ध नाही.");
                return;
            }
            torchOn = !torchOn;
            manager.setTorchMode(cameraId, torchOn);
            reply(torchOn ? "टॉर्च सुरू केला." : "टॉर्च बंद केला.");
        } catch (Exception e) {
            reply("टॉर्च सुरू करता आला नाही.");
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
