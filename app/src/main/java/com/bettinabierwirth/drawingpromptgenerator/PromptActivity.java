package com.bettinabierwirth.drawingpromptgenerator;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class PromptActivity extends AppCompatActivity {


    private TextView promptTextView;
    private List<String> prompts;
    private List<String> displayedPrompts;
    private Bitmap promptBitmap;
    private ActivityResultLauncher<Intent> saveImageLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompt);

        // Initialize views
        TextView selectedCategoryTextView = findViewById(R.id.selected_category);
        promptTextView = findViewById(R.id.prompt);

        Button regeneratePromptButton = findViewById(R.id.regenerate_prompt);
        Button returnToCategoriesButton = findViewById(R.id.return_to_categories);

        // Get the selected category from the intent
        String selectedCategory = getIntent().getStringExtra("SELECTED_CATEGORY");

        // Set the selected category text
        selectedCategoryTextView.setText(selectedCategory);

        // Load prompts from JSON file
        loadPromptsFromJson(selectedCategory);

        // Initialize the list to keep track of displayed prompts
        displayedPrompts = new ArrayList<>();

        // Update prompt text
        updatePromptText();

        // Set click listener for the "Return to Categories" button
        returnToCategoriesButton.setOnClickListener(v -> returnToCategories());

        // Set click listener for the "Regenerate Prompt" button
        regeneratePromptButton.setOnClickListener(v -> updatePromptText());

        Button downloadButton = findViewById(R.id.download_button);
        downloadButton.setOnClickListener(v -> savePromptImage());

        // Initialize the saveImageLauncher
        saveImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                // Handle the result here
                Intent data = result.getData();
                saveImage(data);
            } else {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadPromptsFromJson(String category) {
        prompts = new ArrayList<>();

        String fileName = "prompts.json";
        AssetManager assetManager = getAssets();

        try {
            InputStream inputStream = assetManager.open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            // Parse the JSON file
            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
            JSONArray categoryArray = jsonObject.getJSONArray(category);

            // Add prompts to the list
            for (int i = 0; i < categoryArray.length(); i++) {
                prompts.add(categoryArray.getString(i));
            }

            // Close the streams
            bufferedReader.close();
            inputStream.close();

        } catch (IOException e) {
            Log.e("PromptActivity", "IOException while loading prompts from JSON: " + e.getMessage());
            Toast.makeText(this, "Failed to load prompts. Please try again.", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Log.e("PromptActivity", "JSONException while parsing JSON: " + e.getMessage());
            Toast.makeText(this, "Failed to parse JSON. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePromptText() {
        if (prompts != null && !prompts.isEmpty()) {
            // Check if all prompts have been displayed
            if (displayedPrompts.size() == prompts.size()) {
                // If all prompts have been displayed, reset the list
                displayedPrompts.clear();
            }

            // Generate a random index to get an unused prompt from the list
            int randomIndex;
            do {
                randomIndex = new Random().nextInt(prompts.size());
            } while (displayedPrompts.contains(prompts.get(randomIndex)));

            // Get the prompt and mark it as displayed
            String prompt = prompts.get(randomIndex);
            displayedPrompts.add(prompt);

            // Update the promptTextView with the generated prompt
            promptTextView.setText(prompt);
        } else {
            // Handle the case when prompts are not loaded or the list is empty
            promptTextView.setText(R.string.no_prompts_available_for_this_category);
        }
    }

    private void returnToCategories() {
        finish();
    }

    private void savePromptImage() {
        promptTextView.setDrawingCacheEnabled(true);
        promptBitmap = Bitmap.createBitmap(promptTextView.getWidth(), promptTextView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(promptBitmap);
        canvas.drawColor(Color.WHITE);

        // Get the current text color
        int currentTextColor = promptTextView.getCurrentTextColor();

        // Set the text color to black
        promptTextView.setTextColor(Color.BLACK);

        // Draw the text on the bitmap
        promptTextView.draw(canvas);

        // Reset the text color to the original color
        promptTextView.setTextColor(currentTextColor);

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_TITLE, "prompt_image.jpg");

        // Launch the saveImageLauncher
        saveImageLauncher.launch(intent);
    }


    private void saveImage(Intent data) {
        // Handle saving the image here
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(Objects.requireNonNull(data.getData()));
            if (outputStream != null) {
                // Save the bitmap to the output stream
                this.promptBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();

                // Show a Toast message with the saved image path
                String message = "Image saved successfully. Path: " + data.getData().toString();
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to open output stream", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("PromptActivity", "IOException while saving image: " + e.getMessage());
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }


}


