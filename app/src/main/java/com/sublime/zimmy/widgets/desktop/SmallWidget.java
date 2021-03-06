package com.sublime.zimmy.widgets.desktop;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.sublime.zimmy.MusicPlayer;
import com.sublime.zimmy.MusicService;
import com.sublime.zimmy.R;
import com.sublime.zimmy.utils.NavigationUtils;

/**
 * Created by nv95 on 02.11.16.
 */

public class SmallWidget extends BaseWidget {

    @Override
    int getLayoutRes() {
        return R.layout.widget_small;
    }

    @Override
    void onViewsUpdate(Context context, RemoteViews remoteViews, ComponentName serviceName) {
        remoteViews.setOnClickPendingIntent(R.id.image_next, PendingIntent.getService(
                context,
                REQUEST_NEXT,
                new Intent(context, MusicService.class)
                        .setAction(MusicService.NEXT_ACTION)
                        .setComponent(serviceName),
                0
        ));
        remoteViews.setOnClickPendingIntent(R.id.image_playpause, PendingIntent.getService(
                context,
                REQUEST_PLAYPAUSE,
                new Intent(context, MusicService.class)
                        .setAction(MusicService.TOGGLEPAUSE_ACTION)
                        .setComponent(serviceName),
                0
        ));
        String t = MusicPlayer.getTrackName();
        if (t != null) {
            remoteViews.setTextViewText(R.id.textView_title, t);
        }
        t = MusicPlayer.getArtistName();
        if (t != null) {
            remoteViews.setTextViewText(R.id.textView_subtitle, t);
        }
        remoteViews.setImageViewResource(R.id.image_playpause,
                MusicPlayer.isPlaying() ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_white_36dp);
        long albumId = MusicPlayer.getCurrentAlbumId();
        if (albumId != -1) {
//            Bitmap artwork;
//            artwork = ImageLoader.getInstance().loadImageSync(TimberUtils.getAlbumArtUri(albumId).toString());
//            if (artwork == null) {
//                artwork = ImageLoader.getInstance().loadImageSync("drawable://" + R.drawable.ic_empty_music2);
//            }
//            remoteViews.setImageViewBitmap(R.id.imageView_cover, artwork);
        }
        remoteViews.setOnClickPendingIntent(R.id.textView_title, PendingIntent.getActivity(
                context,
                0,
                NavigationUtils.getNowPlayingIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT
        ));
    }
}
