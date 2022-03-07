package com.example.charles_nfc;

import static android.content.ContentValues.TAG;
import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(
                this, "This device doesn't support NFC.",
                Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        } else {
            Intent targetIntent = new Intent(this, getClass());
            int intentFlag = PendingIntent.FLAG_UPDATE_CURRENT;
            if (SDK_INT >= Build.VERSION_CODES.S) {
                intentFlag = PendingIntent.FLAG_MUTABLE;
            }

            mPendingIntent = PendingIntent.getActivity(
                this, 0,
                targetIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                intentFlag
            );
        }

        TextView ScanTitle = (TextView) findViewById(R.id.scan_title);
        TextView ScanID = (TextView) findViewById(R.id.scan_nfc_id);

        if (!mNfcAdapter.isEnabled()) {
            ScanID.setText(R.string.nfc_disabled);
        } else {
            ScanID.setText(R.string.nfc_enabled);
        }

        // handleIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNfcAdapter.enableForegroundDispatch(
            this, mPendingIntent, null, null
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final String nfc_content = getTagInfo(intent);
        if (nfc_content == null) { return; }

        TextView ScanID = (TextView) findViewById(R.id.scan_nfc_id);
        ScanID.setText(nfc_content);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference citiesRef = db.collection(
            "inventory"
        );

        Query query = citiesRef.whereEqualTo("tag_id", nfc_content);
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
        ImageView itemImageView = (ImageView) findViewById(R.id.item_image);
        TextView scanTitleView = (TextView) findViewById(R.id.scan_title);
        TextView caloriesTextInfo = (TextView) findViewById(R.id.item_calories);
        TextView carbsTextInfo = (TextView) findViewById(R.id.item_carbs);
        TextView proteinTextInfo = (TextView) findViewById(R.id.item_protein);
        TextView fatsTextInfo = (TextView) findViewById(R.id.item_fats);
        TextView tagIdView = (TextView) findViewById(R.id.scan_nfc_id);

        tagIdView.setText(tagID);

        if (documentData == null) {
            scanTitleView.setText(R.string.unknown_item);
            caloriesTextInfo.setText(R.string.na);
            itemImageView.setImageResource(R.drawable.shopping_cart);
            carbsTextInfo.setText(R.string.na);
            proteinTextInfo.setText(R.string.na);
            fatsTextInfo.setText(R.string.na);
            return;
        }

        Long calories = (Long) documentData.get("calories");
        Double carbs = parseDouble(documentData.get("carbs"));
        Double protein = parseDouble(documentData.get("protein"));
        Double fats = parseDouble(documentData.get("fats"));
        String displayName = (String) documentData.get("display_name");
        String imageUrl = (String) documentData.get("image_url");

        assert calories != null;
        assert carbs != null;
        assert protein != null;
        assert fats != null;

        scanTitleView.setText(displayName);
        caloriesTextInfo.setText(calories.toString());
        carbsTextInfo.setText(carbs.toString() + "g");
        proteinTextInfo.setText(protein.toString() + "g");
        fatsTextInfo.setText(fats.toString() + "g");

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
        Bundle extras = getIntent().getExtras();
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