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
                String apiKey = BuildConfig.GIGACHAT_API_KEY;
                if (apiKey == null || apiKey.isEmpty()) {
                    callback.onError("Заполните GIGACHAT_API_KEY в local.properties (для demo вернется mock).");
                    return;
                }

                URL url = new URL("https://gigachat.devices.sberbank.ru/api/v1/chat/completions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("model", "GigaChat");
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
                    callback.onError("Ошибка API: " + response);
                    return;
                }

                JSONObject json = new JSONObject(response.toString());
                JSONArray choices = json.optJSONArray("choices");
                if (choices == null || choices.length() == 0) {
                    callback.onError("Пустой ответ модели");
                    return;
                }
                String text = choices.getJSONObject(0).getJSONObject("message").optString("content", "");
                callback.onSuccess(text);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }
}
