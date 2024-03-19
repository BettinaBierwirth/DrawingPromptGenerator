package com.bettinabierwirth.drawingpromptgenerator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize category buttons
        Button floraButton = findViewById(R.id.category_flora);
        Button faunaButton = findViewById(R.id.category_fauna);
        Button landscapeButton = findViewById(R.id.category_landscape);
        Button cityscapeButton = findViewById(R.id.category_cityscape);
        Button situationButton = findViewById(R.id.category_situation);
        Button wordButton = findViewById(R.id.category_word);

        // Set click listeners for category buttons
        floraButton.setOnClickListener(v -> startPromptActivity("flora"));

        faunaButton.setOnClickListener(v -> startPromptActivity("fauna"));

        landscapeButton.setOnClickListener(v -> startPromptActivity("landscape"));

        cityscapeButton.setOnClickListener(v -> startPromptActivity("cityscape"));

        situationButton.setOnClickListener(v -> startPromptActivity("situation"));

        wordButton.setOnClickListener(v -> startPromptActivity("word"));

    }


    private void startPromptActivity(String category) {
        Intent promptIntent = new Intent(MainActivity.this, PromptActivity.class);
        promptIntent.putExtra("SELECTED_CATEGORY", category);
        startActivity(promptIntent);
    }
}
