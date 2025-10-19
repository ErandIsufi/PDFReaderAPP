package com.example.bookapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private final List<Book> books;
    private final Context context;
    private final OnBookClickListener listener;

    public interface OnBookClickListener {
        void onClick(Book book);
        void onLongClick(Book book);
    }

    public BookAdapter(Context context, List<Book> books, OnBookClickListener listener) {
        this.context = context;
        this.books = books;
        this.listener = listener;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImage;
        TextView title;
        ImageButton deleteButton;

        public BookViewHolder(View view) {
            super(view);
            coverImage = view.findViewById(R.id.bookCoverImageView);
            title = view.findViewById(R.id.bookTitleTextView);
            deleteButton = view.findViewById(R.id.btnDeleteBook); // new delete button
        }
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.title.setText(book.getTitle());

        // Faqja e par e pdf esht kopertina
        try {
            Uri pdfUri = Uri.parse(book.getUri());
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(pdfUri, "r");
            if (fileDescriptor != null) {
                PdfRenderer renderer = new PdfRenderer(fileDescriptor);
                if (renderer.getPageCount() > 0) {
                    PdfRenderer.Page page = renderer.openPage(0);
                    Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    holder.coverImage.setImageBitmap(bitmap);
                    page.close();
                }
                renderer.close();
                fileDescriptor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            holder.coverImage.setImageResource(R.drawable.ic_book_placeholder); // fotoja e librit
        }

        // Kur te trus e qel qat pdf
        holder.itemView.setOnClickListener(v -> listener.onClick(book));

        // Me fshi pdf
        holder.deleteButton.setOnClickListener(v -> listener.onLongClick(book));
    }

    @Override
    public int getItemCount() {
        return books.size();
    }
}
