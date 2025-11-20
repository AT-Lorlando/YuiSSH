package com.example.mobiletemplate;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        registerPlugin(SSHJPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
