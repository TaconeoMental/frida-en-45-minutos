package com.nivel4.httpClient;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestPost {
    OkHttpClient client;

    public RequestPost() {
        client = new OkHttpClient();
    }

    public void requestPost(JSONObject jsonBody, String targetURL, CustomResponseCallback callback) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(jsonBody.toString(), JSON);

        Request request = new Request.Builder()
                .url(targetURL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String result = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        callback.onResponse(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onFailure(call, new IOException("Error parsing JSON"));
                    }
                } else {
                    callback.onFailure(call, new IOException("Invalid response: " + response.code()));
                }
            }
        });
    }

    public interface CustomResponseCallback {
        void onFailure(Call call, IOException e);
        void onResponse(JSONObject response);
    }
}
