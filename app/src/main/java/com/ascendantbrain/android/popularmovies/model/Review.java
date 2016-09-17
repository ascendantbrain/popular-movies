package com.ascendantbrain.android.popularmovies.model;

import android.database.Cursor;
import android.provider.BaseColumns;

import com.ascendantbrain.android.popularmovies.provider.MoviesContract;

import org.json.JSONException;
import org.json.JSONObject;

public class Review {

    public static final long INVALID_ID = -1;
    public final long _id;          // primary key
    public final String movie_id;   // "id":297761
    public final String author;     // "author":"Cineport"
    public final String review;     // "content":"Fantastic action that makes up for some plot holes."
    public final String review_id;  // "id":"57a814dc9251415cfb00309a"

    /**
     * Constructs movie object from cursor.  Cursor fields must
     * match the projection defined in this class.
     */
    public static Review fromCursor(Cursor cursor){
        long _id = cursor.getLong(COL_ID);
        String movie_id = cursor.getString(COL_MOVIE_ID);
        String author = cursor.getString(COL_AUTHOR);
        String review = cursor.getString(COL_REVIEW);
        String review_id = cursor.getString(COL_REVIEW_ID);
        return new Review(_id,movie_id,author,review,review_id);
    }

    public static Review fromJsonObject(final String movie_id, final JSONObject reviewObject){
        // JSON keys for the review object (id,author,content; ignore: url)
        final String MDB_REVIEW_ID = "id";
        final String MDB_AUTHOR = "author";
        final String MDB_CONTENT = "content";

        Review review = null;
        try {
            final String review_id = reviewObject.getString(MDB_REVIEW_ID);
            final String author = reviewObject.getString(MDB_AUTHOR);
            final String content = reviewObject.getString(MDB_CONTENT);
            review = new Review(INVALID_ID,movie_id,author,content,review_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return review;
    }

    Review(long _id,String movie_id, String author, String review, String review_id){
        this._id = _id;
        this.movie_id = movie_id;
        this.author = author;
        this.review = review;
        this.review_id = review_id;
    }

    public final static String[] MODEL_PROJECTION = {
            BaseColumns._ID,
            MoviesContract.Review.COLUMN_MOVIE_ID,
            MoviesContract.Review.COLUMN_AUTHOR,
            MoviesContract.Review.COLUMN_REVIEW,
            MoviesContract.Review.COLUMN_REVIEW_ID,
    };

    public final static int COL_ID = 0;
    public final static int COL_MOVIE_ID = 1;
    public final static int COL_AUTHOR = 2;
    public final static int COL_REVIEW = 3;
    public final static int COL_REVIEW_ID = 4;
}
