package com.ascendantbrain.android.popularmovies.ui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ascendantbrain.android.popularmovies.BuildConfig;
import com.ascendantbrain.android.popularmovies.R;
import com.ascendantbrain.android.popularmovies.model.Movie;
import com.ascendantbrain.android.popularmovies.model.Review;
import com.ascendantbrain.android.popularmovies.model.Video;
import com.ascendantbrain.android.popularmovies.network.DataRequester;
import com.ascendantbrain.android.popularmovies.provider.MoviesContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link PosterActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = MovieDetailFragment.class.getSimpleName();
    private static final boolean DEBUG = false;

    /** The argument representing the movie id for this fragment */
    public static final String KEY_MOVIE_ID = "movie_id";

    // movie data
    private String mMovieId = null;
    private Movie mMovie = null;
    private int mLoaderId_MovieDetails;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        if(mMovieId!=null) outState.putString(KEY_MOVIE_ID, mMovieId);
//        super.onSaveInstanceState(outState);
//    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(DEBUG) Log.e(TAG,"Detail fragment:: onActivityCreated() checkpoint.");

        if (getArguments()!=null && getArguments().containsKey(KEY_MOVIE_ID)) {
            if(DEBUG) Log.d(TAG,"Found MovieID in getArguments()!");
            // retrieve movie_id contained in arguments
            mMovieId = getArguments().getString(KEY_MOVIE_ID);
        }

