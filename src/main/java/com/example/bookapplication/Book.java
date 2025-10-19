
package com.example.bookapplication;

public class Book {
    private String title;
    private String uri;

    public Book(String title, String uri) {
        this.title = title;
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return title;  // Titulli qe del ne list se ma ka thi qe dy or sdalke
    }
}


