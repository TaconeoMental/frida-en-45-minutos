package com.nivel4.httpClient;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.nivel4.fridaen45minutos.MainActivity;

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
                String errorMessage = e.getMessage(); // Get the exception message
                callback.onFailure(call, new IOException(errorMessage));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String result = response.body().string();
                if (response.code() == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        callback.onResponse(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onFailure(call, new IOException("Error parsing JSON"));
                    }
                } else if (response.code() == 401) {
                    String errorMessage = getErrorMessageFromResponse(result); // Extract the error message from the response
                    callback.onFailure(call, new IOException(errorMessage));
                } else if (response.code() == 403) {
                    String errorMessage = getErrorMessageFromResponse(result); // Extract the error message from the response
                    callback.onFailure(call, new IOException(errorMessage));
                } else if (response.code() == 500) {
                    String errorMessage = getErrorMessageFromResponse(result); // Extract the error message from the response
                    callback.onFailure(call, new IOException(errorMessage));
                } else {
                    callback.onFailure(call, new IOException("Invalid response: " + response.code()));
                }
            }
        });
    }

    public void requestPostAuth(String token, JSONObject jsonBody, String targetURL, CustomResponseCallback callback) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(jsonBody.toString(), JSON);

        Request request = new Request.Builder()
                .url(targetURL)
                .post(requestBody)
                .header("45minutetoken", token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                String errorMessage = e.getMessage(); // Get the exception message
                callback.onFailure(call, new IOException(errorMessage));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String result = response.body().string();
                if (response.code() == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        callback.onResponse(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onFailure(call, new IOException("Error parsing JSON"));
                    }
                } else if (response.code() == 401) {
                    String errorMessage = getErrorMessageFromResponse(result); // Extract the error message from the response
                    callback.onFailure(call, new IOException(errorMessage));
                } else if (response.code() == 403) {
                    String errorMessage = getErrorMessageFromResponse(result); // Extract the error message from the response
                    callback.onFailure(call, new IOException(errorMessage));
                } else if (response.code() == 500) {
                    String errorMessage = getErrorMessageFromResponse(result); // Extract the error message from the response
                    callback.onFailure(call, new IOException(errorMessage));
                } else {
                    callback.onFailure(call, new IOException("Invalid response: " + response.code()));
                }
            }
        });
    }

    private String getErrorMessageFromResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("msg")) {
                return jsonObject.getString("msg");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "Unknown error";
    }

    public interface CustomResponseCallback {
        void onFailure(Call call, IOException e);
        void onResponse(JSONObject response);
    }
}
