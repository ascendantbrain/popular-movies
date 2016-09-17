package com.ascendantbrain.android.popularmovies.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.ascendantbrain.android.popularmovies.R;
import com.ascendantbrain.android.popularmovies.sync.MovieSyncAdapter;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class PosterActivity extends AppCompatActivity implements PosterRecyclerViewCursorAdapter.ItemSelectionListener{
    private final static String TAG = PosterActivity.class.getSimpleName();
    private final static boolean DEBUG = false;

    /** record if activity is in two-pane mode, i.e. running on a tablet device. */
    private boolean mTwoPane;

    private static final String TAG_DETAIL_FRAGMENT = "tag_detail_fragment";
    private static final String KEY_MOVIE_ID = "movie_id";

    private String mSelectedMovieId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // restore saved state
        if(savedInstanceState!=null){
            mSelectedMovieId = savedInstanceState.getString(KEY_MOVIE_ID);
        }

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts.
            // If this view is present then the activity should be in two-pane mode.
            if (DEBUG) Log.d(TAG, "Dual pane mode");
            mTwoPane = true;
            // In two-pane mode, show the detail fragment in this activity
            if (savedInstanceState != null) {
                // only show detail fragment if a movie has already been selected
                if(mSelectedMovieId!=null) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    Fragment detailFragment = fragmentManager.findFragmentByTag(TAG_DETAIL_FRAGMENT);
                    fragmentManager.beginTransaction()
                            .replace(R.id.movie_detail_container, detailFragment, TAG_DETAIL_FRAGMENT)
                            .commit();
                }

            }
        }

        // sync data if this is the first time running
        if(savedInstanceState==null){
            MovieSyncAdapter.initializeSyncAdapter(this);
            MovieSyncAdapter.syncImmediately(this);
        }
    }

    @Override
    public void onItemSelected(String movieId) {
        if(DEBUG) Log.d(TAG,"Launch movie detail screen: "+movieId);
        // updated selected movie id
        mSelectedMovieId = movieId;

        // launch detail fragment
        if(mTwoPane){
            // using two panes... launch detail fragment into its view container
            Bundle arguments = new Bundle();
            arguments.putString(MovieDetailFragment.KEY_MOVIE_ID,mSelectedMovieId);

            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, TAG_DETAIL_FRAGMENT)
                    .commit();
        } else {
            // using single pane... launch detail activity
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra(MovieDetailFragment.KEY_MOVIE_ID, mSelectedMovieId);
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_MOVIE_ID,mSelectedMovieId);
        super.onSaveInstanceState(outState);
    }
}
