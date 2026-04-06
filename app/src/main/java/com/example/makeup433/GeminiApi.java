package com.example.makeup433;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

public class GeminiApi {
    private static final String API_KEY = "AIzaSyBFYW4gARPcuOh5BtlOa-Q-NP-okBXGaEM";

    private static final String MODEL = "gemini-2.0-flash";

    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL + ":generateContent?key=" + API_KEY;

    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient();

    public static String generateText(String prompt) {
        try {
            // Build request JSON
            JSONObject root = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();

            part.put("text", prompt);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            root.put("contents", contents);

            RequestBody body = RequestBody.create(root.toString(), JSON);

            Request request = new Request.Builder()
                    .url(ENDPOINT)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String bodyStr = response.body() != null
                        ? response.body().string()
                        : "";

                if (!response.isSuccessful()) {
                    try {
                        JSONObject errObj = new JSONObject(bodyStr);
                        if (errObj.has("error")) {
                            JSONObject err = errObj.getJSONObject("error");
                            String msg = err.optString("message", bodyStr);
                            return "Gemini error: " + msg;
                        }
                    } catch (Exception ignored) { }
                    return "Gemini error: HTTP " + response.code() + " " + bodyStr;
                }

                // Parse candidates[0].content.parts[0].text
                try {
                    JSONObject respJson = new JSONObject(bodyStr);

                    if (!respJson.has("candidates")) {
                        return "Gemini parse error: no candidates field";
                    }

                    JSONArray candidates = respJson.getJSONArray("candidates");
                    if (candidates.length() == 0) {
                        return "Gemini parse error: empty candidates";
                    }

                    JSONObject first = candidates.getJSONObject(0);
                    if (!first.has("content")) {
                        return "Gemini parse error: no content";
                    }

                    JSONObject respContent = first.getJSONObject("content");
                    if (!respContent.has("parts")) {
                        return "Gemini parse error: no parts";
                    }

                    JSONArray respParts = respContent.getJSONArray("parts");
                    if (respParts.length() == 0) {
                        return "Gemini parse error: empty parts";
                    }

                    JSONObject firstPart = respParts.getJSONObject(0);
                    String text = firstPart.optString("text", "").trim();
                    if (text.isEmpty()) {
                        return "Gemini parse error: empty text";
                    }

                    return text;

                } catch (Exception je) {
                    return "Gemini parse error: " + je.getMessage();
                }
            }

        } catch (Exception e) {
            return "Gemini error: " + e.getMessage();
        }
    }
}