package com.example.chatbot;



import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.chatRecyclerView);
        EditText messageInput = findViewById(R.id.messageInput);
        Button sendButton = findViewById(R.id.sendButton);

        adapter = new MessageAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ChatGPTClient chatGPTClient = new ChatGPTClient();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMessage = messageInput.getText().toString().trim();
                if (!userMessage.isEmpty()) {
                    // Add user message to the list and update RecyclerView
                    messageList.add(new Message(userMessage, true));
                    adapter.notifyItemInserted(messageList.size() - 1);

                    // Clear input field
                    messageInput.setText("");

                    // Send user message to ChatGPT and get the response
                    chatGPTClient.sendMessage(userMessage, new ChatGPTClient.ResponseCallback() {
                        @Override
                        public void onResponse(String response) {
                            runOnUiThread(() -> {
                                // Add bot response to the list and update RecyclerView
                                messageList.add(new Message(response, false));
                                adapter.notifyItemInserted(messageList.size() - 1);
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                // Handle error (optional)
                                messageList.add(new Message("Error: " + error, false));
                                adapter.notifyItemInserted(messageList.size() - 1);
                            });
                        }
                    });
                }
            }
        });

        // Add a welcome message from the bot (optional)
        messageList.add(new Message("Hello! I'm your chatbot. How can I assist you?", false));
        adapter.notifyItemInserted(messageList.size() - 1);
    }
}
