package com.zendesk.example;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.zendesk.sdk.feedback.impl.BaseZendeskFeedbackConfiguration;
import com.zendesk.sdk.feedback.ui.ContactZendeskActivity;
import com.zendesk.sdk.logger.Logger;
import com.zendesk.sdk.model.AuthenticationType;
import com.zendesk.sdk.model.network.AnonymousIdentity;
import com.zendesk.sdk.model.network.ErrorResponse;
import com.zendesk.sdk.model.network.Identity;
import com.zendesk.sdk.model.network.JwtIdentity;
import com.zendesk.sdk.model.network.PushRegistrationResponse;
import com.zendesk.sdk.network.impl.ZendeskCallback;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.sdk.requests.RequestActivity;
import com.zendesk.sdk.support.SupportActivity;
import com.zendesk.sdk.util.StringUtils;

import java.util.Locale;

import retrofit.client.Response;

/**
 * This activity is a springboard that you can use to launch various parts of the Zendesk SDK.
 */
public class MainActivity extends FragmentActivity {

    private static final String LOG_TAG = "MainActivity";

    public static final String EXTRA_ZENDESK_URL = "https://devpbi.zendesk.com";
    public static final String EXTRA_ZENDESK_APPLICATION_ID = "06af82cbbf2c0a4e0d0583a94773fad9a7072e9c5d26330a";
    public static final String EXTRA_ZENDESK_OAUTH_CLIENT_ID = "mobile_sdk_client_e33be0ced0387e8c802c";

    public static final String EXTRA_AUTHENTICATION_TYPE = "anonymous";

    public static final String EXTRA_JWT_USER_IDENTIFIER  = "jwt_user_identifier";

    public static final String EXTRA_ANONYMOUS_NAME = "Mobile SDK App";
    public static final String EXTRA_ANONYMOUS_EMAIL = "sakchai.c@playbasis";
    public static final String EXTRA_ANONYMOUS_EXTERNAL_ID = "anonymous_external_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConfigDataProvider configData = new ConfigDataProvderImpl();;
        if(getIntent() != null && getIntent().hasExtra(EXTRA_ZENDESK_URL)) {
            configData = new ConfigDataProvderImpl(getIntent());
        }else if(getIntent() != null && getIntent().getData() != null){
            configData = new ConfigDataProvderImpl(getIntent().getData());
        }

        final String zendeskUrl = configData.getData(EXTRA_ZENDESK_URL);
        final String anonymousName = configData.getData(EXTRA_ANONYMOUS_NAME);
        final String anonymousEmail = configData.getData(EXTRA_ANONYMOUS_EMAIL);
        final String anonymousExternalId = configData.getData(EXTRA_ANONYMOUS_EXTERNAL_ID);
        final String applicationId = configData.getData(EXTRA_ZENDESK_APPLICATION_ID);
        final String oauthClientId = configData.getData(EXTRA_ZENDESK_OAUTH_CLIENT_ID);
        final String authenticationType = configData.getData(EXTRA_AUTHENTICATION_TYPE);
        final String jwtUserIdentifier = configData.getData(EXTRA_JWT_USER_IDENTIFIER);


        if (StringUtils.isEmpty(zendeskUrl) || StringUtils.isEmpty(applicationId) || StringUtils.isEmpty(oauthClientId)) {
            Logger.e(LOG_TAG, "You must supply a zendesk url and application id");
            finish();
        }

        /**
         * Initialises the SDK with authentication
         */
        ZendeskConfig.INSTANCE.init(this, zendeskUrl, applicationId, oauthClientId);
        Identity user;

        if (AuthenticationType.JWT.getAuthenticationType().equals(authenticationType)) {
            user = new JwtIdentity(jwtUserIdentifier);
        } else {
            user = new AnonymousIdentity.Builder()
                    .withEmailIdentifier(anonymousEmail)
                    .withNameIdentifier(anonymousName)
                    .withExternalIdentifier(anonymousExternalId)
                    .build();
        }

        ZendeskConfig.INSTANCE.setIdentity(user);

        //Setting Configuration for contact component
        ZendeskConfig.INSTANCE.setContactConfiguration(new SampleContactConfiguration(this));

