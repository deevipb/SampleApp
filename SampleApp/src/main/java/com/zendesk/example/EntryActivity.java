package com.zendesk.example;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.zendesk.sdk.logger.Logger;
import com.zendesk.sdk.model.AuthenticationType;
import com.zendesk.sdk.network.impl.ZendeskGsonProvider;
import com.zendesk.sdk.storage.SdkStorage;
import com.zendesk.sdk.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Allows a simple entry of a subdomain like "mysubdomain" or as an IP address.
 * <p>
 *     "mysubdomain" will be changed to https://mysubdomain.zendesk.com but IP addresses will
 *     not be changed.  No validation is carried out on the entry so please type carefully.
 * </p>
 */
public class EntryActivity extends ActionBarActivity {

    static final String LOG_TAG = EntryActivity.class.getSimpleName();
    static final String PREFERENCES_FILE = "EntryActivityAutocompleteModel";
    static final String MODEL_KEY = "ModelJson";

    private AutoCompleteTextView mSubdomainEditText;
    private AutoCompleteTextView mApplicationIdEditText;
    private AutoCompleteTextView mOauthClientIdEditText;

    private RadioButton mJwtAccessRadioButton;
    private RadioButton mAnonymousAccessRadioButton;

    private EditText mJwtUserIdentifierEditText;

    private EditText mAnonymousNameEditText;
    private EditText mAnonymousEmailEditText;
    private EditText mAnonymousExternalIdEditText;

    private View mJwtDetailsContainer;
    private View mAnonymousDetailsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//lllllllutrhjrtjhrjthijfrtttttg
        /**
         * Set that our Logger will log. This doesn't rely on {@link com.zendesk.example.BuildConfig}
         * because debug and production variables are currently not propagated to library projects
         * by the Android build system
         */
        Logger.setLoggable(true);

        setContentView(R.layout.activity_entry);

        mSubdomainEditText = (AutoCompleteTextView) findViewById(R.id.activity_entry_subdomain_edittext);
        mApplicationIdEditText = (AutoCompleteTextView) findViewById(R.id.activity_entry_appid_edittext);
        mOauthClientIdEditText = (AutoCompleteTextView) findViewById(R.id.activity_entry_clientid_edittext);

        mJwtAccessRadioButton = (RadioButton) findViewById(R.id.activity_entry_jwt_radiobutton);
        mAnonymousAccessRadioButton = (RadioButton) findViewById(R.id.activity_entry_anon_radiobutton);

        mJwtUserIdentifierEditText = (EditText) findViewById(R.id.activity_entry_jwt_useridentifer_edittext);
        mAnonymousNameEditText = (EditText) findViewById(R.id.activity_entry_anon_name_edittext);
        mAnonymousEmailEditText = (EditText) findViewById(R.id.activity_entry_anon_email_edittext);
        mAnonymousExternalIdEditText = (EditText) findViewById(R.id.activity_entry_anon_external_id_edittext);

        mJwtAccessRadioButton.setOnClickListener(mRadioButtonListener);
        mAnonymousAccessRadioButton.setOnClickListener(mRadioButtonListener);

        mJwtDetailsContainer = findViewById(R.id.activity_entry_jwt_details_container);
        mAnonymousDetailsContainer = findViewById(R.id.activity_entry_anon_details_container);

        final EntryActivityAutocompleteModel model = EntryActivityAutocompleteModel.get(this);

        setupAutocomplete(mSubdomainEditText, model.getSubdomains());
        setupAutocomplete(mApplicationIdEditText, model.getApplicationIds());
        setupAutocomplete(mOauthClientIdEditText, model.getOauthClientIds());

        String zendeskUrl = mSubdomainEditText.getText().toString().trim();
        String applicationId = mApplicationIdEditText.getText().toString().trim();
        String oauthClientId = mOauthClientIdEditText.getText().toString().trim();
        zendeskUrl = "https://devpbi.zendesk.com";
        applicationId = "06af82cbbf2c0a4e0d0583a94773fad9a7072e9c5d26330a";
        oauthClientId = "mobile_sdk_client_e33be0ced0387e8c802c";

