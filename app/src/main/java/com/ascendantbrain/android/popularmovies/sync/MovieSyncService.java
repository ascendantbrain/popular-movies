package com.ascendantbrain.android.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/*
 * Service allowing the sync adapter framework to call onPerformSync().
 * Code is sourced from Google's boilerplate reference material found here:
 * https://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 */
public class MovieSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static MovieSyncAdapter sMovieSyncAdapter = null;

    /**
     * Instantiate the sync adapter object.
     */
    @Override
    public void onCreate() {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized (sSyncAdapterLock) {
            if (sMovieSyncAdapter == null) {
                sMovieSyncAdapter = new MovieSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    /**
     * Return an object that allows the system to invoke the sync adapter.
     */
    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return sMovieSyncAdapter.getSyncAdapterBinder();
    }
}
