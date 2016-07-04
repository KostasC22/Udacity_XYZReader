package com.example.xyzreader;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kostas on 03/07/2016.
 */
public class Article implements Parcelable{

    private String photoURL;
    private String title;
    private String author;
    private String body;

    public Article(){

    }

    public Article(String photoURL, String title, String author, String body) {
        this.photoURL = photoURL;
        this.title = title;
        this.author = author;
        this.body = body;
    }

    public Article(Parcel in){
        this.photoURL = in.readString();
        this.title = in.readString();
        this.author = in.readString();
        this.body = in.readString();
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Article{" +
                "photoURL='" + photoURL + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", body='" + body + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getPhotoURL());
        dest.writeString(getTitle());
        dest.writeString(getAuthor());
        dest.writeString(getBody());
    }

    public static final Parcelable.Creator<Article> CREATOR = new Parcelable.Creator<Article>() {
        public Article createFromParcel(Parcel in) {
            return new Article(in);
        }

        public Article[] newArray(int size) {
            return new Article[size];
        }
    };
}
