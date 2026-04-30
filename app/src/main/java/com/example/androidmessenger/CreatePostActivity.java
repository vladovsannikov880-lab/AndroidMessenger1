package com.example.androidmessenger;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class CreatePostActivity extends AppCompatActivity {

    private EditText promptEt;
    private TextView generatedTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        promptEt = findViewById(R.id.et_prompt);
        generatedTv = findViewById(R.id.tv_generated);

        findViewById(R.id.btn_generate).setOnClickListener(v -> generate());
        findViewById(R.id.btn_save).setOnClickListener(v -> savePost());
    }

    private void generate() {
        String prompt = promptEt.getText().toString().trim();
        if (prompt.isEmpty()) {
            Toast.makeText(this, "Введите тему поста", Toast.LENGTH_SHORT).show();
            return;
        }
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        AiClient.generateText(prompt, new AiClient.Callback() {
            @Override
            public void onSuccess(String text) {
                runOnUiThread(() -> {
                    findViewById(R.id.progress).setVisibility(View.GONE);
                    generatedTv.setText(text);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    findViewById(R.id.progress).setVisibility(View.GONE);
                    generatedTv.setText("Демо-пост: " + prompt + "\n\n" +
                            "Это fallback-текст. Добавьте рабочий ключ API, чтобы получать ответы модели.");
                    Toast.makeText(CreatePostActivity.this, message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void savePost() {
        String text = generatedTv.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Сначала сгенерируйте текст", Toast.LENGTH_SHORT).show();
            return;
        }
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Сессия истекла, войдите заново", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String id = FirebaseDatabase.getInstance().getReference("posts").child(uid).push().getKey();
        if (id == null) {
            Toast.makeText(this, "Не удалось создать ID поста", Toast.LENGTH_SHORT).show();
            return;
        }
        long now = System.currentTimeMillis();
        String title = text.length() > 60 ? text.substring(0, 60) + "..." : text;
        Post post = new Post(id, uid, promptEt.getText().toString().trim(), text, title, now, now);

        FirebaseDatabase.getInstance().getReference("posts").child(uid).child(id).setValue(post)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Пост сохранен", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
