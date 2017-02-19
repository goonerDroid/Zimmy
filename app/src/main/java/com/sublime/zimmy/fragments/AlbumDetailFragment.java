/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package com.sublime.zimmy.fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.sublime.zimmy.MusicPlayer;
import com.sublime.zimmy.R;
import com.sublime.zimmy.adapters.AlbumSongsAdapter;
import com.sublime.zimmy.dataloaders.AlbumLoader;
import com.sublime.zimmy.dataloaders.AlbumSongLoader;
import com.sublime.zimmy.listeners.SimplelTransitionListener;
import com.sublime.zimmy.models.Album;
import com.sublime.zimmy.models.Song;
import com.sublime.zimmy.utils.ATEUtils;
import com.sublime.zimmy.utils.Constants;
import com.sublime.zimmy.utils.FabAnimationUtils;
import com.sublime.zimmy.utils.Helpers;
import com.sublime.zimmy.utils.NavigationUtils;
import com.sublime.zimmy.utils.PreferencesUtility;
import com.sublime.zimmy.utils.SortOrder;
import com.sublime.zimmy.utils.TimberUtils;
import com.sublime.zimmy.widgets.DividerItemDecoration;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.List;

import static java.lang.String.valueOf;

public class AlbumDetailFragment extends Fragment {

    long albumID = -1;

    private ImageView albumArt;
    private ImageView artistArt;
    private TextView albumTitle;
    private TextView albumDetails;

    private RecyclerView recyclerView;
    private AlbumSongsAdapter mAdapter;

    private Toolbar toolbar;

    private Album album;

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;
    private FloatingActionButton fab;

    private PreferencesUtility mPreferences;
    private Context mContext;
    private int primaryColor = -1;