        /**
         * This will make the RateMyApp dialog activity.
         */
        findViewById(R.id.main_btn_rate_my_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RateMyAppDialogTest.class));
            }
        });

        /**
         * This will make a full-screen feedback screen appear. It is very similar to how
         * the feedback dialog works but it is hosted in an activity.
         */
        findViewById(R.id.main_btn_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ContactZendeskActivity.class);
                startActivity(intent);
            }
        });

        /**
         * This will launch an Activity that will show the current Requests that a
         * user has opened.
         */
        findViewById(R.id.main_btn_request_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RequestActivity.class));
            }
        });

        final EditText supportEdittext = (EditText) findViewById(R.id.main_edittext_support);

        findViewById(R.id.main_btn_support).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String labels = supportEdittext.getText().toString();
                String[] labelsArray = null;

                if (StringUtils.hasLength(labels)) {
                    labelsArray = labels.split(",");
                }

                if (labelsArray != null) {

                    if(labelsArray.length == 1 && labelsArray[0].matches("-?\\d+")){
                        SupportActivity.startActivity(MainActivity.this, Long.parseLong(labelsArray[0]));

                    }else{
                        SupportActivity.startActivity(MainActivity.this, labelsArray);

                    }

                } else {
                    Intent intent = new Intent(MainActivity.this, SupportActivity.class);
                    startActivity(intent);
                }
            }
        });

        final EditText devicePushToken = (EditText) findViewById(R.id.main_edittext_push);

        findViewById(R.id.main_btn_push_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZendeskConfig.INSTANCE.enablePush(devicePushToken.getText().toString(), new ZendeskCallback<PushRegistrationResponse>() {
                    @Override
                    public void onSuccess(PushRegistrationResponse result) {
                        Toast.makeText(getApplicationContext(), "Registration success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(ErrorResponse error) {
                        Toast.makeText(getApplicationContext(), "Registration failure: " + error.getReason(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        findViewById(R.id.main_btn_push_unregister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZendeskConfig.INSTANCE.disablePush(devicePushToken.getText().toString(), new ZendeskCallback<Response>() {
                    @Override
                    public void onSuccess(Response result) {
                        Toast.makeText(getApplicationContext(), "Deregistration success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(ErrorResponse error) {
                        Toast.makeText(getApplicationContext(), "Deregistration failure: " + error.getReason(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
}

/**
 * This class will configure the feedback dialog with the minimum amount of options that
 * are required.
 */
class SampleContactConfiguration extends BaseZendeskFeedbackConfiguration {

    public final transient Context mContext;

    public SampleContactConfiguration(Context context) {
        this.mContext = context;
    }

    @Override
    public String getRequestSubject() {

        /**
         * A request will normally have a shorter subject and a longer description. Here we are
         * specifying the subject that will be on the request that is created by the feedback
         * dialog.
         */
        return mContext.getString(R.string.rate_my_app_dialog_feedback_request_subject);
    }
}



class ConfigDataProvderImpl implements ConfigDataProvider{
    
    private static final String LOG_TAG = ConfigDataProvderImpl.class.getSimpleName();
    
    private final ConfigDataProvider mConfigDataProvider;
    
    ConfigDataProvderImpl(){
        this.mConfigDataProvider = new ConfigDataProvider() {
            @Nullable
            @Override
            public String getData(String key) {
                return null;
            }
        };
    }
    
    ConfigDataProvderImpl(final Intent intent){
        this.mConfigDataProvider = new ConfigDataProvider() {
            @Nullable
            @Override
            public String getData(String key) {
                if(intent != null) {
                    return intent.getStringExtra(key);
                }
                return null;
            }
        };
    }
    
    ConfigDataProvderImpl(final Uri uri){
        this.mConfigDataProvider = new ConfigDataProvider() {
            @Nullable
            @Override
            public String getData(String key) {
                if(uri != null) {
                    return Uri.decode(uri.getQueryParameter(key));
                }
                return null;
            }
        };
    }

    @Nullable
    @Override
    public String getData(String key) {
        final String result = mConfigDataProvider.getData(key);
        Logger.d(LOG_TAG, String.format(Locale.US,"getData(%s) = %s", key , result));
        return result;
    }
}

interface ConfigDataProvider {
    @Nullable
    public String getData(String key);
}