        mSubdomainEditText.setText(zendeskUrl);
        mApplicationIdEditText.setText(applicationId);
        mOauthClientIdEditText.setText(oauthClientId);
        findViewById(R.id.activity_entry_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean hasValidData = StringUtils.hasLength(mSubdomainEditText.getText().toString())
                        && StringUtils.hasLength(mApplicationIdEditText.getText().toString())
                        && StringUtils.hasLength(mOauthClientIdEditText.getText().toString());

                boolean hasValidIdentityData = mAnonymousAccessRadioButton.isChecked()
                        || (mJwtAccessRadioButton.isChecked() && StringUtils.hasLength(mJwtUserIdentifierEditText.getText().toString()));

                if (hasValidData && hasValidIdentityData) {

                    Intent startMainActivity = new Intent(EntryActivity.this, MainActivity.class);

                    String zendeskUrl = mSubdomainEditText.getText().toString().trim();
                    String applicationId = mApplicationIdEditText.getText().toString().trim();
                    String oauthClientId = mOauthClientIdEditText.getText().toString().trim();
                    zendeskUrl = "https://devpbi.zendesk.com";
                    applicationId = "06af82cbbf2c0a4e0d0583a94773fad9a7072e9c5d26330a";
                    oauthClientId = "mobile_sdk_client_e33be0ced0387e8c802c";

                    mSubdomainEditText.setText(zendeskUrl);
                    mApplicationIdEditText.setText(applicationId);
                    mOauthClientIdEditText.setText(oauthClientId);

                    if (zendeskUrl.indexOf('.') == -1) {
                        zendeskUrl = "https://" + zendeskUrl + ".zendesk.com";
                    }

                    startMainActivity.putExtra(MainActivity.EXTRA_ZENDESK_URL, zendeskUrl);
                    Logger.d(LOG_TAG, "Using Zendesk URL: " + zendeskUrl);

                    startMainActivity.putExtra(MainActivity.EXTRA_ZENDESK_APPLICATION_ID, applicationId);
                    Logger.d(LOG_TAG, "Using Application ID: " + applicationId);

                    model.getSubdomains().add(zendeskUrl);
                    model.getApplicationIds().add(applicationId);

                    // Don't want to be saving empty strings.
                    if (StringUtils.hasLength(oauthClientId)) {
                        startMainActivity.putExtra(MainActivity.EXTRA_ZENDESK_OAUTH_CLIENT_ID, oauthClientId);
                        Logger.d(LOG_TAG, "Using Oauth Client Id: " + oauthClientId);

                        model.getOauthClientIds().add(oauthClientId);
                    } else {
                        Logger.d(LOG_TAG, "Ignoring oauth client id as it is unset");
                    }

                    model.save(EntryActivity.this);

                    String authenticationType = mJwtAccessRadioButton.isChecked()
                            ? AuthenticationType.JWT.getAuthenticationType()
                            : AuthenticationType.ANONYMOUS.getAuthenticationType();
                    startMainActivity.putExtra(MainActivity.EXTRA_AUTHENTICATION_TYPE, authenticationType);

                    if (mJwtAccessRadioButton.isChecked()) {
                        startMainActivity.putExtra(MainActivity.EXTRA_JWT_USER_IDENTIFIER, mJwtUserIdentifierEditText.getText().toString());
                    } else {
                        startMainActivity.putExtra(MainActivity.EXTRA_ANONYMOUS_NAME, mAnonymousNameEditText.getText().toString());
                        startMainActivity.putExtra(MainActivity.EXTRA_ANONYMOUS_EMAIL, mAnonymousEmailEditText.getText().toString());
                        startMainActivity.putExtra(MainActivity.EXTRA_ANONYMOUS_EXTERNAL_ID, mAnonymousExternalIdEditText.getText().toString());

                    }

                    startActivity(startMainActivity);
                    finish();

                } else {
                    Toast.makeText(
                            EntryActivity.this,
                            "All of the required parameters have not been set", Toast.LENGTH_SHORT).show();

                    Logger.e(LOG_TAG, "Nothing has been entered in the edit text");
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (R.id.activity_entry_menu_clear == item.getItemId()) {

            mSubdomainEditText.setText("");
            mApplicationIdEditText.setText("");
            mOauthClientIdEditText.setText("");

            mJwtUserIdentifierEditText.setText("");
            mAnonymousNameEditText.setText("");
            mAnonymousEmailEditText.setText("");
            mAnonymousExternalIdEditText.setText("");

            SdkStorage.INSTANCE.init(this);
            SdkStorage.INSTANCE.clearUserData();

            return true;
        }else if(R.id.activity_entry_menu_barcode == item.getItemId()){
            new IntentIntegrator(this).initiateScan();
            
            return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(intentResult != null && intentResult.getContents() != null) {
            final Uri parse = Uri.parse(intentResult.getContents());
            
            final String scheme = getResources().getString(R.string.intent_import_settings_scheme);
            final String host = getResources().getString(R.string.intent_import_settings_host);
            
            if (parse != null && scheme.equals(parse.getScheme()) && host.equals(parse.getAuthority())) {
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(intentResult.getContents()));
                startActivity(intent);
                finish();
            }else{
                Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private View.OnClickListener mRadioButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.activity_entry_jwt_radiobutton: {

                    mJwtDetailsContainer.setVisibility(View.VISIBLE);
                    mAnonymousDetailsContainer.setVisibility(View.GONE);

                    break;
                }
                case R.id.activity_entry_anon_radiobutton: {

                    mJwtDetailsContainer.setVisibility(View.GONE);
                    mAnonymousDetailsContainer.setVisibility(View.VISIBLE);

                    break;
                }
            }

        }
    };

    private void setupAutocomplete(AutoCompleteTextView autoCompleteTextView, Set<String> entries) {
        ArrayList<String> entriesList = new ArrayList<String>(entries);

        Collections.reverse(entriesList);

        autoCompleteTextView.setAdapter(
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        entriesList));
        if (entriesList.size() > 0) {
            autoCompleteTextView.setText(entriesList.get(0));
        }
    }

}

