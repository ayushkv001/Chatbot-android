package com.example.chatbot;


import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class ChatGPTClient {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = API.API_KEY_3;

    public interface ResponseCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public void sendMessage(String userMessage, ResponseCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            // Create the JSON request body correctly
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "gpt-3.5-turbo");

            // Create messages array properly
            JSONArray messagesArray = new JSONArray();
            JSONObject messageObject = new JSONObject();
            messageObject.put("role", "user");
            messageObject.put("content", userMessage);
            messagesArray.put(messageObject);

            jsonBody.put("messages", messagesArray);

            // Create the request
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    jsonBody.toString()
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(body)
                    .build();

            // Execute the request
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();

                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            String botResponse = jsonResponse.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");
                            callback.onResponse(botResponse);
                        } catch (JSONException e) {
                            callback.onError("JSON parsing error: " + e.getMessage());
                        }
                    } else {
                        // Return the error message from the API
                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            String errorMessage = errorJson.getJSONObject("error").getString("message");
                            callback.onError("API error: " + errorMessage);
                        } catch (JSONException e) {
                            callback.onError("Error: " + response.code() + " " + response.message());
                        }
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Request creation error: " + e.getMessage());
        }
    }
}
