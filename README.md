# Scancart

Scancart is an app that utilises the “tap-and-go” capability of Near Field Communication (NFC) in smartphones to allow elderly to obtain their groceries conveniently and monitor their nutritional intake based on their health profiles. Created for the 1D project of the 50.004 (information systems) module.

Team 4-F members:  
Lim Thian Yew (1003158)  
Ryan Pan Tang Kai (1005037)  
Sean Soo Jun Hao (1005263)  
Tao Sihan (1005515)  
Lawrence Chen Qing Zhao (1005012)  
Constance Chua Jie Ning (1005499)

https://user-images.githubusercontent.com/11241733/165093083-6bc7c7c6-1d57-4631-967f-1d4792dc405b.mp4

![poster](https://github.com/milselarch/scancart/blob/master/poster.png)

### Navigating the repo
Our final 1D app is located under [scancart/app](https://github.com/milselarch/scancart/tree/master/scancart/app), while the subapps folder contains smaller subapps made by team members individually as they were working on their own individual assigned app features during the earlier phases of the development process (The plan was that we would each implement our own assigned features as a standalone app, then merge everything together to create our final app).  

![Screenshot from 2022-04-25 15-31-35](https://user-images.githubusercontent.com/11241733/165041246-263843ec-978f-45fa-9c42-066dab4ef882.png)
<em><p align="center">
User flow state transistion diagram of our app for reference ([] square brackets denote fragment / activity name). Each square denotes an activity, while each circle denotes a fragement within our app.
</p></em>

### Caveats about running the app
1. Our app requires access to firebase's firestore database, and authenticating access for our app to the firestore database requires that we put a [google-services.json](https://github.com/milselarch/scancart/releases/download/v1.0.0/google-services.json) file under the [root folder](https://github.com/milselarch/scancart/tree/master/scancart/app) of our app. We've placed a [google-services.json.example](https://github.com/milselarch/scancart/blob/master/scancart/app/google-services.json.example) but the actual credentials file is not stored in the repo for security reasons. Running the app after pulling the code from the repo will require including google-services.json into the app's root folder. A working google-services.json credential file can be found in the [app release](https://github.com/milselarch/scancart/releases/tag/v1.0.0). Alternatively, the [app release](https://github.com/milselarch/scancart/releases/tag/v1.0.0) has my own local copy scancart.zip containing all the apps and subapps in the repo with [google-services.json](https://github.com/milselarch/scancart/releases/download/v1.0.0/google-services.json) already included.  

2. The NFC tags we used to represent the shopping items are encoded in plain text format, and our NFC scanning code is thus only programmed to handle decoding tags with content in plaintext format only. To write data to the tags I used the [NFC tag reader](https://play.google.com/store/apps/details?id=com.gonext.nfcreader&hl=en_SG&gl=US) app and went to write tags > write data > Plain text to write the tag_id to the NFC tag. Example tag ids that are currently within our firestore datbase include `POTATO_23`, `KITKAT_4`, `NUGGETS_7`, and `COLA_1`.  

3. The login / registration page requires a phone number in the following format `+CCXXXXXXXX`, where `CC` is the country code (e.g. 65) and `XXXXXXXX` is the phone number. Note that there is [an issue with firebase's SMS authentication](https://stackoverflow.com/questions/46751766/this-app-is-not-authorized-to-use-firebase-authentication-please-verify-that-the) where running the app on a new device / android studio install will cause firebase to complain that the app is no authorized to use firebase authentication, and resolving this issue requires that we [use the current android studio's gradlew to generate a SHA signature](https://stackoverflow.com/a/62362112) that has to be added to our firestore database in order for firebase authentication to allow the app built by your current android studio install to use SMS authentication. To bypass this, we have two testing phone numbers (`+6591234567` with test OTP `000000`, and `+6598765432` with test OTP `123456`) that can be used in the login page, which should be able bypass needing to sign the app and adding it to firestore before being able to login via SMS to access the rest of the app.

### Description of Java classes in the app

1. MainActivity  
Firebase Phone SMS Authentication for User Sign-In process.
2. FirebaseHandler  
Singleton instance of FirebaseFirestore, that is used by all other classes within the app to interact with the Firebase database. Firebase calls (User Registration, Removing items from Shopping Cart, Get Completed Orders etc.) are extracted to the FirebaseHandler to facilitate modularity and readability of code in other classes.
3. UserAccount  
Singleton instance managing the user ID that allows users to remain signed in using Shared Preference until the user choose to log out.
4. EditProfile  
Allows user to edit their Name, Phone Number, Street Address, Postal Code, Floor and Unit Number and Health Conditions.
5. Shop  
Near Field Communication Scanning of NFC Tags and decoding the payload into UTF-8 or UTF-16 format. Item is displayed with its nutritional information after it is successfully scanned and the payload decoded is used to query the Firebase database. Dialog Alert message is triggered if user has a health condition and scans an item that is not recommended for their health condition.
6. Cart  
Recycler View to show all items within the shopping cart of the user. Total cost of the items in the shopping cart is calculated and shown.
7. ShoppingCartItemModel  
Model for Shopping Cart Item with required attributes (Item Tag ID, name, quantity, cost, Image URL). This is used by the Cart and Checkout class.
8. ShoppingCartAdapterClass  
Adapter class to manage the views in the Recycler View of Cart class. Items are sorted by alphabetical order and additional functionality such as modification of quantity and removal of item from shopping cart are included in this class.
9. Checkout  
Confirmation of order, delivery address and delivery timeslots. Once order is checked out, the shopping cart is cleared and the order can be reviewed under the Delivery tab.
10. SelectTiming  
Use of Android Widget DatePicker and TimePicker to select delivery date and time.
11. Delivery  
List View to show all delivery orders, sorted by chronological order based on delivery dates.
12. Order  
Model for Order Item with required attributes (Delivery Date, Delivery Status, Order ID). Orders are sorted by chronological order based on delivery dates.
13. OrderAdapter  
Adapter class to manage the views in the List View of Delivery clas.
14. GroceryList  
Recycler View to show all items within an order.
15. Model  
Static Nest Class for Grocery Item with required attributes (Item Tag ID, name, quantity, cost, Image URL). This is used by the Grocery class.
16. GroceryAdapter  
Adapter class to manage the views in the Recycler View of GroceryList class.
