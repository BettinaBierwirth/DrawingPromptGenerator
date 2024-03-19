package com.bettinabierwirth.drawingpromptgenerator;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PromptActivity extends AppCompatActivity {

    private TextView promptTextView;
    private List<String> prompts;
    private List<String> displayedPrompts;

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

        } catch (IOException | JSONException e) {
            e.printStackTrace();
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
        Bitmap promptBitmap = Bitmap.createBitmap(promptTextView.getWidth(), promptTextView.getHeight(), Bitmap.Config.ARGB_8888);
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

        try {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String filename = getNextFilename(directory);
            File file = new File(directory, filename);

            FileOutputStream fos = new FileOutputStream(file);

            // Save the bitmap as JPEG
            promptBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            // Show a Toast message with the saved image path
            String message = "Image saved successfully. Path: " + file.getAbsolutePath();
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            // Notify the user if there's an error saving the image
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    private String getNextFilename(File directory) {
        int imageCount = countSavedImages(directory);
        return "prompt_image_" + (imageCount + 1) + ".jpg";
    }

    private int countSavedImages(File directory) {
        int count = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().startsWith("prompt_image_") && file.getName().endsWith(".jpg")) {
                    count++;
                }
            }
        }
        return count;
    }

}