    public static AlbumDetailFragment newInstance(long id, boolean useTransition, String transitionName) {
        AlbumDetailFragment fragment = new AlbumDetailFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.ALBUM_ID, id);
        args.putBoolean("transition", useTransition);
        if (useTransition)
            args.putString("transition_name", transitionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            albumID = getArguments().getLong(Constants.ALBUM_ID);
        }
        mContext = getActivity();
        mPreferences = PreferencesUtility.getInstance(mContext);
    }

    @TargetApi(21)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(
                R.layout.fragment_album_detail, container, false);

        albumArt = (ImageView) rootView.findViewById(R.id.album_art);
        artistArt = (ImageView) rootView.findViewById(R.id.artist_art);
        albumTitle = (TextView) rootView.findViewById(R.id.album_title);
        albumDetails = (TextView) rootView.findViewById(R.id.album_details);

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        if (getArguments().getBoolean("transition")) {
            albumArt.setTransitionName(getArguments().getString("transition_name"));
        }
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        collapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        appBarLayout = (AppBarLayout) rootView.findViewById(R.id.app_bar);
        recyclerView.setEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        album = AlbumLoader.getAlbum(getActivity(), albumID);

        setAlbumArt();

        setUpEverything();


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AlbumSongsAdapter adapter = (AlbumSongsAdapter) recyclerView.getAdapter();
                        MusicPlayer.playAll(getActivity(), adapter.getSongIds(), 0, albumID, TimberUtils.IdType.Album, true);
                        NavigationUtils.navigateToNowplaying(getActivity(), false);
                    }
                }, 150);
            }
        });

        return rootView;
    }

    private void setupToolbar() {
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout.setTitle(album.title);

    }

    private void setAlbumArt() {
        Glide.with(mContext)
                .load(TimberUtils.getAlbumArtUri(albumID).toString())
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                    @Override
                    public void onResourceReady(Bitmap loadedImage, GlideAnimation anim) {
                        if (loadedImage != null) {
                            albumArt.setImageBitmap(loadedImage);
                            new Palette.Builder(loadedImage).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    Palette.Swatch swatch = palette.getVibrantSwatch();
                                    if (swatch != null) {
                                        primaryColor = swatch.getRgb();
                                        collapsingToolbarLayout.setContentScrimColor(primaryColor);
                                        if (getActivity() != null)
                                            ATEUtils.setStatusBarColor(getActivity(), Helpers.getATEKey(getActivity()), primaryColor);
                                    } else {
                                        Palette.Swatch swatchMuted = palette.getMutedSwatch();
                                        if (swatchMuted != null) {
                                            primaryColor = swatchMuted.getRgb();
                                            collapsingToolbarLayout.setContentScrimColor(primaryColor);
                                            if (getActivity() != null)
                                                ATEUtils.setStatusBarColor(getActivity(), Helpers.getATEKey(getActivity()), primaryColor);
                                        }
                                    }

                                    MaterialDrawableBuilder builder = MaterialDrawableBuilder.with(getActivity())
                                            .setIcon(MaterialDrawableBuilder.IconValue.SHUFFLE)
                                            .setSizeDp(30);
                                    if (primaryColor != -1) {
                                        builder.setColor(TimberUtils.getBlackWhiteColor(primaryColor));
                                        ATEUtils.setFabBackgroundTint(fab, primaryColor);
                                        fab.setImageDrawable(builder.build());
                                    } else {
                                        if (mContext != null) {
                                            ATEUtils.setFabBackgroundTint(fab, Config.accentColor(mContext, Helpers.getATEKey(mContext)));
                                            builder.setColor(TimberUtils.getBlackWhiteColor(Config.accentColor(mContext, Helpers.getATEKey(mContext))));
                                            fab.setImageDrawable(builder.build());
                                        }
                                    }
                                }
                            });
                        }else{
                            MaterialDrawableBuilder builder = MaterialDrawableBuilder.with(mContext)
                                    .setIcon(MaterialDrawableBuilder.IconValue.SHUFFLE)
                                    .setColor(TimberUtils.getBlackWhiteColor(Config.accentColor(mContext, Helpers.getATEKey(mContext))));
                            ATEUtils.setFabBackgroundTint(fab, Config.accentColor(mContext, Helpers.getATEKey(mContext)));
                            fab.setImageDrawable(builder.build());
                        }
                    }
                });
    }

    private void setAlbumDetails() {
        String songCount = TimberUtils.makeLabel(getActivity(), R.plurals.Nsongs, album.songCount);
        String year = (album.year != 0) ? (" - " + valueOf(album.year)) : "";
        albumTitle.setText(album.title);
        albumDetails.setText(album.artistName + " - " + songCount + year);


    }

    private void setUpAlbumSongs() {
        List<Song> songList = AlbumSongLoader.getSongsForAlbum(getActivity(), albumID);
        mAdapter = new AlbumSongsAdapter(getActivity(), songList, albumID);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setAdapter(mAdapter);
    }

    private void setUpEverything() {
        setupToolbar();
        setAlbumDetails();
        setUpAlbumSongs();
    }

    private void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                List<Song> songList = AlbumSongLoader.getSongsForAlbum(getActivity(), albumID);
                mAdapter.updateDataSet(songList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.album_song_sort_by, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_by_az:
                mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_A_Z);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_za:
                mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_Z_A);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_year:
                mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_YEAR);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_duration:
                mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_DURATION);
                reloadAdapter();
                return true;
            default://Sort by track number
                mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST);
                reloadAdapter();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        if (primaryColor != -1 && getActivity() != null) {
            collapsingToolbarLayout.setContentScrimColor(primaryColor);
            ATEUtils.setFabBackgroundTint(fab, primaryColor);
            String ateKey = Helpers.getATEKey(getActivity());
            ATEUtils.setStatusBarColor(getActivity(), ateKey, primaryColor);
        }

    }

    private class EnterTransitionListener extends SimplelTransitionListener {

        @TargetApi(21)
        public void onTransitionEnd(Transition paramTransition) {
            FabAnimationUtils.scaleIn(fab);
        }

        public void onTransitionStart(Transition paramTransition) {
            FabAnimationUtils.scaleOut(fab, 0, null);
        }

    }

}
