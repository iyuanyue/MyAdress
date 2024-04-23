package com.iyuanyue.car.Navigation;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.Log;

import java.util.ArrayList;

public class SpeechRecognitionHelper {
    private static final int SPEECH_REQUEST_CODE = 123;
    private Activity activity;
    private SpeechRecognitionListener mListener;

    public SpeechRecognitionHelper(Activity activity) {
        this.activity = activity;
    }

    // 设置回调接口
    public void setSpeechRecognitionListener(SpeechRecognitionListener listener) {
        this.mListener = listener;
    }

    // 启动语音识别
    public void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        activity.startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // 处理语音识别结果
    public void handleSpeechRecognitionResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && result.size() > 0 && mListener != null) {
                String recognizedText = result.get(0);
                mListener.onSpeechRecognized(recognizedText);
                Log.d("SpeechRecognitionHelper", "Recognized text: " + recognizedText);
            }
        }
    }

    // 定义一个接口，用于处理语音识别结果
    public interface SpeechRecognitionListener {
        void onSpeechRecognized(String recognizedText);
    }
}
