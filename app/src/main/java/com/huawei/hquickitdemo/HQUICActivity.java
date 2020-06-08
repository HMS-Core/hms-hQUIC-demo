/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huawei.hquickitdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.chromium.net.CronetException;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public class HQUICActivity extends AppCompatActivity {
    private static final String TAG = "HQUICActivity";

    private static final String URL = "https://replaceby_your_quic_website_name";

    private static final String METHOD = "GET";

    private TextView callText;

    private String callStr;

    private HQUICService hquicService;

    private long startTime;

    private static final int CAPACITY = 102400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        callText = findViewById(R.id.call_text);
        createHQUIC();
    }

    private void createHQUIC() {
        hquicService = new HQUICService(this);
        hquicService.setCallback(
                new UrlRequest.Callback() {
                    @Override
                    public void onRedirectReceived(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo, String s)
                            throws Exception {
                        Log.i(TAG, "onRedirectReceived: method is called");
                        urlRequest.followRedirect();
                    }

                    @Override
                    public void onResponseStarted(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo)
                            throws Exception {
                        Log.i(TAG, "onResponseStarted: method is called");
                        urlRequest.read(ByteBuffer.allocateDirect(CAPACITY));
                    }

                    @Override
                    public void onReadCompleted(
                            UrlRequest urlRequest, UrlResponseInfo urlResponseInfo, ByteBuffer byteBuffer)
                            throws Exception {
                        Log.i(TAG, "onReadCompleted: method is called");
                        urlRequest.read(ByteBuffer.allocateDirect(CAPACITY));
                    }

                    @Override
                    public void onSucceeded(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo) {
                        Log.i(TAG, "onSucceeded: method is called");
                        Log.i(TAG, "onSucceeded: protocol is " + urlResponseInfo.getNegotiatedProtocol());
                        long endTime = System.currentTimeMillis();
                        long duration = endTime - startTime;
                        callStr += ("duration -> " + duration + "ms" + System.lineSeparator());
                        callStr += ("protocol -> " + urlResponseInfo.getNegotiatedProtocol() + System.lineSeparator());
                        List<Map.Entry<String, String>> list = urlResponseInfo.getAllHeadersAsList();
                        for (Map.Entry<String, String> stringStringEntry : list) {
                            callStr += stringStringEntry.getKey() + " -> ";
                            callStr += stringStringEntry.getValue() + System.lineSeparator();
                        }
                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        callText.setText(callStr);
                                    }
                                });
                    }

                    @Override
                    public void onFailed(
                            UrlRequest urlRequest, UrlResponseInfo urlResponseInfo, CronetException error) {
                        Log.e(TAG, "onFailed: method is called ", error);
                        callStr += "onFailed: method is called " + error;
                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        callText.setText(callStr);
                                    }
                                });
                    }
                });
    }

    public void hQUICTest(View view) {
        callStr = "";
        startTime = System.currentTimeMillis();
        hquicService.sendRequest(URL, METHOD);
    }
}
