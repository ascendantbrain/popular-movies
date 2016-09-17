package com.ascendantbrain.android.popularmovies.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ascendantbrain.android.popularmovies.provider.MoviesContract.Movie;
import com.ascendantbrain.android.popularmovies.provider.MoviesContract.Metadata;

public class MoviesProvider extends ContentProvider {
    private static final String TAG = MoviesProvider.class.getSimpleName();

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    MoviesDbHelper mOpenHelper;

    Uri test = Movie.CONTENT_URI;

    // uri matcher codes
    static final int MOVIES_POPULAR = 100;
    static final int MOVIES_ID_POPULAR = 101;
    static final int MOVIES_TOP_RATED = 200;
    static final int MOVIES_ID_TOP_RATED = 201;
    static final int MOVIES_FAVORITE = 300;
    static final int MOVIES_ID_FAVORITE = 301;
    static final int MOVIES = 400;
    static final int MOVIES_ID = 401;
    static final int MOVIES_REVIEWS = 500;
    static final int MOVIES_ID_REVIEWS = 501;
    static final int MOVIES_VIDEOS = 600;
    static final int MOVIES_ID_VIDEOS = 601;
    static final int METADATA = 700;

    /** Create a new MoviesDbHelper  */
    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case MOVIES: {
                String sql  = "DELETE FROM " + Movie.TABLE_NAME + " WHERE "
                        + Movie.TABLE_NAME + "." + Movie.COLUMN_MOVIE_ID
                        + " NOT IN ( SELECT " + Metadata.COLUMN_MOVIE_ID
                        + " FROM " + Metadata.TABLE_NAME + " WHERE "
                        + Metadata.COLUMN_FAVORITE + "=1);";
                rowsDeleted = db.compileStatement(sql).executeUpdateDelete();
                break;
            }
            case METADATA: {
                String select = Metadata.COLUMN_FAVORITE + "<>?";
                String[] selectArgs = {String.valueOf(Metadata.FLAG_FAVORITE)};
                rowsDeleted = db.delete(
                        Metadata.TABLE_NAME,select,selectArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            notifyChange(Movie.CONTENT_URI);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(@NonNull Uri uri) {

        switch (sUriMatcher.match(uri)) {
            // Student: Uncomment and fill out these two cases
            case MOVIES_POPULAR:
                return MoviesContract.Movie.CONTENT_TYPE;
            case MOVIES_ID_POPULAR:
                return MoviesContract.Movie.CONTENT_ITEM_TYPE;
            case MOVIES_TOP_RATED:
                return MoviesContract.Movie.CONTENT_TYPE;
            case MOVIES_ID_TOP_RATED:
                return MoviesContract.Movie.CONTENT_ITEM_TYPE;
            case MOVIES_FAVORITE:
                return MoviesContract.Movie.CONTENT_TYPE;
            case MOVIES_ID_FAVORITE:
                return MoviesContract.Movie.CONTENT_ITEM_TYPE;
            case MOVIES:
                return MoviesContract.Movie.CONTENT_TYPE;
            case MOVIES_ID:
                return MoviesContract.Movie.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Not yet implemented" + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;

        switch (sUriMatcher.match(uri)){
            case MOVIES:{
                //insert movie and build its content uri if successful
                long insertedMovieId = db.insert(MoviesContract.Movie.TABLE_NAME,null,values);
                if (insertedMovieId > -1) {
                    notifyChange(uri);
                    final String movie_id = values.getAsString(MoviesContract.Movie.COLUMN_MOVIE_ID);
                    returnUri = MoviesContract.Movie.buildMovieUri(movie_id);
                } else {
                    throw new SQLException("Failed movie insertion (top_rated): " + uri);
                }
                break;
            }
            case METADATA:{
                //insert metadata and build its content uri if successful
                long insertedFavMovieId = db.insert(MoviesContract.Metadata.TABLE_NAME,null,values);
                if (insertedFavMovieId > -1) {
                    final String movie_id = values.getAsString(MoviesContract.Metadata.COLUMN_MOVIE_ID);
                    returnUri = MoviesContract.Movie.buildMovieUri(movie_id);
                    notifyChange(returnUri);
                } else {
                    throw new SQLException("Failed movie insertion (top_rated): " + uri);
                }
                break;
            }
            case MOVIES_REVIEWS:{
                //insert review and build its content uri if successful
                long insertedReviewId = db.insert(MoviesContract.Review.TABLE_NAME,null,values);
                if (insertedReviewId > -1) {
                    notifyChange(uri);
                    returnUri = MoviesContract.Review.buildReviewUri(insertedReviewId);
                } else {
                    throw new SQLException("Failed movie insertion (top_rated): " + uri);
                }
                break;
            }
            case MOVIES_VIDEOS:{
                //insert video and build its content uri if successful
                long insertedVideoId = db.insert(MoviesContract.Video.TABLE_NAME,null,values);
                if (insertedVideoId > -1) {
                    notifyChange(uri);
                    returnUri = MoviesContract.Video.buildVideoUri(insertedVideoId);
                } else {
                    throw new SQLException("Failed movie insertion (top_rated): " + uri);
                }
                break;
            }
            default:{
                throw new UnsupportedOperationException("Not yet implemented: " + uri);
            }
        }

        return returnUri;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // set
        String tableName;
        switch (sUriMatcher.match(uri)) {
            // "movies"
            case MOVIES: {
                tableName = MoviesContract.Movie.TABLE_MOVIE_JOINED_METADATA;
                break;
            }
            // "movies/*"
            case MOVIES_ID: {
                // return movie matching the movie_id
                tableName = MoviesContract.Movie.TABLE_MOVIE_JOINED_METADATA;
                selection = MoviesContract.Movie.TABLE_NAME
                        + "." + MoviesContract.Movie.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[]{uri.getPathSegments().get(1)};
                sortOrder = MoviesContract.Metadata.COLUMN_FAVORITE + " DESC";
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Cursor resultsCursor = mOpenHelper.getReadableDatabase().query(
                tableName,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        // watch the content URI for changes
        if(getContext()!=null){
            resultsCursor.setNotificationUri(getContext().getContentResolver(), uri);
        } else Log.d(TAG,"Unable to watch content Uri for changes.");
        return resultsCursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int rowsUpdated = 0;
        switch (sUriMatcher.match(uri)) {
            // "movies/*"   {only the movie's favorite field can be updated}
            case MOVIES_ID: {
                // return movies with popular flag
                String movieId = uri.getPathSegments().get(1);
                // selection will find a favorite data entry for a given movie
                selection = MoviesContract.Metadata.COLUMN_MOVIE_ID + "=? AND "
                        + MoviesContract.Metadata.COLUMN_TAG + "=0";
                selectionArgs = new String[]{movieId};
                rowsUpdated = mOpenHelper.getWritableDatabase().update(
                        MoviesContract.Metadata.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                if(rowsUpdated>0) {
                    // notify that favorite field has changed
                    notifyChange(uri);
                } else {
                    // handle the favorite entry not yet existing
                    ContentValues valuesToInsert = new ContentValues();
                    valuesToInsert.put(MoviesContract.Metadata.COLUMN_MOVIE_ID,movieId);
                    valuesToInsert.put(MoviesContract.Metadata.COLUMN_FAVORITE, MoviesContract.Metadata.FLAG_FAVORITE);
                    long rowId = mOpenHelper.getWritableDatabase().insert(
                            MoviesContract.Metadata.TABLE_NAME,
                            null,valuesToInsert);
                    if(rowId>-1) {
                        rowsUpdated = 1;
                        notifyChange(uri);
                    }
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return rowsUpdated;
    }

    private void notifyChange(Uri uri) {
        Context context = getContext();
        if(context!=null) context.getContentResolver().notifyChange(uri, null);
    }

        /** Create our provider's UriMatcher */
    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        // match general movies categories
        matcher.addURI(authority, MoviesContract.PATH_MOVIES_POPULAR, MOVIES_POPULAR);  // "movies/popular"
        matcher.addURI(authority, MoviesContract.PATH_MOVIES_TOP_RATED, MOVIES_TOP_RATED);  // "movies/toprated"
        matcher.addURI(authority, MoviesContract.PATH_MOVIES_FAVORITE, MOVIES_FAVORITE);  // "movies/favorite"
        matcher.addURI(authority, MoviesContract.PATH_MOVIES_VIDEOS, MOVIES_VIDEOS);  // "movies/videos"
        matcher.addURI(authority, MoviesContract.PATH_MOVIES_REVIEWS, MOVIES_REVIEWS);  // "movies/reviews"

        // match category tied to a specific movie
//        matcher.addURI(authority, MoviesContract.PATH_MOVIES_ID_POPULAR, MOVIES_ID_POPULAR);  // "movies/*/popular"
//        matcher.addURI(authority, MoviesContract.PATH_MOVIES_ID_TOP_RATED, MOVIES_ID_TOP_RATED);   // "movies/*/toprated"
//        matcher.addURI(authority, MoviesContract.PATH_MOVIES_ID_FAVORITE, MOVIES_ID_FAVORITE);  // "movies/*/favorite"
        matcher.addURI(authority, MoviesContract.PATH_MOVIES_ID_VIDEOS, MOVIES_ID_VIDEOS);  // "movies/*/videos"
        matcher.addURI(authority, MoviesContract.PATH_MOVIES_ID_REVIEWS, MOVIES_ID_REVIEWS);  // "movies/*/reviews"
        matcher.addURI(authority, MoviesContract.PATH_MOVIES_ID, MOVIES_ID);  // "movies/*"

        // match all movies
        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIES);  // "movies"

        // match metadata
        matcher.addURI(authority, MoviesContract.PATH_METADATA, METADATA); // "metadata"

        return matcher;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.Movie.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                notifyChange(uri);
                return returnCount;
            }
            case METADATA: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.Metadata.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                notifyChange(uri);
                return returnCount;
            }
            case MOVIES_REVIEWS: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.Review.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                notifyChange(uri);
                return returnCount;
            }
            case MOVIES_VIDEOS: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.Video.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                notifyChange(uri);
                return returnCount;
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }


//    INSERT INTO movie (id, name, title, content, author)
//    SELECT old.id, new.name, new.title, old.content, new.author
//    FROM ( SELECT
//           ? AS name,
//           "About this site" AS title,
//           42                AS author
//            ) AS new
//    LEFT JOIN (
//            SELECT id, name, content
//            FROM page
//    ) AS old ON new.name = old.name;
}
