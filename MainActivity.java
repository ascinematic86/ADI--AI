package com.adiaivoice.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
 private static final int SPEECH=10;
 private EditText input;
 private TextView output;
 private TextToSpeech tts;
 private boolean torch=false;
 @Override public void onCreate(Bundle b){ super.onCreate(b);
  LinearLayout layout=new LinearLayout(this); layout.setOrientation(1); layout.setPadding(28,48,28,24);
  TextView title=new TextView(this); title.setText("Adi AI • Voice Assistant"); title.setTextSize(25); layout.addView(title);
  input=new EditText(this); input.setHint("मराठी / हिंदी / English मध्ये सांगा"); layout.addView(input);
  Button send=new Button(this); send.setText("Send Command"); layout.addView(send);
  Button mic=new Button(this); mic.setText("🎤 Speak"); layout.addView(mic);
  output=new TextView(this); output.setTextSize(18); output.setText("नमस्कार! मी Adi AI आहे."); layout.addView(output);
  setContentView(layout);
  tts=new TextToSpeech(this, status->{if(status==TextToSpeech.SUCCESS) tts.setLanguage(new Locale("mr","IN"));});
  send.setOnClickListener(v->runCommand(input.getText().toString()));
  mic.setOnClickListener(v->{ if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},1);return;} startListening(); });
 }
 private void startListening(){try{Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"mr-IN");i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"mr-IN");startActivityForResult(i,SPEECH);}catch(Exception e){reply("Voice recognition उपलब्ध नाही.");}}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==SPEECH&&result==RESULT_OK&&data!=null){ArrayList<String> words=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(words!=null&&!words.isEmpty()){input.setText(words.get(0));runCommand(words.get(0));}}}
 private void reply(String s){output.setText(s);if(tts!=null)tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"adi-reply");}
 private void launch(Intent i){try{startActivity(i);}catch(Exception e){reply("ही सुविधा या फोनवर उपलब्ध नाही.");}}
 private void runCommand(String command){String s=command.toLowerCase(Locale.ROOT).trim();if(s.isEmpty())return;
  try{
   if(s.contains("camera")||s.contains("कॅमेरा")||s.contains("कैमरा")){launch(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA));reply("कॅमेरा उघडत आहे.");}
   else if(s.contains("dial")||s.contains("call")||s.contains("फोन")||s.contains("कॉल")){launch(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:")));reply("डायलर उघडत आहे.");}
   else if(s.contains("settings")||s.contains("सेटिंग")){launch(new Intent(Settings.ACTION_SETTINGS));reply("सेटिंग्स उघडत आहे.");}
   else if(s.contains("alarm")||s.contains("अलार्म")){launch(new Intent(AlarmClock.ACTION_SET_ALARM));reply("अलार्म सेट करा.");}
   else if(s.contains("torch")||s.contains("flashlight")||s.contains("टॉर्च")){if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.CAMERA},2);reply("कॅमेरा परवानगी द्या आणि पुन्हा प्रयत्न करा.");return;}CameraManager cm=(CameraManager)getSystemService(CAMERA_SERVICE);String id=cm.getCameraIdList()[0];torch=!torch;cm.setTorchMode(id,torch);reply(torch?"टॉर्च सुरू केला.":"टॉर्च बंद केला.");}
   else if(s.contains("volume")||s.contains("आवाज")){AudioManager a=(AudioManager)getSystemService(AUDIO_SERVICE);a.adjustStreamVolume(AudioManager.STREAM_MUSIC,s.contains("down")||s.contains("कमी")?AudioManager.ADJUST_LOWER:AudioManager.ADJUST_RAISE,AudioManager.FLAG_SHOW_UI);reply("आवाज बदलला.");}
   else if(s.contains("search")||s.contains("शोध")){String q=s.replace("search","").replace("शोध","").trim();launch(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/search?q="+Uri.encode(q))));reply("वेबवर शोधत आहे.");}
   else reply("मी ऐकलं: "+command+". कॅमेरा, फोन, अलार्म, सेटिंग्स, टॉर्च, आवाज किंवा शोध वापरून पाहा.");
  }catch(Exception e){reply("कमांड पूर्ण करता आली नाही: "+e.getClass().getSimpleName());}
 }
 @Override protected void onDestroy(){if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
}
