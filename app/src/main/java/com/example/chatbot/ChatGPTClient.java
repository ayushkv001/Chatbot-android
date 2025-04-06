package com.example.chatbot;


import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;


public class ChatGPTClient {
    // Using the free model available on Gemini API
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String API_KEY = API.GEMINI_API_KEY;// Get API key from class API in form of string

    public interface ResponseCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public void sendMessage(String userMessage, ResponseCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            // Create the JSON request body for Gemini API
            JSONObject jsonBody = new JSONObject();

            // Create the parts array with the user message
            JSONArray partsArray = new JSONArray();
            JSONObject partObject = new JSONObject();
            partObject.put("text", userMessage);
            partsArray.put(partObject);

            // Create the content object
            JSONObject contentObject = new JSONObject();
            contentObject.put("parts", partsArray);
            contentObject.put("role", "user");

            // Add the content object to contents array
            JSONArray contentsArray = new JSONArray();
            contentsArray.put(contentObject);
            jsonBody.put("contents", contentsArray);

            // Build the URL with the API key as a query parameter
            HttpUrl url = HttpUrl.parse(API_URL).newBuilder()
                    .addQueryParameter("key", API_KEY)
                    .build();

            // Create the request body
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    jsonBody.toString()
            );

            // Build the request
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            // Execute the request asynchronously
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
                            // Parse the Gemini API response to extract the text
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            String botResponse = jsonResponse.getJSONArray("candidates")
                                    .getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");
                            callback.onResponse(botResponse);
                        } catch (JSONException e) {
                            callback.onError("JSON parsing error: " + e.getMessage());
                        }
                    } else {
                        // Handle API error responses
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