class EntryActivityAutocompleteModel {

    private final Set<String> subdomains;

    private final Set<String> applicationIds;

    private final Set<String> oauthClientIds;

    EntryActivityAutocompleteModel() {
        subdomains = new HashSet<String>();
        applicationIds = new HashSet<String>();
        oauthClientIds = new HashSet<String>();
    }

    public static EntryActivityAutocompleteModel get(Context context) {

        SharedPreferences modelStorage =
                context.getSharedPreferences(EntryActivity.PREFERENCES_FILE, Context.MODE_PRIVATE);

        String storedModelJson = modelStorage.getString(EntryActivity.MODEL_KEY, "");

        if (StringUtils.hasLength(storedModelJson)) {
            Logger.d(EntryActivity.LOG_TAG, "Returning autocomplete model from storage");

            Gson gson = ZendeskGsonProvider.INSTANCE.getZendeskGson();

            return gson.fromJson(storedModelJson, EntryActivityAutocompleteModel.class);

        } else {
            Logger.d(EntryActivity.LOG_TAG, "Returning new instance of autocomplete model");
            return new EntryActivityAutocompleteModel();
        }
    }

    public void save(Context context) {

        SharedPreferences modelStorage =
                context.getSharedPreferences(EntryActivity.PREFERENCES_FILE, Context.MODE_PRIVATE);

        Gson gson = ZendeskGsonProvider.INSTANCE.getZendeskGson();
        String modelJson = gson.toJson(this);

        Logger.d(EntryActivity.LOG_TAG, "Saving autocomplete model: " + modelJson);

        modelStorage.edit().putString(EntryActivity.MODEL_KEY, modelJson).apply();

    }

    public Set<String> getSubdomains() {
        return subdomains;
    }

    public Set<String> getApplicationIds() {
        return applicationIds;
    }

    public Set<String> getOauthClientIds() {
        return oauthClientIds;
    }

}
