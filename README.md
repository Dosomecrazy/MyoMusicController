MyoMusicController
==================

This is a very simple app to control the default music player on Android phones.

If you want to compile it by yourself specify the path to the Myo SDK on the file MyoMusicController/build.gradle

This app is based on the Background example provided in the SDK and only supports the following commands:

* Wave in: Previous song
* Wave out: Next song
* Spread fingers: Play/Pause

The default unlock gesture, thumb to pinky, generates quite a lot false positives in a day to day situation, or at least to me.
That's why I modified the unlock gesture for this app to be thumb to pinky followed by fist. After performing this more complex
unlock gesture you will feel a short vibration to let you know that the app is ready to receive commands. After two seconds without
receiving gestures it will lock automatically and the myo armband with vibrate twice to let you know it's locked.
