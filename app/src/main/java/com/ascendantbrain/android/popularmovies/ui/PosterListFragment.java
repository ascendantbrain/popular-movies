package com.ascendantbrain.android.popularmovies.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.ascendantbrain.android.popularmovies.R;
import com.ascendantbrain.android.popularmovies.model.Poster;
import com.ascendantbrain.android.popularmovies.provider.MoviesContract;

import java.util.Locale;

/**
 * A fragment representing a list of movies.
 */
public class PosterListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private final static String TAG = PosterListFragment.class.getSimpleName();
    private final static boolean DEBUG = false;

    // ids to reference our tagged queries
    private final static int POPULAR = 0;
    private final static int TOP_RATED = 1;
    private final static int FAVORITES = 2;
    private int mCurrentQuery = POPULAR;
    private static final String KEY_CURRENT_QUERY = "current_query";

    private boolean mIsDataLoading = false;

    private PosterRecyclerViewCursorAdapter mPosterAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PosterListFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        mPosterAdapter = new PosterRecyclerViewCursorAdapter(context,null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null) {
            mCurrentQuery = savedInstanceState.getInt(KEY_CURRENT_QUERY);
        }
        requestData(mCurrentQuery);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_poster_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.movie_list);
        recyclerView.setAdapter(mPosterAdapter);
        if(getActivity() instanceof PosterRecyclerViewCursorAdapter.ItemSelectionListener){
            mPosterAdapter.setSelectionListener((PosterRecyclerViewCursorAdapter.ItemSelectionListener) getActivity());
        }
    }

    @MainThread
    public void requestData(int requestedQuery){
        if(DEBUG) Log.d(TAG,String.format(Locale.US,"Requesting data: %d (%d)",requestedQuery,mCurrentQuery));

        LoaderManager loaderManager = getLoaderManager();
        if(!mIsDataLoading) {
            // start loader
            mIsDataLoading = true;
            mCurrentQuery = requestedQuery;
            loaderManager.initLoader(mCurrentQuery,null,this);
        } else {
            // interrupt prior loading and request new data
            if (requestedQuery != mCurrentQuery) {
                if(DEBUG) Log.d(TAG,String.format(Locale.US,"Interrupting previous request %d to start %d.",requestedQuery,mCurrentQuery));
                loaderManager.getLoader(mCurrentQuery).cancelLoad();
                loaderManager.initLoader(requestedQuery, null, this);
                loaderManager.destroyLoader(mCurrentQuery);
                mCurrentQuery = requestedQuery;
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle)
    {
    /** Configure loader based on the requested id  */
        switch (loaderID) {
            case POPULAR: {
                // Returns a new CursorLoader
                final String selection = MoviesContract.Metadata.COLUMN_TAG + "=?";
                final String[] selectionArgs = {String.valueOf(MoviesContract.Metadata.TAG_POPULAR)};
                return new CursorLoader(
                        getActivity(),   // Parent activity context
                        MoviesContract.Movie.CONTENT_URI,                   // Table to query
                        Poster.POSTER_PROJECTION,                           // Projection to return
                        selection,                                          // selection clause
                        selectionArgs,                                      // selection arguments
                        null                                                // Default sort order
                );
            }
            case TOP_RATED:{
                // Returns a new CursorLoader
                final String selection = MoviesContract.Metadata.COLUMN_TAG + "=?";
                final String[] selectionArgs = {String.valueOf(MoviesContract.Metadata.TAG_TOP_RATED)};
                return new CursorLoader(
                        getActivity(),                                      // Parent activity context
                        MoviesContract.Movie.CONTENT_URI,                   // Table to query
                        Poster.POSTER_PROJECTION,                           // Projection to return
                        selection,                                          // selection clause
                        selectionArgs,                                      // selection arguments
                        null                                                // Default sort order
                );
            }
            case FAVORITES:{
                // Returns a new CursorLoader
                final String selection = MoviesContract.Metadata.COLUMN_FAVORITE + "=?";
                String[] selectionArgs = {String.valueOf(MoviesContract.Metadata.FLAG_FAVORITE)};
                return new CursorLoader(
                        getActivity(),   // Parent activity context
                        MoviesContract.Movie.CONTENT_URI,                   // Table to query
                        Poster.POSTER_PROJECTION,                           // Projection to return
                        selection,                                          // selection clause
                        selectionArgs,                                      // selection arguments
                        null                                                // Default sort order
                );
            }
            default:
                // invalid id was passed in
                if(DEBUG) Log.e(TAG,"Invalid loader id received ("+loaderID+")");
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(DEBUG) Log.d(TAG,"onLoadFinished() checkpoint -> ReceivedId=" + loader.getId() + "  DisplayedId=" + mCurrentQuery);

        // replace cursor used by adapter
//        mIsDataLoading = false;
        if(mCurrentQuery == loader.getId()) {
            mPosterAdapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(DEBUG) Log.d(TAG,"onLoaderReset() checkpoint -> loader=" + loader.getId() );

        // invalidate cursor data set in the adapter
        if(mCurrentQuery == loader.getId()) {
            mPosterAdapter.changeCursor(null);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(DEBUG) Log.e(TAG,"onCreateOptionsMenu() checkpoint   currentQuery="+ mCurrentQuery);
        inflater.inflate(R.menu.poster_list_fragment_menu, menu);

        // setup spinner adapter using actionbar themed context if available
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        Context spinnerContext = actionBar==null ? activity : actionBar.getThemedContext();
        ArrayAdapter spinnerAdapter = ArrayAdapter.createFromResource(
                spinnerContext,
                R.array.list_select_spinner,
                android.R.layout.simple_spinner_item);  // R.layout.list_select_spinner_item
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  // R.layout.list_select_spinner_item

        // setup actionbar menu spinner
        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(mCurrentQuery,false);
        spinner.setOnItemSelectedListener(mOnMenuItemSelected);
    }

    AdapterView.OnItemSelectedListener mOnMenuItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            requestData(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}     // do nothing

    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_CURRENT_QUERY,mCurrentQuery);
        super.onSaveInstanceState(outState);
    }
}
