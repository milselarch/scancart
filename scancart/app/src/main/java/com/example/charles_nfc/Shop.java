package com.example.charles_nfc;

import static android.content.ContentValues.TAG;
import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Shop extends Fragment {
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private Map<String, Object> itemDocument;
    private final Integer userID = 1;
    Context context;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        return inflater.inflate(
            R.layout.nfc_scan, container, false
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        loadUI();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(
                this.getContext(), "This device doesn't support NFC.",
                Toast.LENGTH_LONG
            ).show();
            return;
        }

        Activity parentActivity = getActivity();
        if (parentActivity == null) { return; }

        Intent targetIntent = new Intent(
            getActivity(), getActivity().getClass()
        );
        int intentFlag = PendingIntent.FLAG_UPDATE_CURRENT;
        if (SDK_INT >= Build.VERSION_CODES.S) {
            intentFlag = PendingIntent.FLAG_MUTABLE;
        }

        mPendingIntent = PendingIntent.getActivity(
            this.getContext(), 0,
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            intentFlag
        );
    }

    void loadUI() {
        View currentView = getView();
        if (currentView == null) { return; }
        TextView ScanTitle = currentView.findViewById(R.id.scan_title);
        TextView ScanID = currentView.findViewById(R.id.scan_nfc_id);

        if (!mNfcAdapter.isEnabled()) {
            ScanID.setText(R.string.nfc_disabled);
        } else {
            ScanID.setText(R.string.nfc_enabled);
        }

        Log.d("CART_BTTN", "BTTN");
        Button cartAddButton = currentView.findViewById(R.id.cart_add_button);
        cartAddButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemDocument == null) {
                    Toast.makeText(
                        context,
                        "Nothing to add!",
                        Toast.LENGTH_SHORT
                    ).show();
                } else {
                    addItemToCart();
                }
            }
        });
    }

    protected void addItemToCart() {
        /*
        cart item document will have 3 key-value pairs:
        user_id: matches user_id in users collection
        tag_id: matches NFC tag_id of items in inventory
        quantity: how much of the item is in the cart
        */
        Log.d("CART_ADD", itemDocument.toString());

        assert itemDocument != null;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference cart = db.collection("shopping_cart");
        String tagID = (String) itemDocument.get("tag_id");
        Query query = cart
            .whereEqualTo("user_id", userID)
            .whereEqualTo("tag_id", tagID);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(
                        context,
                        "Failed to read shopping cart",
                        Toast.LENGTH_SHORT
                    ).show();
                }

                boolean cart_has_items = false;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // we should only get a single document here
                    assert !cart_has_items;
                    String documentId = document.getId();
                    Map<String, Object> documentData = document.getData();
                    Log.i("DOC_CART_ID", documentId);
                    Log.i("DOC_CART_DATA", documentData.toString());
                    cart_has_items = true;

                    // increment shopping cart item quantity by 1
                    Long quantity = (Long) documentData.get("quantity");
                    assert quantity != null;
                    cart.document(documentId).update(
                        "quantity", quantity+1
                    );

                    Toast.makeText(
                        context,
                        "Added another item to cart",
                        Toast.LENGTH_SHORT
                    ).show();
                }

                if (!cart_has_items) {
                    // cart for that item is empty, create new document
                    Map<String, Object> cartItemData = new HashMap<>();
                    cartItemData.put("user_id", userID);
                    cartItemData.put("tag_id", tagID);
                    cartItemData.put("quantity", 1);

                    assert tagID != null;
                    String documentName = (
                        "cart-" + userID.toString() + "-" +
                        tagID.toString()
                    );
                    cart.document(documentName)
                        .set(cartItemData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(
                                    context,
                                    "Item added to cart",
                                    Toast.LENGTH_SHORT
                                ).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                                Toast.makeText(
                                    context,
                                    "Error adding item to cart",
                                    Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                    );
                }

                gotoCartFragment();
            }
        });
    }

    void gotoCartFragment() {
        FragmentManager manager = getParentFragmentManager();
        manager.beginTransaction().replace(
            R.id.container, new Cart()
        ).addToBackStack("").commit();
    }

    public void onResume() {
        super.onResume();
        mNfcAdapter.enableForegroundDispatch(
            getActivity(), mPendingIntent, null, null
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(getActivity());
        }
    }

    protected void onNewIntent(Intent intent) {
        View currentView = getView();
        if (currentView == null) { return; }
        final String nfc_content = getTagInfo(intent);
        if (nfc_content == null) { return; }

        TextView ScanID = currentView.findViewById(R.id.scan_nfc_id);
        ScanID.setText(nfc_content);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference inventory = db.collection(
            "inventory"
        );

        Query query = inventory.whereEqualTo("tag_id", nfc_content);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.println(Log.INFO, "FIRE_STATUS", task.toString());

                if (task.isSuccessful()) {
                    Log.println(Log.INFO, "FIRE_STATUS", "TRUE");
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();
                        Map<String, Object> documentData = document.getData();
                        updateItem(nfc_content, documentData);
                        return;
                    }
                    // no matching items found in firebase
                    updateItem(nfc_content, null);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    protected void updateItem(String tagID, Map<String, Object> documentData) {
        this.itemDocument = documentData;
        View currenView = getView();
        if (currenView == null) { return; }

        ImageView itemImageView = currenView.findViewById(R.id.item_image);
        TextView scanTitleView = currenView.findViewById(R.id.scan_title);
        TextView caloriesTextInfo = currenView.findViewById(R.id.item_calories);
        TextView carbsTextInfo = currenView.findViewById(R.id.item_carbs);
        TextView proteinTextInfo = currenView.findViewById(R.id.item_protein);
        TextView fatsTextInfo = currenView.findViewById(R.id.item_fats);
        TextView priceTextInfo = currenView.findViewById(R.id.item_price);
        TextView tagIdView = currenView.findViewById(R.id.scan_nfc_id);

        tagIdView.setText(tagID);

        if (documentData == null) {
            scanTitleView.setText(R.string.unknown_item);
            caloriesTextInfo.setText(R.string.na);
            itemImageView.setImageResource(R.drawable.shopping_cart);
            carbsTextInfo.setText(R.string.na);
            proteinTextInfo.setText(R.string.na);
            fatsTextInfo.setText(R.string.na);
            priceTextInfo.setText(R.string.na);
            return;
        }

        Long calories = (Long) documentData.get("calories");
        Double carbs = parseDouble(documentData.get("carbs"));
        Double protein = parseDouble(documentData.get("protein"));
        Double fats = parseDouble(documentData.get("fats"));
        Double price = parseDouble(documentData.get("price"));
        String displayName = (String) documentData.get("display_name");
        String imageUrl = (String) documentData.get("image_url");

        assert calories != null;
        assert carbs != null;
        assert protein != null;
        assert fats != null;
        assert price != null;

        scanTitleView.setText(displayName);
        caloriesTextInfo.setText(calories.toString());
        carbsTextInfo.setText(carbs.toString() + "g");
        proteinTextInfo.setText(protein.toString() + "g");
        fatsTextInfo.setText(fats.toString() + "g");
        priceTextInfo.setText("$" + price.toString());

        new DownloadImageTask(new DownloadImageTask.customListener() {
            @Override
            public void postExecute(Bitmap result) {
                itemImageView.setImageBitmap(result);
            }
        }).execute(imageUrl);
    }

    public static Double parseDouble(Object number) {
        try {
            return (Double) number;
        } catch (ClassCastException e) {
            Long longNumber = (Long) number;
            return (double) longNumber;
        }
    }

    public static String bundleToString(Bundle bundle) {
        // https://stackoverflow.com/questions/6474734/how-do-i-know-what-data-is-given-in-a-bundle
        if (bundle == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder("Bundle{");
        for (String key : bundle.keySet()) {
            stringBuilder.append(" ");
            stringBuilder.append(key);
            stringBuilder.append(" => ");
            stringBuilder.append(bundle.get(key)).append(";");
        }

        String string = stringBuilder.toString();
        string += " }Bundle";
        return string;
    }

    public static NdefMessage[] extractNDEF(Intent intent) {
        // https://developer.android.com/guide/topics/connectivity/nfc/nfc#obtain-info
        String type = intent.getType();
        String action = intent.getAction();
        if (!NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            return null;
        }

        Parcelable[] parcs = intent.getParcelableArrayExtra(
            NfcAdapter.EXTRA_NDEF_MESSAGES
        );

        if (parcs == null) { return null; }
        NdefMessage[] messages = new NdefMessage[parcs.length];
        for (int k = 0; k < messages.length; k++) {
            messages[k] = (NdefMessage) parcs[k];
        }

        return messages;
    }

    private String getTagInfo(Intent intent) {
        // https://stackoverflow.com/questions/12313596/how-to-read-nfc-tag/28565383#28565383
        Bundle extras = intent.getExtras();
        String str_bundle = bundleToString(extras);

        if (str_bundle == null) {
            Log.i("NFC_BUNDLE", "INTENT NULL");
        } else {
            Log.i("BUNDLE", str_bundle);
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();

            for (String s : techList) {
                Log.i("TAG_INFO", s);
            }
        }

        NdefMessage[] nfc_messages = extractNDEF(intent);
        if (nfc_messages == null) { return null; }
        NdefMessage ndefMessage = nfc_messages[0];
        NdefRecord firstRecord = ndefMessage.getRecords()[0];
        Log.println(Log.INFO, "NFC_RECORD", firstRecord.toString());

        // https://stackoverflow.com/questions/14607425/read-data-from-nfc-tag
        byte[] payload = firstRecord.getPayload();
        String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = payload[0] & 0077;
        String languageCode = new String(
            payload, 1, languageCodeLength, StandardCharsets.US_ASCII
        );

        String nfc_text;
        //Get the Text
        try {
            nfc_text = new String(
                payload, languageCodeLength + 1,
                payload.length - languageCodeLength - 1, textEncoding
            );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        Log.println(Log.INFO, "NFC_TEXT", nfc_text);
        return nfc_text;
    }
}