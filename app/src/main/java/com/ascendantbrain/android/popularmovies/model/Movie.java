package com.ascendantbrain.android.popularmovies.model;

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.ascendantbrain.android.popularmovies.provider.MoviesContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Movie {
    public int _ID;
    public String title;
    public String overview;
    public String poster_path;
    public String backdrop_path;
    public String release_date;
    public String release_year;
    public double vote_average;
    public String movie_id;
    public boolean isFavorite;

    // empty constructor
    public Movie(){};

    /**
     * Constructs movie object from cursor.  Cursor fields must
     * match the projection defined in this class.
     */
    public static Movie fromCursor(Cursor cursor){
        Movie movie = new Movie();
        movie.title = cursor.getString(COL_TITLE);
        movie.overview = cursor.getString(COL_OVERVIEW);
        movie.poster_path = cursor.getString(COL_POSTER_PATH);
        movie.backdrop_path = cursor.getString(COL_BACKDROP_PATH);
        movie.vote_average = cursor.getDouble(COL_VOTE_AVERAGE);
        movie.movie_id = cursor.getString(COL_MOVIE_ID);
        movie._ID = cursor.getInt(COL_ID);
        movie.isFavorite = (cursor.getInt(COL_FAVORITE)==MoviesContract.Metadata.FLAG_FAVORITE);

        // parse release date
        Date date = null;
        try{
            String movie_release = cursor.getString(COL_RELEASE_DATE);
            date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(movie_release);
        }catch (ParseException e) {
            Log.getStackTraceString(e);
        }finally{
            if(date==null) date = new Date(0);
            movie.release_year = String.format(Locale.US,"%1$tY",date);
            movie.release_date = String.format(Locale.US,"%1$tB %1$te",date);
        }

        return movie;
    }

    public final static String[] PROJECTION = {
            MoviesContract.Movie.TABLE_NAME + "." + BaseColumns._ID,
            MoviesContract.Movie.COLUMN_TITLE,
            MoviesContract.Movie.COLUMN_OVERVIEW,
            MoviesContract.Movie.TABLE_NAME + "." + MoviesContract.Movie.COLUMN_MOVIE_ID,
            MoviesContract.Movie.COLUMN_RELEASE_DATE,
            MoviesContract.Movie.COLUMN_POSTER_PATH,
            MoviesContract.Movie.COLUMN_VOTE_AVERAGE,
            MoviesContract.Movie.COLUMN_BACKDROP_PATH,
            MoviesContract.Metadata.COLUMN_FAVORITE
    };

    public final static int COL_ID = 0;
    public final static int COL_TITLE = 1;
    public final static int COL_OVERVIEW = 2;
    public final static int COL_MOVIE_ID = 3;
    public final static int COL_RELEASE_DATE = 4;
    public final static int COL_POSTER_PATH = 5;
    public final static int COL_VOTE_AVERAGE = 6;
    public final static int COL_BACKDROP_PATH = 7;
    public final static int COL_FAVORITE = 8;

    private static final String IMAGE_BASE_POSTER_PATH =
            "http://image.tmdb.org/t/p/w342";     // sizes: w185,w342,w500

    private static final String IMAGE_BASE_BACKDROP_PATH =
            "http://image.tmdb.org/t/p/w500";     // sizes: w185,w342,w500


    public Uri getPosterPath(){
        return Uri.parse(IMAGE_BASE_POSTER_PATH + poster_path);
    }

    public Uri getBackdropPath(){
        return Uri.parse(IMAGE_BASE_BACKDROP_PATH + backdrop_path);
    }

}