//        if(savedInstanceState!=null) {
//            Log.e(TAG,"Found MovieID in savedInstanceState!");
//            mMovieId = savedInstanceState.getString(KEY_MOVIE_ID);
//        }

        if(mMovieId!=null){
            // load content from our content provider.
            if(DEBUG) Log.d(TAG,"Found MovieID::"+mMovieId);
            mLoaderId_MovieDetails = Integer.parseInt(mMovieId);
            getActivity().getSupportLoaderManager().initLoader(mLoaderId_MovieDetails,null,this);
        }

        View mainView = getView();
        if(mainView!=null) {
            loadReviewData((LinearLayout) mainView.findViewById(R.id.review_container));
            loadVideoData((LinearLayout) mainView.findViewById(R.id.video_container));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.movie_detail, container, false);
    }

    /** Instantiate and return a new Loader for the given ID. */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle args) {

        if(DEBUG) Log.i(TAG,"MovieId="+mMovieId);
        Uri movieUri = MoviesContract.Movie.buildMovieUri(mMovieId);

        if(loaderID == mLoaderId_MovieDetails){
            // Returns a new CursorLoader
            return new CursorLoader(
                    getActivity(),                                      // Parent activity context
                    movieUri,                                           // Content to query
                    Movie.PROJECTION,                                   // Projection to return
                    null,                                               // Selection clause
                    null,                                               // Selection arguments
                    null                                                // Default sort order
            );
        } else {
            throw new UnsupportedOperationException("Loader not yet implemented:"+loaderID);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int loaderID = loader.getId();

        if(loaderID == mLoaderId_MovieDetails){
            if(cursor.moveToFirst()) {
                mMovie = Movie.fromCursor(cursor);
                if(DEBUG) Log.i(TAG, String.format(Locale.US,
                        "Movie details loaded. Title:%s MovieId:%s",mMovie.title,mMovie.movie_id));
                bindViewData(mMovie);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    private void bindViewData(Movie movie){
        // get fragment's root view
        View view = getView();
        if(view==null) return;

        // movie title
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) getActivity().findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) appBarLayout.setTitle(movie.title);

        // lookup views
        ImageView backdropView = (ImageView)getActivity().findViewById(R.id.movie_backdrop);
        ImageView posterView = (ImageView)view.findViewById(R.id.movie_poster);
        TextView yearView = (TextView) view.findViewById(R.id.movie_year);
        TextView dateView = (TextView) view.findViewById(R.id.movie_date);
        TextView ratingView = (TextView)view.findViewById(R.id.movie_rating);
        TextView overviewView = (TextView)view.findViewById(R.id.movie_overview);
        ToggleButton favorite = (ToggleButton)view.findViewById(R.id.favorite_button);

        favorite.setVisibility(View.VISIBLE);
        favorite.setChecked(movie.isFavorite);

        // bind data
        Picasso.with(getActivity()).load(movie.getBackdropPath()).into(backdropView);
        Picasso.with(getActivity()).load(movie.getPosterPath()).placeholder(R.drawable.poster_placeholder).into(posterView);
        yearView.setText(movie.release_year);
        dateView.setText(movie.release_date);
        ratingView.setText(String.format(Locale.US,"%.1f / 10",movie.vote_average));
        overviewView.setText(movie.overview);
        favorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // create favorite data to be updated
                ContentValues values = new ContentValues();
                int updatedFavoriteValue = isChecked ? MoviesContract.Metadata.FLAG_FAVORITE : 0;
                values.put(MoviesContract.Metadata.COLUMN_FAVORITE,updatedFavoriteValue);

                // update our movie's favorite field
                Uri movieUri = MoviesContract.Movie.buildMovieUri(mMovieId);
                int rowsupdated = getContext().getContentResolver().update(movieUri,values,null,null);
                if(rowsupdated<1) Log.w(TAG, "Updated favorite value not saved!!!");
            }
        });
    }

    private void loadReviewData(@NonNull final ViewGroup viewContainer){
        final Context context = getActivity();

        final  Uri MOVIEDB_BASE_URI = Uri.parse("http://api.themoviedb.org/3/movie");
        final  String APPID_PARAM = "api_key";
        final String PATH_REVIEWS = "reviews";

        Uri reviewUri = MOVIEDB_BASE_URI.buildUpon()
                .appendPath(mMovieId)
                .appendPath(PATH_REVIEWS)
                .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIE_API_KEY)
                .build();

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, reviewUri.toString(), null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Context context = getActivity();
                        LayoutInflater inflater = LayoutInflater.from(context);

                        // parse reviews from JSON response
                        List<Review> reviewList = parseReviewsFromJson(response);

                        // show card if reviews are available
                        if(reviewList.size()>0) {
                            View view = getView();
                            if(view==null) return;
                            CardView reviewCard = (CardView) view.findViewById(R.id.card_review);
                            if(reviewCard!=null) reviewCard.setVisibility(View.VISIBLE);
                        }

                        // add review entries inside card
                        for(Review review: reviewList){
                            View view = inflater.inflate(R.layout.review_cell,null);
                            TextView author = (TextView) view.findViewById(R.id.author);
                            author.setText(String.format(Locale.US,"by %s",review.author));

                            TextView comment = (TextView) view.findViewById(R.id.comment);
                            comment.setText(String.format(Locale.US,"\"%s\"",review.review));

                            viewContainer.addView(view);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.w(TAG,"Network request error. "+error.networkResponse);
                    }
                });
        DataRequester.getInstance(context).addToRequestQueue(jsObjRequest);
    }

    private void loadVideoData(final ViewGroup viewContainer){
        final Context context = getActivity();

        final  Uri MOVIEDB_BASE_URI = Uri.parse("http://api.themoviedb.org/3/movie");
        final  String APPID_PARAM = "api_key";
        final String PATH_REVIEWS = "videos";

        Uri videoUri = MOVIEDB_BASE_URI.buildUpon()
                .appendPath(mMovieId)
                .appendPath(PATH_REVIEWS)
                .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIE_API_KEY)
                .build();

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, videoUri.toString(), null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        final Context context = getActivity();
                        LayoutInflater inflater = LayoutInflater.from(context);

                        // parse videos from JSON response
                        List<Video> videoList = parseVideosFromJson(response);

                        // show card if videos are available
                        if(videoList.size()>0) {
                            View view = getView();
                            if(view==null) return;
                            CardView videoCard = (CardView) view.findViewById(R.id.card_videos);
                            if(videoCard!=null) videoCard.setVisibility(View.VISIBLE);
                        }

                        // create video entries and insert into card
                        for(final Video video: videoList){
                            View view = inflater.inflate(R.layout.video_cell,viewContainer,false);

                            // find video block views
                            ImageView thumbnail = (ImageView) view.findViewById(R.id.image);
                            ImageView button_play = (ImageView) view.findViewById(R.id.button_play);
                            TextView name = (TextView) view.findViewById(R.id.name);
                            TextView type = (TextView) view.findViewById(R.id.type);

                            // bind views with data
                            Picasso.with(context).load(video.getThumbnailUri()).into(thumbnail);
                            name.setText(video.name);
                            type.setText(String.format(Locale.US,"\"%s\"",video.type));
                            button_play.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    video.launchVideo(getActivity());
                                }
                            });

                            viewContainer.addView(view);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.w(TAG,"Network request error. "+error.networkResponse);
                    }
                });
        DataRequester.getInstance(context).addToRequestQueue(jsObjRequest);
    }


    private List<Review> parseReviewsFromJson(@NonNull final JSONObject responseObject) {
        List<Review> reviewList = new ArrayList<>();

        /** JSON keys used for parsing */

        // JSON keys for the data envelope (id, results; ignore: page, total_pages, total_results)
        final String MDB_MOVIE_ID = "id";
        final String MDB_RESULTS_ARRAY = "results";

        try{
            // parse movie id
            String movie_id = responseObject.getString(MDB_MOVIE_ID);

            // parse results array
            JSONArray reviewsArray = responseObject.getJSONArray(MDB_RESULTS_ARRAY);
            if(reviewsArray==null) return reviewList;  // array is empty; no need to parse

            // parse array of reviews
            Review review = null;
            for(int i=0; i < reviewsArray.length(); i++){
                // parse next review and add to list
                review = Review.fromJsonObject(movie_id,reviewsArray.getJSONObject(i));
                if(review!=null) reviewList.add(review);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return reviewList;
    }

    private List<Video> parseVideosFromJson(@NonNull final JSONObject responseObject) {
        List<Video> videoList = new ArrayList<>();

        /** JSON keys used for parsing */

        // JSON keys for the data envelope (id, results; ignore: page, total_pages, total_results)
        final String MDB_MOVIE_ID = "id";
        final String MDB_RESULTS_ARRAY = "results";

        try{
            // parse movie id
            String movie_id = responseObject.getString(MDB_MOVIE_ID);

            // parse results array
            JSONArray videosArray = responseObject.getJSONArray(MDB_RESULTS_ARRAY);
            if(videosArray==null) return videoList;  // array is empty; no need to parse

            // parse array of reviews
            Video video = null;
            for(int i=0; i < videosArray.length(); i++){
                // parse next review and add to list
                video = Video.fromJsonObject(movie_id,videosArray.getJSONObject(i));
                if(video!=null) videoList.add(video);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return videoList;
    }

}
