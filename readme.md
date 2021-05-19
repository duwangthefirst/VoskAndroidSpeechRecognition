### VoskVoiceRecognition

#### Description
A voice recognition demo for Android.
- based on [vosk-android-demo](https://github.com/alphacep/vosk-android-demo)
- support Chinese voice recognition with microphone as audio data source
- by change model file, you can easily change target language
- code is simplified and more readable
- future work:
  - train my own voice recognition model in the future and share the whole procedure

#### Usage
1. go to `vosk/src/main/assets/` folder
2. download model zip file from [official model list](https://alphacephei.com/vosk/models) (my choice is `vosk-model-small-cn-0.3`)
3. unzip the model zip file to `vosk/src/main/assets/` and change the result folder name from `vosk-model-small-cn-0.3` to `model-zh-cn`
4. accordingly, you should also change `vosk/build.gradle` line 23 to `def odir = file("$buildDir/generated/assets/model-zh-cn")`, and change `app/src/main/java/com/dfanr/voskvoicerecgnition/MainActivity.java` line 186(in function initModel()) to `StorageService.unpack(this, "model-zh-cn", "model", ...)`
5. finally the app is supposed to run with no problem.