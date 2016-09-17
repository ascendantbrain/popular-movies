package com.ascendantbrain.android.popularmovies.ui;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ascendantbrain.android.popularmovies.R;
import com.ascendantbrain.android.popularmovies.model.Poster;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class PosterRecyclerViewCursorAdapter extends RecyclerView.Adapter<PosterRecyclerViewCursorAdapter.ViewHolder> {
    private final static String TAG = PosterRecyclerViewCursorAdapter.class.getSimpleName();
    private final static boolean DEBUG = false;

    private Context mContext;
    private Cursor mCursor;
    private boolean mDataSetValid = false;

    private ItemSelectionListener mListener = setDefaultItemSelectionListener();

    public PosterRecyclerViewCursorAdapter(Context context, Cursor cursor) {
        mContext = context;
        if(cursor!=null) {
            mDataSetValid = true;
            mCursor = cursor;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_list_cell, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // bind data if data set is valid and cursor is updated to correct position
        if(mDataSetValid && mCursor.moveToPosition(position)){
            // create poster from cursor
            final Poster poster = Poster.fromCursor(mCursor);
            if(DEBUG) Log.d(TAG,"Poster:: title:"+poster.title);
            // load image into imageview
            Picasso.with(mContext).load(poster.getPath()).placeholder(R.drawable.poster_placeholder_noborder).into(holder.mImageView);

            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Notify listener that an item has been selected.
                    if(DEBUG) Log.i(TAG,String.format(Locale.US,
                            "Movie selected. Title:%s MovieId:%s",poster.title,poster.movieId));
                    mListener.onItemSelected(poster.movieId);
                }
            });
        } else {
            throw new IllegalStateException(mDataSetValid ?
                    "Unable to move cursor to position "+position:
                    "Data set not valid. Unable to bind data to views");
        }
    }

    @Override
    public int getItemCount() {
        if(mDataSetValid && mCursor!=null){
            return mCursor.getCount();
        }
        return 0;
    }

    public long getItemId(int position){
        if(mDataSetValid && mCursor!=null && mCursor.moveToPosition(position)){
            return Poster.fromCursor(mCursor).getId();
        }
        return 0;
    }

    /**
     * Change the adapter's cursor.  Any existing cursor will be closed.
     */
    public void changeCursor(Cursor cursor){
        Cursor oldCursor = swapCursor(cursor);
        if(oldCursor != null){
            oldCursor.close();
        }
    }

    /**
     * Swap out adapter cursor.
     */
    public Cursor swapCursor(Cursor newCursor){
        // return null if cursor doesn't change
        if(newCursor == mCursor) return null;

        // save reference to old cursor and cleanup observer
        final Cursor oldCursor = mCursor;
        if(oldCursor!=null){
            // unregister datasetobserver for the old cursor
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }

        // prepare new cursor to be used by adapter
        mCursor = newCursor;
        if(newCursor!=null){
            // assign our observer to handle data set changes
            mCursor.registerDataSetObserver(mDataSetObserver);
            mDataSetValid = true;
            notifyDataSetChanged();
        } else {
            mDataSetValid = false;
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView mImageView;

        public ViewHolder(View view) {
            super(view);
            mImageView = (ImageView) view.findViewById(R.id.imageview_poster);
        }
    }

    /**
     * Observer to handle data set changed events in our cursor.
     */
    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        /**
         * This method is called when the entire data set has changed,
         * most likely through a call to {@link Cursor#requery()} on a {@link Cursor}.
         */
        @Override
        public void onChanged() {
            super.onChanged();
            mDataSetValid = true;
            notifyDataSetChanged();
        }

        /**
         * This method is called when the entire data becomes invalid,
         * most likely through a call to {@link Cursor#deactivate()} or {@link Cursor#close()} on a
         * {@link Cursor}.
         */
        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataSetValid = false;
            notifyDataSetChanged();
        }
    };

    /** Assign handler for item selection events */
    public void setSelectionListener(ItemSelectionListener listener){
        if(listener!=null){
            mListener = listener;
        } else {
            mListener = setDefaultItemSelectionListener();
        }
    }

    // default selection handler that does nothing
    private static ItemSelectionListener setDefaultItemSelectionListener() {
        return new ItemSelectionListener() {
            @Override
            public void onItemSelected(String movieId) {Log.d(TAG,"Stub item selection handler.  movie:"+movieId);}
        };
    }

    /**
     * Define interface for item selection handling.
     */
    interface ItemSelectionListener {
        void onItemSelected(String movieId);
    }


}
