package com.ascendantbrain.android.popularmovies.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ascendantbrain.android.popularmovies.BuildConfig;
import com.ascendantbrain.android.popularmovies.provider.MoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MovieSyncHelper {
    private final static String TAG = MovieSyncHelper.class.getSimpleName();
    private final static boolean DEBUG = false;

    private final static Uri MOVIEDB_BASE_URI = Uri.parse("http://api.themoviedb.org/3/movie");
    private final static String APPID_PARAM = "api_key";

    Context mContext;

    public static MovieSyncHelper getInstance(@NonNull Context context){
        return new MovieSyncHelper(context);
    }

    private MovieSyncHelper(Context context){
        mContext = context;
    }

    public void performMoviesSync(){
        // request movie data from MovieDB website
        String popularMoviesJson = syncPopularMovies();
        String topratedMoviesJson = syncTopRatedMovies();

        // create lists for movie and metadata content values
        List<ContentValues> movieList = new ArrayList<ContentValues>();
        List<ContentValues> metadataList = new ArrayList<ContentValues>();


        // parse and add movie and metadata to our content value lists
        parseMoviesFromJson(popularMoviesJson,MoviesContract.PopularMovie.CATEGORY, movieList, metadataList);
        parseMoviesFromJson(topratedMoviesJson,MoviesContract.TopRatedMovie.CATEGORY, movieList, metadataList);
        if(movieList.size()<1) {
            Log.w(TAG,"Failed to sync movies.");
            return;
        }

        // clean data set from earlier syncs
        deletePreviousEntries();

        // convert our lists into arrays to support bulk insert
        ContentValues[] movieArray = new ContentValues[movieList.size()];
        movieList.toArray(movieArray);
        ContentValues[] metadataArray = new ContentValues[metadataList.size()];
        metadataList.toArray(metadataArray);


        // perform bulk insert
        ContentResolver contentResolver = mContext.getContentResolver();
        contentResolver.bulkInsert(MoviesContract.Movie.CONTENT_URI,movieArray);
        contentResolver.bulkInsert(MoviesContract.Metadata.CONTENT_URI,metadataArray);
//        contentResolver.bulkInsert(MoviesContract.Review.CONTENT_URI,reviewArray);
//        contentResolver.bulkInsert(MoviesContract.Video.CONTENT_URI,videoArray);
    }

    public String syncPopularMovies(){
        final String PATH_POPULAR = "popular";

        // build path for popular movies
        //    .../movie/popular
        Uri movieUri = MOVIEDB_BASE_URI.buildUpon()
                .appendPath(PATH_POPULAR)
                .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIE_API_KEY)
                .build();

        return fetchJsonFromUri(movieUri);
    }

    public String syncTopRatedMovies(){
        final String PATH_TOP_RATED = "top_rated";

        // build path for top rated movies
        //    .../movie/top_rated
        Uri movieUri = MOVIEDB_BASE_URI.buildUpon()
                .appendPath(PATH_TOP_RATED)
                .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIE_API_KEY)
                .build();

        return fetchJsonFromUri(movieUri);
    }

    public List<String> syncMovieReviews(List<ContentValues> movieList){
        final String PATH_REVIEWS = "reviews";

        // build path for movie reviews
        //   .../movie/*/reviews
        List<String> reviewJsonList = new ArrayList<>();
        Uri movieUri;
        for(ContentValues movie: movieList){
            movieUri = MOVIEDB_BASE_URI.buildUpon()
                    .appendPath(movie.getAsString(MoviesContract.Movie.COLUMN_MOVIE_ID))
                    .appendPath(PATH_REVIEWS)
                    .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIE_API_KEY)
                    .build();
            reviewJsonList.add(fetchJsonFromUri(movieUri));
        }
        return reviewJsonList;
    }

    public List<String> syncMovieVideos(List<ContentValues> movieList){
        final String PATH_VIDEOS = "videos";

        // build path for movie reviews
        //   .../movie/*/reviews
        List<String> videoJsonList = new ArrayList<>();
        Uri movieUri;
        for(ContentValues movie: movieList){
            movieUri = MOVIEDB_BASE_URI.buildUpon()
                    .appendPath(movie.getAsString(MoviesContract.Movie.COLUMN_MOVIE_ID))
                    .appendPath(PATH_VIDEOS)
                    .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIE_API_KEY)
                    .build();
            videoJsonList.add(fetchJsonFromUri(movieUri));
        }

        return videoJsonList;
    }


    public String fetchJsonFromUri(Uri reviewUri){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // raw JSON response
        String resultsJsonString = null;

        try {
            URL url = new URL(reviewUri.toString());

            /** create get request and open connection */
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            int response = urlConnection.getResponseCode();
            if(response>=400){
                Log.w(TAG,String.format(Locale.US,"HTTP connection failure (code=%d)",response));
                return "";
            };

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) return ""; // cancel sync given inputStream failure
            reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuffer buffer = new StringBuffer();
            final String LINE_RETURN = "\n";
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append(LINE_RETURN);
            }

            if (buffer.length() == 0) return "";  // no data to parse

            // parse response as JSON
            resultsJsonString =  buffer.toString();

        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }
        return resultsJsonString;
    }


    private boolean parseMoviesFromJson(@NonNull String movieJsonString,
                                                    int category,
                                                    @NonNull List<ContentValues> movieList,
                                                    @NonNull List<ContentValues> metadataList) {
        // reqs: title, release date, movie poster, vote average, and plot synopsis.
        // suggested: backdrop image

        /** JSON keys used for parsing */
        // {"id":"*","results":[{movie}]}
        final String MDB_RESULTS_ARRAY = "results";

        // JSON keys for our movie data
        final String MDB_POSTER_PATH = "poster_path";
        final String MDB_BACKDROP_PATH = "backdrop_path";
        final String MDB_OVERVIEW = "overview";
        final String MDB_RELEASE_DATE = "release_date";
        final String MDB_TITLE = "title";
        final String MDB_VOTE_AVERAGE = "vote_average";
        final String MDB_MOVIE_ID = "id";

        ContentValues values;

        try{
            JSONObject moviesJson = new JSONObject(movieJsonString);
            JSONArray resultsMovieArray = moviesJson.getJSONArray(MDB_RESULTS_ARRAY);

            for(int i=0; i < resultsMovieArray.length(); i++){
                // retrieve data from json movie object
                JSONObject jsonObject = resultsMovieArray.getJSONObject(i);
                final String movie_id = jsonObject.getString(MDB_MOVIE_ID);

                // movie content values
                values = new ContentValues();
                values.put(MoviesContract.Movie.COLUMN_TITLE,jsonObject.getString(MDB_TITLE));
                values.put(MoviesContract.Movie.COLUMN_OVERVIEW,jsonObject.getString(MDB_OVERVIEW));
                values.put(MoviesContract.Movie.COLUMN_POSTER_PATH,jsonObject.getString(MDB_POSTER_PATH));
                values.put(MoviesContract.Movie.COLUMN_BACKDROP_PATH,jsonObject.getString(MDB_BACKDROP_PATH));
                values.put(MoviesContract.Movie.COLUMN_RELEASE_DATE,jsonObject.getString(MDB_RELEASE_DATE));
                values.put(MoviesContract.Movie.COLUMN_VOTE_AVERAGE,jsonObject.getString(MDB_VOTE_AVERAGE));
                values.put(MoviesContract.Movie.COLUMN_MOVIE_ID,movie_id);
                movieList.add(values);

                // metadata content values
                values = new ContentValues();
                values.put(MoviesContract.Metadata.COLUMN_MOVIE_ID,movie_id);
                values.put(MoviesContract.Metadata.COLUMN_TAG,String.valueOf(category));
                metadataList.add(values);
            }
            return true;
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
    }

    private int deletePreviousEntries(){
        // delete old entries
        int rowsDeleted = 0;
        ContentResolver contentResolver = mContext.getContentResolver();
        rowsDeleted += contentResolver.delete(MoviesContract.Movie.CONTENT_URI,null,null);
        rowsDeleted += contentResolver.delete(MoviesContract.Metadata.CONTENT_URI,null,null);
        return rowsDeleted;
    }
}
