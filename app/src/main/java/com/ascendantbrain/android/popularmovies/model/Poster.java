package com.ascendantbrain.android.popularmovies.model;

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.ascendantbrain.android.popularmovies.provider.MoviesContract;

public class Poster {
    private final long _id;
    public final String title;
    public final String pathUri;
    public final String movieId;

    Poster(long id, String title, String path, String movieId){
        this._id = id;
        this.title = title;
        this.pathUri = path;
        this.movieId = movieId;
    }

    private static final String IMAGE_BASE_PATH =
            "http://image.tmdb.org/t/p/w342";  // sizes: w185,w342,w500

    public Uri getPath(){
        return Uri.parse(IMAGE_BASE_PATH + pathUri);
    }

    public static Poster fromCursor(Cursor cursor){
        final long _id = cursor.getLong(_ID);
        final String title = cursor.getString(COL_TITLE);
        final String posterPath = cursor.getString(COL_POSTER_PATH);
        final String movieId = cursor.getString(COL_MOVIE_ID);
        return new Poster(_id,title,posterPath,movieId);
    }

    @Override
    public String toString() {
        return title;
    }

    public long getId(){
        return _id;
    }

    public final static String[] POSTER_PROJECTION = {
            MoviesContract.Movie.TABLE_NAME + "." + BaseColumns._ID,
            MoviesContract.Movie.COLUMN_TITLE,
            MoviesContract.Movie.COLUMN_POSTER_PATH,
            MoviesContract.Movie.TABLE_NAME + "." + MoviesContract.Movie.COLUMN_MOVIE_ID
    };

    public final static int _ID = 0;
    public final static int COL_TITLE = 1;
    public final static int COL_POSTER_PATH = 2;
    public final static int COL_MOVIE_ID = 3;

}
