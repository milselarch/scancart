package com.example.charles_nfc;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

//Singleton Design Pattern to ensure only one FirebaseFirestore instance
public class FirebaseHandler {
    protected static FirebaseFirestore fStore;
    protected static FirebaseAuth fAuth;
    protected FirebaseHandler() {

    };

    public static FirebaseFirestore getInstanceDatabase(){
        if (fStore == null) {
            fStore = FirebaseFirestore.getInstance();
        }
        return fStore;
    }
    public static FirebaseAuth getInstanceAuth(){
        if (fAuth == null){
            fAuth = FirebaseAuth.getInstance();
        }
        return fAuth;
    }

    public static String getUID(){
        return fAuth.getUid();
    }

    public static String getPhone() {
        return fAuth.getCurrentUser().getPhoneNumber();
    }

    //Non static nested class to improve code maintenance since it is only used here
    class User{
        String userUID;
        String userName;
        String userPhoneNumber;
        String userStreetAddress;
        String userPostalCode;
        String userFloorAndUnit;
        String userHealthConditions;
        Long user_id;

        public User(){}

        public User(
                String UUID, String Name, String phoneNumber, String streetAddress, String postalCode,
                String floorAndUnit, String healthConditions, Long userID
        ) {
            this.userUID = UUID;
            this.userName = Name;
            this.userPhoneNumber = phoneNumber;
            this.userStreetAddress = streetAddress;
            this.userPostalCode = postalCode;
            this.userFloorAndUnit = floorAndUnit;
            this.userHealthConditions = healthConditions;
            this.user_id = userID;
        }

        public Long getUser_id() {return this.user_id; }
        public String getUserUID(){
            return this.userUID;
        }
        void setUserUID(String userUID){
            this.userUID = userUID;
        }
        public String getUserName(){
            return this.userName;
        }
        void setUserName(String Name){
            this.userName = Name;
        }
        public String getUserPhoneNumber(){
            return this.userPhoneNumber;
        }
        void setUserPhoneNumber(String phoneNumber){
            this.userPhoneNumber = phoneNumber;
        }
        public String getUserStreetAddress(){
            return this.userStreetAddress;
        }
        void setUserStreetAddress(String streetAddress){this.userStreetAddress = streetAddress;}
        public String getUserPostalCode(){return this.userPostalCode;}
        void setUserPostalCode(String postalCode){this.userPostalCode = postalCode;}
        public String getUserFloorAndUnit(){return this.userFloorAndUnit;}
        void setUserFloorAndUnit(String floorAndUnit){this.userFloorAndUnit = floorAndUnit;}
        public String getUserHealthConditions(){return this.userHealthConditions;}
        void setUserHealthConditions(String healthConditions){this.userHealthConditions = healthConditions;}
    }

