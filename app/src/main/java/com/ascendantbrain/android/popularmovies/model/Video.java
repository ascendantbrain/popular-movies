package com.ascendantbrain.android.popularmovies.model;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.widget.Toast;

import com.ascendantbrain.android.popularmovies.provider.MoviesContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class Video {
    public final long _id;
    public final String movie_id;   // e.g. "id":297761
    public final String key;        // e.g. "key":"0WWzgGyAH6Y"
    public final String name;       // e.g. "name":"Official Comic-Con Trailer"
    public final String site;       // e.g. "site":"YouTube"
    public final String type;       // e.g. "type":"Trailer"
    public final String video_id;   // e.g. "id":"57878d7592514137c9005476"
    public static final long INVALID_ID = -1;


    private final static String YOUTUBE_MIMETYPE = "vnd.youtube:";
    private final static String YOUTUBE_VIDEO_URL = "http://www.youtube.com/watch?v=";

    /**
     * Constructs movie object from cursor.  Cursor fields must
     * match the projection defined in this class.
     */
    public static Video fromCursor(Cursor cursor){
        long _id = cursor.getLong(COL_ID);
        String movie_id = cursor.getString(COL_MOVIE_ID);
        String key = cursor.getString(COL_KEY);
        String name = cursor.getString(COL_NAME);
        String site = cursor.getString(COL_SITE);
        String type = cursor.getString(COL_TYPE);
        String video_id = cursor.getString(COL_VIDEO_ID);
        return new Video(_id,movie_id,key,name,site,type,video_id);
    }

    public static Video fromJsonObject(final String movie_id, final JSONObject videoObject){
        // JSON keys for the review object (id,key,name,site,type; ignore: size, iso_638_1, iso_3166_1)
        final String MDB_VIDEO_ID = "id";
        final String MDB_KEY = "key";
        final String MDB_NAME = "name";
        final String MDB_SITE = "site";
        final String MDB_TYPE = "type";

        Video video = null;
        try {
            final String video_id = videoObject.getString(MDB_VIDEO_ID);
            final String key = videoObject.getString(MDB_KEY);
            final String name = videoObject.getString(MDB_NAME);
            final String site = videoObject.getString(MDB_SITE);
            final String type = videoObject.getString(MDB_TYPE);
            video = new Video(INVALID_ID,movie_id,key,name,site,type,video_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return video;
    }

    Video(long _id, String movie_id, String key, String name, String site, String type, String video_id){
        this._id = _id;
        this.movie_id = movie_id;
        this.key = key;
        this.name = name;
        this.site = site;
        this.type = type;
        this.video_id = video_id;
    }

    public final static String[] MODEL_PROJECTION = {
            BaseColumns._ID,
            MoviesContract.Video.COLUMN_MOVIE_ID,
            MoviesContract.Video.COLUMN_KEY,
            MoviesContract.Video.COLUMN_NAME,
            MoviesContract.Video.COLUMN_SITE,
            MoviesContract.Video.COLUMN_TYPE,
            MoviesContract.Video.COLUMN_VIDEO_ID
    };

    public final static int COL_ID = 0;
    public final static int COL_MOVIE_ID = 1;
    public final static int COL_KEY = 2;
    public final static int COL_NAME = 3;
    public final static int COL_SITE = 4;
    public final static int COL_TYPE = 5;
    public final static int COL_VIDEO_ID = 6;


    /**
     * Launch a YouTube video using the video's key value.
     * Initially we attempt to launch the video via an implicit intent and mime type.
     * If the implicit intent fails, we fall back to the YouTube url.
     */
    public void launchVideo(Activity activity){
        if(site.equals("YouTube")){
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_MIMETYPE + key));
                activity.startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_VIDEO_URL + key));
                activity.startActivity(intent);
            }
        } else {
            // Video is hosted by an unsupported site.  Display a short error message.
            Toast.makeText(activity,"Unable to launch video",Toast.LENGTH_SHORT).show();
        }
    }

    public Uri getThumbnailUri(){
        return Uri.parse(String.format(Locale.US,"http://img.youtube.com/vi/%s/hqdefault.jpg",key));
    }

}
