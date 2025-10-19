package com.example.bookapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SavedBooksActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    BookAdapter adapter;
    List<Book> books = new ArrayList<>();

    private static final String PREFS_NAME = "MyBooks";
    private static final String KEY_BOOK_LIST = "BookList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_books);

        recyclerView = findViewById(R.id.savedBooksRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns

        loadBooks();

        adapter = new BookAdapter(this, books, new BookAdapter.OnBookClickListener() {
            @Override
            public void onClick(Book book) {
                Uri uri = Uri.parse(book.getUri());
                Intent intent = new Intent(SavedBooksActivity.this, PdfViewerActivity.class);
                intent.putExtra("pdfUri", uri.toString());
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            }

            @Override
            public void onLongClick(Book book) {
                new AlertDialog.Builder(SavedBooksActivity.this)
                        .setTitle("Fshi librin")
                        .setMessage("A doni të fshini '" + book.getTitle() + "'?")
                        .setPositiveButton("Po", (dialog, which) -> {
                            deleteBook(book);
                        })
                        .setNegativeButton("Jo", null)
                        .show();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadBooks() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String jsonString = prefs.getString(KEY_BOOK_LIST, "[]");

        books.clear();

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject bookObj = jsonArray.getJSONObject(i);
                String title = bookObj.getString("title");
                String uri = bookObj.getString("uri");
                books.add(new Book(title, uri));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gabim gjatë ngarkimit të librave", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteBook(Book book) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String jsonString = prefs.getString(KEY_BOOK_LIST, "[]");

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            JSONArray newArray = new JSONArray();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject bookObj = jsonArray.getJSONObject(i);
                if (!(bookObj.getString("title").equals(book.getTitle())
                        && bookObj.getString("uri").equals(book.getUri()))) {
                    newArray.put(bookObj);
                }
            }

            prefs.edit().putString(KEY_BOOK_LIST, newArray.toString()).apply();

            loadBooks();
            adapter.notifyDataSetChanged();

            Toast.makeText(this, "Libri u fshi", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gabim gjatë fshirjes së librit", Toast.LENGTH_SHORT).show();
        }
    }
}
