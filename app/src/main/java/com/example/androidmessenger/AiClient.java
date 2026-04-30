package com.example.androidmessenger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AiClient {

    public interface Callback {
        void onSuccess(String text);
        void onError(String message);
    }

    public static void generateText(String prompt, Callback callback) {
        new Thread(() -> {
            try {
                String provider = BuildConfig.AI_PROVIDER;
                if (provider == null || provider.isEmpty()) {
                    provider = "openai";
                }

                String apiKey = BuildConfig.AI_API_KEY;
                if (apiKey == null || apiKey.isEmpty()) {
                    callback.onError("Заполните AI_API_KEY в local.properties (вернется demo-текст).");
                    return;
                }

                String endpoint = resolveEndpoint(provider);
                String model = resolveModel(provider);
                String result = callOpenAiCompatible(endpoint, apiKey, model, prompt);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    private static String resolveEndpoint(String provider) {
        String custom = BuildConfig.AI_BASE_URL;
        if (custom != null && !custom.isEmpty()) {
            return custom;
        }
        switch (provider.toLowerCase()) {
            case "mistral":
                return "https://api.mistral.ai/v1/chat/completions";
            case "openrouter":
                return "https://openrouter.ai/api/v1/chat/completions";
            case "openai":
            default:
                return "https://api.openai.com/v1/chat/completions";
        }
    }

    private static String resolveModel(String provider) {
        String custom = BuildConfig.AI_MODEL;
        if (custom != null && !custom.isEmpty()) {
            return custom;
        }
        switch (provider.toLowerCase()) {
            case "mistral":
                return "mistral-small-latest";
            case "openrouter":
                return "openai/gpt-4o-mini";
            case "openai":
            default:
                return "gpt-4o-mini";
        }
    }

    private static String callOpenAiCompatible(String endpoint, String apiKey, String model, String prompt) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);

        JSONObject body = new JSONObject();
        body.put("model", model);
        JSONArray messages = new JSONArray();
        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("content", "Сгенерируй короткий пост для соцсетей: " + prompt);
        messages.put(user);
        body.put("messages", messages);
        body.put("temperature", 0.8);

        OutputStream os = conn.getOutputStream();
        os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        os.close();

        InputStream stream = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        if (conn.getResponseCode() >= 400) {
            throw new RuntimeException("Ошибка API: " + response);
        }

        JSONObject json = new JSONObject(response.toString());
        JSONArray choices = json.optJSONArray("choices");
        if (choices == null || choices.length() == 0) {
            throw new RuntimeException("Пустой ответ модели");
        }
        return choices.getJSONObject(0).getJSONObject("message").optString("content", "");
    }
}
