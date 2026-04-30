package com.example.androidmessenger;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class EditPostActivity extends AppCompatActivity {

    private String postId;
    private String uid;
    private EditText textEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        postId = getIntent().getStringExtra("post_id");
        textEt = findViewById(R.id.et_text);

        FirebaseDatabase.getInstance().getReference("posts").child(uid).child(postId).get()
                .addOnSuccessListener(snapshot -> {
                    Post post = snapshot.getValue(Post.class);
                    if (post != null) {
                        textEt.setText(post.text);
                    }
                });

        findViewById(R.id.btn_update).setOnClickListener(v -> update());
    }

    private void update() {
        String text = textEt.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Текст пустой", Toast.LENGTH_SHORT).show();
            return;
        }
        String title = text.length() > 60 ? text.substring(0, 60) + "..." : text;
        FirebaseDatabase.getInstance().getReference("posts").child(uid).child(postId).child("text").setValue(text);
        FirebaseDatabase.getInstance().getReference("posts").child(uid).child(postId).child("title").setValue(title);
        FirebaseDatabase.getInstance().getReference("posts").child(uid).child(postId).child("updatedAt").setValue(System.currentTimeMillis())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
