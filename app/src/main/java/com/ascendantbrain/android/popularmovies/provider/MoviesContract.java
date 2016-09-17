/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ascendantbrain.android.popularmovies.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import com.ascendantbrain.android.popularmovies.BuildConfig;

/**
 * Defines table and column names.
 */
public class MoviesContract {

    /** our content provider identifier */
    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID;

    /** the base of all Uris used to connect with our content provider */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String MOVIES = "movies";
    public static final String POPULAR = "popular";
    public static final String TOP_RATED = "toprated";
    public static final String FAVORITE = "favorite";
    public static final String REVIEWS = "reviews";
    public static final String VIDEOS = "videos";
    public static final String MOVIES_SLASH = MOVIES + "/";
    public static final String MOVIES_ID = MOVIES_SLASH + "*/";
    public static final String METADATA = "metadata";

    /** paths to supported content */
    public static final String PATH_MOVIES = MOVIES;
    public static final String PATH_MOVIES_ID = MOVIES_SLASH + "*";
    public static final String PATH_MOVIES_POPULAR = MOVIES_SLASH + POPULAR;
    public static final String PATH_MOVIES_ID_POPULAR = MOVIES_ID + POPULAR;
    public static final String PATH_MOVIES_TOP_RATED = MOVIES_SLASH + TOP_RATED;
    public static final String PATH_MOVIES_ID_TOP_RATED = MOVIES_ID + TOP_RATED;
    public static final String PATH_MOVIES_FAVORITE = MOVIES_SLASH + FAVORITE;
    public static final String PATH_MOVIES_ID_FAVORITE = MOVIES_ID + FAVORITE;
    public static final String PATH_MOVIES_REVIEWS = MOVIES_SLASH + REVIEWS;
    public static final String PATH_MOVIES_ID_REVIEWS = MOVIES_ID + REVIEWS;
    public static final String PATH_MOVIES_VIDEOS = MOVIES_SLASH + VIDEOS;
    public static final String PATH_MOVIES_ID_VIDEOS = MOVIES_ID + VIDEOS;
    public static final String PATH_METADATA = METADATA;

    /**
     * Inner class that defines the table contents of a movie.
     * This class is used primarily to support joins and is not queried directly.
     */
    public static class Movie implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        // Table name
        public static final String TABLE_NAME = "movie";

        // Joined table name (joins movies with corresponding metadata)
        public static final String TABLE_MOVIE_JOINED_METADATA = TABLE_NAME
                + " JOIN " + Metadata.TABLE_NAME + " AS D"
                + " ON " + TABLE_NAME + "." + Movie.COLUMN_MOVIE_ID
                + " = D." + Metadata.COLUMN_MOVIE_ID;

        // .../movie/*
        public static Uri buildMovieUri(String movie_id) {
            return CONTENT_URI.buildUpon().appendPath(movie_id).build();
        }

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";

        // metadata identifier

    }

    /** Convenience class providing easy access to popular movies */
    public static final class PopularMovie extends Movie {

        // uniquely identify the popular movie category
        public static final int CATEGORY = 1;
    }

    /** Convenience class providing easy access to top rated movies */
    public static final class TopRatedMovie extends Movie {

        // uniquely identify the top rated movie category
        public static final int CATEGORY = 2;
    }


    /**
     * Inner class to capture application specific movie metadata.
     * Its primary purpose is to support definitions for joining movies and metadata.
     */
    public static final class Metadata implements BaseColumns {

        // content uri to manage our movie metadata
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_METADATA).build();

        // Table name
        public static final String TABLE_NAME = "metadata";

        // data fields (primary key _ID is defined in BaseColumns)
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TAG = "movie_tag";
        public static final String COLUMN_FAVORITE = "favorite";

        // tag movies to capture searchable properties
        public static final int TAG_POPULAR = 1;
        public static final int TAG_TOP_RATED = 2;

        // flag indicating favorite status for our favorite field
        public static final int FLAG_FAVORITE = 1;
    }

    /**
     * Inner class that defines the table contents of movie videos.
     */
    public static final class Video implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendEncodedPath(PATH_MOVIES_VIDEOS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES_VIDEOS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES_VIDEOS;

        // Table name
        public static final String TABLE_NAME = "video";

        public static Uri buildVideoUri(long movie_id) {
            return ContentUris.withAppendedId(CONTENT_URI, movie_id);
        }

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_VIDEO_ID = "video_id";

    }

    /**
     * Inner class that defines the table contents of movie reviews.
     */
    public static final class Review implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendEncodedPath(PATH_MOVIES_REVIEWS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES_REVIEWS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES_REVIEWS;

        // Table name
        public static final String TABLE_NAME = "review";

        public static Uri buildReviewUri(long movie_id) {
            return ContentUris.withAppendedId(CONTENT_URI, movie_id);
        }

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_REVIEW_ID = "review_id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_REVIEW = "review";

    }

