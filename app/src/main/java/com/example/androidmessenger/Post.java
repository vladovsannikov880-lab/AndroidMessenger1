package com.example.androidmessenger;

public class Post {
    public String id;
    public String userId;
    public String prompt;
    public String text;
    public String title;
    public long createdAt;
    public long updatedAt;

    public Post() {
    }

    public Post(String id, String userId, String prompt, String text, String title, long createdAt, long updatedAt) {
        this.id = id;
        this.userId = userId;
        this.prompt = prompt;
        this.text = text;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
