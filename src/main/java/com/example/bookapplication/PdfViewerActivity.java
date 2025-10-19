package com.example.bookapplication;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.io.InputStream;

public class PdfViewerActivity extends AppCompatActivity implements OnLoadCompleteListener, OnPageChangeListener, OnPageErrorListener {

    PDFView pdfView;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        pdfView = findViewById(R.id.pdfView);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("\tLoading...");
        progressDialog.setCancelable(false);

        String uriString = getIntent().getStringExtra("pdfUri");
        if (uriString == null) {
            Toast.makeText(this, "No PDF URI received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Uri uri = Uri.parse(uriString);

        // O ti rrezon majmuni, qeta ke harru me qit
        displayPdfFromUri(uri);
    }

    private void displayPdfFromUri(Uri uri) {
        progressDialog.show();
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed to open PDF input stream", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            int lastPage = getSharedPreferences("LastRead", MODE_PRIVATE).getInt(uri.toString(), 0);
            pdfView.fromStream(inputStream)
                    .defaultPage(lastPage)//Qe me dal faqja e fundit qa e ke lexu
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .onLoad(this)
                    .onPageChange(this)
                    .onPageError(this)
                    .enableAnnotationRendering(true)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .spacing(10)
                    .autoSpacing(true)
                    .fitEachPage(true)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .nightMode(false)
                    .load();

        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to open PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }


    @Override
    public void loadComplete(int nbPages) {
        progressDialog.dismiss();
    }

    @Override //qekjo esht per me rujt faqen e fundit qe e ke lexu
    public void onPageChanged(int page, int pageCount) {
        String uri = getIntent().getStringExtra("pdfUri");
        if (uri != null) {
            getSharedPreferences("LastRead", MODE_PRIVATE)
                    .edit()
                    .putInt(uri, page)
                    .apply();
        }
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Toast.makeText(this, "Page error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