//    // create table for movie reviews
//    final String SQL_CREATE_MOVIE_REVIEW_TABLE = "CREATE TABLE " + Review.TABLE_NAME + " (" +
//            Review._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
//            Review.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
//            Review.COLUMN_AUTHOR + " TEXT NOT NULL, " +
//            Review.COLUMN_REVIEW + " TEXT NOT NULL, " +
//            " FOREIGN KEY(" + Review.COLUMN_MOVIE_ID + ") REFERENCES " +
//            Movie.TABLE_NAME + "(" + Movie.COLUMN_MOVIE_ID + "));";
//    // create table for movie videos
//    final String SQL_CREATE_MOVIE_VIDEO_TABLE = "CREATE TABLE " + Video.TABLE_NAME + " (" +
//            Video._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
//            Video.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
//            Video.COLUMN_KEY + " TEXT NOT NULL, " +
//            Video.COLUMN_NAME + " TEXT NOT NULL, " +
//            Video.COLUMN_SITE + " TEXT NOT NULL, " +
//            Video.COLUMN_TYPE + " TEXT NOT NULL, " +
//            " FOREIGN KEY(" + Video.COLUMN_MOVIE_ID + ") REFERENCES " +
//            Movie.TABLE_NAME + "(" + Movie.COLUMN_MOVIE_ID + "));";


//    @Retention(RetentionPolicy.SOURCE)
//    @StringDef({
//            COLUMN_MOVIE_ID,
//            COLUMN_TITLE,
//            COLUMN_OVERVIEW,
//            COLUMN_RELEASE_DATE,
//            COLUMN_POSTER_PATH,
//            COLUMN_VOTE_AVERAGE,
//            COLUMN_POSTER_PATH
//    })
//    public @interface MovieDetail {}


    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
//    public static long normalizeDate(long startDate) {
//        // normalize the start date to the beginning of the (UTC) day
//        Time time = new Time();
//        time.set(startDate);
//        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
//        return time.setJulianDay(julianDay);
//    }

//    /* Inner class that defines the table contents of the popular movie table */
//    public static final class PopularMovie implements BaseColumns {
//
//        public static final Uri CONTENT_URI =
//                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POPULAR).build();
//
//        public static final String CONTENT_TYPE =
//                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POPULAR;
//        public static final String CONTENT_ITEM_TYPE =
//                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POPULAR;
//
//        // Table name
//        public static final String TABLE_NAME = "popular_movies";
//
//        public static Uri buildPopularMovieUri(long id) {
//            return ContentUris.withAppendedId(CONTENT_URI, id);
//        }
//    }

//    /* Inner class that defines the table contents of the top rated movie table */
//    public static final class TopRatedMovie implements BaseColumns {
//
//        public static final Uri CONTENT_URI =
//                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TOP_RATED).build();
//
//        public static final String CONTENT_TYPE =
//                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TOP_RATED;
//        public static final String CONTENT_ITEM_TYPE =
//                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TOP_RATED;
//
//        public static final String TABLE_NAME = "top_rated_movies";
//
//        public static Uri buildTopRatedMovieUri(long id) {
//            return ContentUris.withAppendedId(CONTENT_URI, id);
//        }
//
//    }

//    /* Inner class that defines the table contents of the top rated movie table */
//    public static final class FavoriteMovie implements BaseColumns {
//
//        public static final Uri CONTENT_URI =
//                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE).build();
//
//        public static final String CONTENT_TYPE =
//                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;
//        public static final String CONTENT_ITEM_TYPE =
//                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;
//
//        public static final String TABLE_NAME = "favorite_movies";
//
//        public static Uri buildFavoriteMovieUri(long id) {
//            return ContentUris.withAppendedId(CONTENT_URI, id);
//        }
//
//    }

    //        // All movies not marked as favorites
//        public static final String WHERE_NOT_FAVORITE =
//                COLUMN_MOVIE_ID + " NOT IN (SELECT" + MetaData.COLUMN_MOVIE_ID +
//                        "WHERE '" + MetaData.COLUMN_FAVORITE + "' == 1 FROM " +
//                        MetaData.TABLE_NAME + ")";
//
//        public static final int POPULAR_TYPE = 0;
//        public static final int TOP_RATED_TYPE = 1;

//        // All popular movies
//        public static final String WHERE_POPULAR =
//                COLUMN_MOVIE_ID + " IN (SELECT" + MetaData.COLUMN_MOVIE_ID +
//                        "WHERE '" + MetaData.COLUMN_MOVIE_TYPE + "' == " +
//                        POPULAR_TYPE + " FROM " + MetaData.TABLE_NAME + ")";
//
//        // All top rated movies
//        public static final String WHERE_TOP_RATED =
//                COLUMN_MOVIE_ID + " IN (SELECT" + MetaData.COLUMN_MOVIE_ID +
//                        "WHERE '" + MetaData.COLUMN_MOVIE_TYPE + "' == " +
//                        TOP_RATED_TYPE + " FROM " + MetaData.TABLE_NAME + ")";
//

/**
 *  Inner class that defines the table contents of movie metadata.
 *  This class is used primarily to support joins and is not queried directly.
 */
//    public static final class MetaData implements BaseColumns {
//
//        // Table name
//        public static final String TABLE_NAME = "metadata";
//
//        public static final String COLUMN_MOVIE_ID = "movie_id";
//        public static final String COLUMN_FAVORITE = "favorite";
//        public static final String COLUMN_MOVIE_TYPE = "movie_type";
//
//    }

}
