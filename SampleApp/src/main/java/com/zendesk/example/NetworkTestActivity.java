package com.zendesk.example;

import android.os.Bundle;
import android.widget.TextView;

import com.zendesk.sdk.ui.NetworkAwareActionbarActivity;
import com.zendesk.sdk.util.NetworkUtils;

import java.util.Date;

/**
 * Shows an example of extending the {@link com.zendesk.sdk.ui.NetworkAwareActionbarActivity}.
 * <p>
 *      You can receive the {@link com.zendesk.sdk.ui.NetworkAwareActionbarActivity#onNetworkAvailable()}
 *      and {@link com.zendesk.sdk.ui.NetworkAwareActionbarActivity#onNetworkUnavailable()} events.  An
 *      example of how to use this would be to use these methods to hide or show a persistent "no network"
 *      message on your screens.
 * </p>
 */
public class NetworkTestActivity extends NetworkAwareActionbarActivity {

    private TextView mLogTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_test);

        mLogTextView = (TextView) findViewById(R.id.network_text_textview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLogTextView.append("isConnected() from active network: " + NetworkUtils.isConnected(this) + "\n");
    }

    @Override
    public void onNetworkAvailable() {
        super.onNetworkAvailable();
        mLogTextView.append(new Date() + ": network available\n");
    }

    @Override
    public void onNetworkUnavailable() {
        super.onNetworkUnavailable();
        mLogTextView.append(new Date() + ": network unavailable\n");
    }
}
