package com.example.charles_nfc;

import static android.app.PendingIntent.FLAG_MUTABLE;
import static android.os.Build.VERSION.SDK_INT;
import static java.util.Objects.isNull;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.os.Bundle;
import com.github.skjolber.ndef.externaltype.AndroidApplicationRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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
                intentFlag |= PendingIntent.FLAG_MUTABLE;
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
        getTagInfo(intent);
    }

    public static String bundle_to_string(Bundle bundle) {
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

    public static String extract_text(Intent intent) {
        String type = intent.getType();
        String action = intent.getAction();

        if (!NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            return null;
        }

        Parcelable[] parcs = intent.getParcelableArrayExtra(
            NfcAdapter.EXTRA_NDEF_MESSAGES
        );

        for (Parcelable p : parcs) {

        }

        return "";
    }

    private void getTagInfo(Intent intent) {
        // https://stackoverflow.com/questions/12313596/how-to-read-nfc-tag/28565383#28565383
        Bundle extras = getIntent().getExtras();
        String str_bundle = bundle_to_string(extras);

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
    }
}