Scancart is an app that utilises the “tap-and-go” capability of Near Field Communication (NFC) in smartphones to allow elderly to obtain their groceries conveniently and monitor their nutritional intake based on their health profiles. Created for the 1D project of the 50.004 (information systems) module.

Team members:  
Lim Thian Yew (1003158)  
Ryan Pan Tang Kai (1005037)  
Sean Soo Jun Hao (1005263)  
Tao Sihan (1005515)  
Lawrence Chen Qing Zhao (1005012)  
Constance Chua Jie Ning (1005499)  

### Caveats about running the app:
Our final 1D app is located under [scancart/app](https://github.com/milselarch/scancart/tree/master/scancart/app), while the subapps folder contains smaller subapps made by team members individually as they were working on their own individual assigned app features during the earlier phases of the development process (The plan was that we would each implement our own assigned features as a standalone app, then merge everything together to create our final app).  

Our app requires access to firebase's firestore database, and authenticating access for our app to the firestore database requires that we put a [google-services.json](https://github.com/milselarch/scancart/releases/download/v1.0.0/google-services.json) file under the [root folder](https://github.com/milselarch/scancart/tree/master/scancart/app) of our app. We've placed a [google-services.json.example](https://github.com/milselarch/scancart/blob/master/scancart/app/google-services.json.example) but the actual credentials file is not stored in the repo for security reasons. Running the app after pulling the code from the repo will require including google-services.json into the app's root folder. A working google-services.json credential file can be found in the [app release](https://github.com/milselarch/scancart/releases/tag/v1.0.0). Alternatively, the [app release](https://github.com/milselarch/scancart/releases/tag/v1.0.0) has my own local copy scancart.zip containing all the apps and subapps in the repo with [google-services.json](https://github.com/milselarch/scancart/releases/download/v1.0.0/google-services.json) already included.  

The NFC tags we used to represent the shopping items are encoded in plain text format, and our NFC scanning code is thus only programmed to handle decoding tags with content in plaintext format only. To write data to the tags I used the [NFC tag reader](https://play.google.com/store/apps/details?id=com.gonext.nfcreader&hl=en_SG&gl=US) app and went to write tags > write data > Plain text to write the tag_id to the NFC tag. Example tag ids that are currently within our firestore datbase include `POTATO_23`, `KITKAT_4`, `NUGGETS_7`, and `COLA_1`.  

The login / registration page requires a phone number in the following format `+CCXXXXXXXX`, where `CC` is the country code (e.g. 65) and `XXXXXXXX` is the phone number. Note that there is [an issue with firebase's SMS authentication](https://stackoverflow.com/questions/46751766/this-app-is-not-authorized-to-use-firebase-authentication-please-verify-that-the) where running the app on a new device / android studio install will cause firebase to complain that the app is no authorized to use firebase authentication, and resolving this issue requires that we [use the current android studio's gradlew to generate a SHA signature](https://stackoverflow.com/a/62362112) that has to be added to our firestore database in order for firebase authentication to allow the app built by your current android studio install to use SMS authentication. To bypass this, we have two testing phone numbers (`+6591234567` with test OTP `000000`, and `+6598765432` with test OTP `123456`) that can be used in the login page, which should be able bypass needing to sign the app and adding it to firestore before being able to login via SMS to access the rest of the app.

![poster](https://github.com/milselarch/scancart/blob/master/poster.png)
