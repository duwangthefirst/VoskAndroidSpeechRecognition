package com.dfanr.voskvoicerecgnition;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.dfanr.voskvoicerecgnition.databinding.ActivityMainBinding;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.StorageService;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;

    private Model voskModel;
    private SpeechService speechService;

    public enum VoskState {
        START,
        READY,
        START_RECOGNIZING,
        STOP_RECOGNIZING
    }

    private VoskState voskState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        // todo：状态变为start
        setVoskState(VoskState.START);
        requestPermission();
        initButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
        }
    }

    private void requestPermission() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.RECORD_AUDIO)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        initModel();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(MainActivity.this, "被应用需要录音权限才能正常运行", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        Toast.makeText(MainActivity.this, "本应用需要录音权限以便执行语音识别操作", Toast.LENGTH_SHORT).show();
                    }
                }).check();
    }

    private void setVoskState(VoskState voskState) {
        this.voskState = voskState;
        switch (this.voskState) {
            case START:
                binding.button.setEnabled(false);
                binding.textView.setText("正在准备语音识别模块");
                break;
            case READY:
                binding.button.setEnabled(true);
                binding.textView.setText("语音识别模块已经准备就绪");
                break;
            case START_RECOGNIZING:
                binding.button.setEnabled(true);
                binding.button.setText("停止识别");
                binding.textView.setText("说些什么吧！");
                break;
            case STOP_RECOGNIZING:
                binding.button.setEnabled(true);
                binding.button.setText("开始识别");

                break;
            default:
                Log.e(TAG, "setVoskState: unknown argument");
                break;
        }
    }

    private void setErrorMessage(String message) {
        binding.button.setEnabled(false);
        binding.button.setText("开始识别");
        binding.textView.setText(message);
    }

    private void initButton() {

        binding.button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startRecognizing();
                        break;
                    case MotionEvent.ACTION_UP:
                        stopRecognizing();
                        break;
                }
                return false;
            }
        });


    }

    private void stopRecognizing(){
        if (speechService != null) {
            // 停止识别
            setVoskState(VoskState.STOP_RECOGNIZING);
            speechService.stop();
            speechService = null;
        }
    }

    private void startRecognizing(){
        // 开始识别
        setVoskState(VoskState.START_RECOGNIZING);
        try {
            Recognizer recognizer = new Recognizer(voskModel, 16000.0f);
            speechService = new SpeechService(recognizer, 16000.0f);
            speechService.startListening(new RecognitionListener() {
                @Override
                public void onPartialResult(String s) {
                    binding.textView.append("\n"+"onPartialResult"+s);
                }

                @Override
                public void onResult(String s) {
                    binding.textView.append("\n" + "onResult" + s);
                }

                @Override
                public void onFinalResult(String s) {
                    binding.textView.append("\n" + "onFinalResult" + s);
                    setVoskState(VoskState.STOP_RECOGNIZING);
                }

                @Override
                public void onError(Exception e) {
                    setErrorMessage(e.getMessage());
                }

                @Override
                public void onTimeout() {
                    setVoskState(VoskState.STOP_RECOGNIZING);
                }
            });
        } catch (IOException e) {
            setErrorMessage(e.getMessage());
        }
    }

    private void initModel() {
        StorageService.unpack(this, "model-zh-cn", "model", new StorageService.Callback<Model>() {
            @Override
            public void onComplete(Model model) {
                voskModel = model;
                // todo：状态变为ready
                setVoskState(VoskState.READY);
            }
        }, new StorageService.Callback<IOException>() {
            @Override
            public void onComplete(IOException e) {
                // todo：状态变为错误
                setErrorMessage(e.getMessage());
            }
        });
    }
}