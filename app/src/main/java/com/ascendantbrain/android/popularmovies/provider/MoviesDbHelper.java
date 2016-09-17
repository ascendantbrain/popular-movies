package com.ascendantbrain.android.popularmovies.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ascendantbrain.android.popularmovies.provider.MoviesContract.Movie;
import com.ascendantbrain.android.popularmovies.provider.MoviesContract.Metadata;
import com.ascendantbrain.android.popularmovies.provider.MoviesContract.Video;
import com.ascendantbrain.android.popularmovies.provider.MoviesContract.Review;

/**
 * Manages a local database for movie data.
 */
public class MoviesDbHelper extends SQLiteOpenHelper {

    // increment the version whenever changes are made to the database scheme 
    private static final int DATABASE_VERSION = 21;

    static final String DATABASE_NAME = "popular_movies.db";

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // create table for all movies
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + Movie.TABLE_NAME + " (" +
                Movie._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Movie.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                Movie.COLUMN_TITLE + " TEXT NOT NULL, " +
                Movie.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                Movie.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                Movie.COLUMN_BACKDROP_PATH + " TEXT NOT NULL, " +
                Movie.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                Movie.COLUMN_RELEASE_DATE + " INTEGER NOT NULL, " +
                " UNIQUE (" + Movie.COLUMN_MOVIE_ID +
                ") ON CONFLICT REPLACE);";

        // create table for movie metadata
        final String SQL_CREATE_MOVIE_METADATA_TABLE = "CREATE TABLE " + Metadata.TABLE_NAME + " (" +
                Metadata._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Metadata.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                Metadata.COLUMN_TAG + " INTEGER DEFAULT 0, " +
                Metadata.COLUMN_FAVORITE + " INTEGER DEFAULT 0, " +
                " FOREIGN KEY("+Metadata.COLUMN_MOVIE_ID+") REFERENCES " +
                Movie.TABLE_NAME + "(" + Movie.COLUMN_MOVIE_ID + "));";

        // create table for movie reviews
        final String SQL_CREATE_MOVIE_REVIEW_TABLE = "CREATE TABLE " + Review.TABLE_NAME + " (" +
                Review._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Review.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                Review.COLUMN_REVIEW_ID + " TEXT NOT NULL, " +
                Review.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                Review.COLUMN_REVIEW + " TEXT NOT NULL, " +
                " FOREIGN KEY(" + Review.COLUMN_MOVIE_ID + ") REFERENCES " +
                Movie.TABLE_NAME + "(" + Movie.COLUMN_MOVIE_ID + "));";

        // create table for movie videos
        final String SQL_CREATE_MOVIE_VIDEO_TABLE = "CREATE TABLE " + Video.TABLE_NAME + " (" +
                Video._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Video.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                Video.COLUMN_VIDEO_ID + " TEXT NOT NULL, " +
                Video.COLUMN_KEY + " TEXT NOT NULL, " +
                Video.COLUMN_NAME + " TEXT NOT NULL, " +
                Video.COLUMN_SITE + " TEXT NOT NULL, " +
                Video.COLUMN_TYPE + " TEXT NOT NULL, " +
                " FOREIGN KEY(" + Video.COLUMN_MOVIE_ID + ") REFERENCES " +
                Movie.TABLE_NAME + "(" + Movie.COLUMN_MOVIE_ID + "));";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_METADATA_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_REVIEW_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_VIDEO_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // future improvement: retain favorite data during database upgrade
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Movie.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Metadata.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Review.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Video.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    /**
     * Called when the database connection is being configured, to enable features
     * such as write-ahead logging or foreign key support.
     * <p>
     * This method is called before {@link #onCreate}, {@link #onUpgrade},
     * {@link #onDowngrade}, or {@link #onOpen} are called.  It should not modify
     * the database except to configure the database connection as required.
     * </p><p>
     * This method should only call methods that configure the parameters of the
     * database connection, such as {@link SQLiteDatabase#enableWriteAheadLogging}
     * {@link SQLiteDatabase#setForeignKeyConstraintsEnabled},
     * {@link SQLiteDatabase#setLocale}, {@link SQLiteDatabase#setMaximumSize},
     * or executing PRAGMA statements.
     * </p>
     *
     * @param db The database.
     */
//    @Override
//    public void onConfigure(SQLiteDatabase db) {
//        super.onConfigure(db);
//        // support foreign keys including cascading foreign key deletion
//        db.setForeignKeyConstraintsEnabled(true);
//    }
}
