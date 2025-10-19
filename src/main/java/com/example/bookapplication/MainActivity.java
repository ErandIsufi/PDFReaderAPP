package com.example.bookapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_PDF_FILE = 42;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyBooks";
    private static final String KEY_BOOK_LIST = "BookList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before setting content view
        SharedPreferences themePrefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        boolean isDark = themePrefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Theme toggle using ImageButton
        ImageButton btnToggleTheme = findViewById(R.id.btnToggleTheme);
        updateThemeIcon(btnToggleTheme, isDark);

        btnToggleTheme.setOnClickListener(v -> {
            boolean currentlyDark = themePrefs.getBoolean("dark_mode", false);
            themePrefs.edit().putBoolean("dark_mode", !currentlyDark).apply();
            AppCompatDelegate.setDefaultNightMode(
                    !currentlyDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            recreate(); // e restarton aktivitietin me ndryshu ngjyren
        });

        Button btnPickPdf = findViewById(R.id.btnPickPdf);
        btnPickPdf.setOnClickListener(v -> pickPdfFile());

        Button btnViewBooks = findViewById(R.id.btnViewBooks);
        btnViewBooks.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SavedBooksActivity.class));
        });

        Button btnContinueReading = findViewById(R.id.btnContinueReading);
        btnContinueReading.setOnClickListener(v -> continueReading());
    }

    private void updateThemeIcon(ImageButton button, boolean isDark) {
        button.setImageResource(isDark ? R.drawable.ic_sun : R.drawable.ic_moon);
    }

    private void pickPdfFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, PICK_PDF_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_FILE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                int flags = data.getFlags();
                int takeFlags = flags & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                try {
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);
                } catch (SecurityException e) {
                    Log.e("MainActivity", "URI permission failed", e);
                    Toast.makeText(this, "Nuk u mor leja për PDF-në", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Per me vendos titullin
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Vendosni titullin e librit");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Ruaj", (dialog, which) -> {
                String title = input.getText().toString().trim();
                if (!title.isEmpty()) {
                    saveBook(title, uri.toString());
                    Toast.makeText(this, "Libri u ruajt me sukses!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Titulli nuk mund të jetë bosh", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Anulo", (dialog, which) -> dialog.cancel());
            builder.show();
        }
    }

    private void saveBook(String title, String uri) {
        try {
            JSONArray booksArray = new JSONArray(sharedPreferences.getString(KEY_BOOK_LIST, "[]"));
            JSONObject bookObj = new JSONObject();
            bookObj.put("title", title);
            bookObj.put("uri", uri);
            booksArray.put(bookObj);

            sharedPreferences.edit().putString(KEY_BOOK_LIST, booksArray.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gabim gjatë ruajtjes së librit", Toast.LENGTH_SHORT).show();
        }
    }

    private void continueReading() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String jsonString = prefs.getString(KEY_BOOK_LIST, "[]");

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            if (jsonArray.length() == 0) {
                Toast.makeText(this, "Nuk ka libra të ruajtur", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences readPrefs = getSharedPreferences("LastRead", MODE_PRIVATE);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject bookObj = jsonArray.getJSONObject(i);
                String uri = bookObj.getString("uri");

                if (readPrefs.contains(uri)) {
                    Intent intent = new Intent(MainActivity.this, PdfViewerActivity.class);
                    intent.putExtra("pdfUri", uri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                    return;
                }
            }

            Toast.makeText(this, "Nuk ka lexim të fundit", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gabim gjatë vazhdimit të leximit", Toast.LENGTH_SHORT).show();
        }
    }
}