    public static void registerUser(
            User user,
            FirebaseFirestore fStore,
            FireCallback onRegisterComplete
    ) {
        Log.d(TAG, "" + user.getUserUID());
        DocumentReference documentReference = fStore.collection("users").document(user.getUserUID());
        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "User Created");
                onRegisterComplete.callback(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("EDIT_USER_FAIL", e.toString());
                onRegisterComplete.callback(false);
            }
        });
    }

    public static void editUser(
            User user,
            FirebaseFirestore fStore,
            FireCallback onEditComplete
    ) {
        Log.d("EDIT_USER", "" + user.getUserUID());
        DocumentReference documentReference = fStore.collection("users").document(user.getUserUID());
        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "User Edited");
                onEditComplete.callback(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("EDIT_USER_FAIL", e.toString());
                onEditComplete.callback(false);
            }
        });
    }

    interface FireCallback {
        void callback(Object result);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public CompletableFuture<DocumentSnapshot> loadUserInfo(String UUID)
            throws InterruptedException
    {
        assert UUID != null;
        getInstanceDatabase();

        CompletableFuture<DocumentSnapshot> completableFuture = new CompletableFuture<>();
        CollectionReference collection = fStore.collection("users");
        DocumentReference result = collection.document(UUID);

        result.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    completableFuture.completeExceptionally(
                            new FirebaseFirestoreException(
                                    "firestore load failed",
                                    FirebaseFirestoreException.Code.DATA_LOSS
                            )
                    );
                }
                DocumentSnapshot user = task.getResult();
                completableFuture.complete(user);
            }
        });

        return completableFuture;
    };

    public void getMaxUserID(
            FireCallback resultHandler
    ) {
        getInstanceDatabase();
        fStore.collection("users")
                .orderBy("user_id", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                               if (!task.isSuccessful()) {
                                                   resultHandler.callback(-1);
                                               }

                                               Long maxUserID = 0L;
                                               QuerySnapshot snapshot = task.getResult();
                                               for (QueryDocumentSnapshot doc : snapshot) {
                                                   maxUserID = (Long) doc.get("user_id");
                                               }

                                               Log.d("MAX_USER_ID", String.valueOf(maxUserID));
                                               resultHandler.callback(maxUserID);
                                           }
                                       }
                );
    }

    public boolean loadUserID(FireCallback resultHandler) {
        String UUID = null;

        try {
            UUID = fAuth.getCurrentUser().getUid();
        } catch (NullPointerException e) {
            // callback is passed e is user is not logged in in fireAuth
            Log.e("FIRE_ERROR", "NO UUID");
            resultHandler.callback(e);
            return false;
        }

        getInstanceDatabase();
        fStore.collection("users")
                .whereEqualTo("userUID", UUID)
                .addSnapshotListener(
                        new EventListener<QuerySnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onEvent(
                                    @Nullable QuerySnapshot value,
                                    @Nullable FirebaseFirestoreException error
                            ) {
                                if (error != null) {
                                    // callback is passed null if firebase has error
                                    resultHandler.callback(error);
                                    Log.e("FIRE_ERROR", "NO MATCHING UUID");
                                    return;
                                }

                                for (QueryDocumentSnapshot doc : value) {
                                    Map<String, Object> docData = doc.getData();
                                    int userID = -1;

                                    try {
                                        userID = Math.toIntExact((Long) docData.get("user_id"));
                                    } catch (NullPointerException e) {
                                        Log.e("FIRE_ERROR", docData.toString());
                                        Log.e("FIRE_ERROR", "NO user_id FIELD");
                                        resultHandler.callback(e);
                                    }

                                    resultHandler.callback(userID);
                                    return;
                                }

                                // userID not set for current user
                                resultHandler.callback(-1);
                            }
                        }
                );

        return true;
    }

    public void FirebaseCheckOut(
            String str_date,
            String str_time,
            List<Map> shopping_cart_array,
            int userID,
            String orderID,
            double total_cost,
            String address,
            FireCallback callback
    ) {
        getInstanceDatabase();
        CollectionReference cart = fStore.collection("completed_orders");

        Map<String, Object> docData = new HashMap<>();
        docData.put("delivery_date",str_date);
        docData.put("delivery_time",str_time);
        docData.put("delivery_status","Delivery in Progress");
        docData.put("items", shopping_cart_array);
        docData.put("user_id", userID);
        docData.put("order_id", orderID);
        docData.put("total_cost", total_cost);
        docData.put("address", address);

        cart.add(docData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.e(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error writing document", e);
                    }
                });

        Map<String,Object> checkOutList = new HashMap<>();

        fStore.collection("shopping_cart")
                .whereEqualTo("user_id", userID)
                .get()
                .addOnSuccessListener((querySnapshot) -> {
                    WriteBatch batch = fStore.batch();
                    for (DocumentSnapshot doc : querySnapshot) {
                        batch.delete(doc.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener((result) -> {
                                Log.i(TAG, "All items have been removed.");
                                callback.callback(null);
                            })
                            .addOnFailureListener((error) -> {
                                Log.e(TAG, "Failed to remove all items.", error);
                            });
                })
                .addOnFailureListener((error) -> {
                    Log.e(TAG, "Failed to get your cart items.", error);
                });
    }

    public void minusQuantity(
            String tagID,
            Long block_remove,
            int userID,
            TextView name
    ) {
        getInstanceDatabase();
        fStore.collection("shopping_cart")
                .whereEqualTo("user_id", userID)
                .whereEqualTo("tag_id", tagID)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        if (block_remove == 1) {
                            Toast.makeText(
                                    name.getContext(),
                                    "Quantity cannot be less than 1",
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else {
                            fStore.collection("shopping_cart").document(document.getId()).update("quantity", FieldValue.increment(-1));
                        }
                    }
                } else {
                    Log.d("Delete Item", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    public void plusQuantity(
            String tagID,
            int userID
    ) {
        getInstanceDatabase();
        fStore.collection("shopping_cart")
                .whereEqualTo("user_id", userID)
                .whereEqualTo("tag_id", tagID)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        fStore.collection("shopping_cart").document(document.getId()).update("quantity", FieldValue.increment(1));;
                    }
                } else {
                    Log.d("Delete Item", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    public void removeItem(
            TextView name,
            int userID,
            String tagID
    ) {
        getInstanceDatabase();
        // Confirmation Dialog Box
        AlertDialog.Builder builder = new AlertDialog.Builder(name.getContext());
        builder.setTitle("Item will be removed from cart");
        builder.setMessage("Are you sure you would like to remove item?");

        // If user click "Yes"
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                fStore.collection("shopping_cart")
                        .whereEqualTo("user_id", userID)
                        .whereEqualTo("tag_id", tagID)
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                fStore.collection("shopping_cart").document(document.getId()).delete();
                            }
                        } else {
                            Log.d("Delete Item", "Error getting documents: ", task.getException());
                        }
                    }
                });
                dialog.dismiss();
            }

        });

        // If user click "No"
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void getCompletedOrder(
            String order_id,
            FireCallback callback
    ) {
        getInstanceDatabase();
        CollectionReference orders_completed = fStore.collection("completed_orders");
        Query query = orders_completed.whereEqualTo("order_id", order_id);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d("ITEM_STATUS", String.valueOf(task.isSuccessful()));
                if (!task.isSuccessful()) {
                    callback.callback(-1);
                };

                List<Model.Grocery> groceries = new ArrayList<>();
                QuerySnapshot snapshot = task.getResult();
                Log.d("SNAPSHOT", snapshot.toString());

                for (DocumentSnapshot document: task.getResult()) {
                    List<Map> list = (List<Map>) document.get("items");
                    Log.d("LIST_VALUES", list.toString());
                    Log.d("DOC_DATA", document.toString());

                    for (Map item : list) {
                        Log.d("ADD_ITEM", item.toString());
                        String cost = String.valueOf(item.get("cost"));
                        String image_url = (String) item.get("image_url");
                        String name = (String) item.get("name");
                        String quantity = String.valueOf(item.get("quantity"));
                        String tag_id = (String) item.get("tag_id");

                        Log.d("TAG", String.valueOf(item));
                        groceries.add(new Model.Grocery(
                                cost,image_url,name,quantity,tag_id
                        ));
                    }
                }
                callback.callback(groceries);
            }
        });
    }


}
