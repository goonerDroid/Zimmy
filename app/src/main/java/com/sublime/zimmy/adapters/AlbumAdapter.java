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

package com.sublime.zimmy.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.sublime.zimmy.R;
import com.sublime.zimmy.models.Album;
import com.sublime.zimmy.utils.Helpers;
import com.sublime.zimmy.utils.NavigationUtils;
import com.sublime.zimmy.utils.PreferencesUtility;
import com.sublime.zimmy.utils.TimberUtils;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ItemHolder> {

    private List<Album> arraylist;
    private Activity mContext;
    private boolean isGrid;

    public AlbumAdapter(Activity context, List<Album> arraylist) {
        this.arraylist = arraylist;
        this.mContext = context;
        this.isGrid = PreferencesUtility.getInstance(mContext).isAlbumsInGrid();

    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v;
        if (isGrid) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_album_grid, viewGroup,false);
        } else {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_album_list, viewGroup,false);
        }
        return new ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemHolder itemHolder, final int i) {
        Album localItem = arraylist.get(i);
        itemHolder.title.setText(localItem.title);
        itemHolder.artist.setText(localItem.artistName);
        Glide.with(mContext)
                .load(TimberUtils.getAlbumArtUri(localItem.id).toString())
                .crossFade()
                .into(itemHolder.albumArt);
        setBackgroundColor(localItem,itemHolder);
        if (TimberUtils.isLollipop()) {
            itemHolder.albumArt.setTransitionName("transition_album_art" + i);
        }
    }

    private void setBackgroundColor(Album localItem, final ItemHolder itemHolder){
        Glide.with(mContext)
                .load(TimberUtils.getAlbumArtUri(localItem.id).toString())
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                    @Override
                    public void onResourceReady(Bitmap loadedImage, GlideAnimation anim) {
                        if (loadedImage != null) {
                            itemHolder.albumArt.setImageBitmap(loadedImage);
                            if (isGrid) {
                                new Palette.Builder(loadedImage).generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(Palette palette) {
                                        Palette.Swatch swatch = palette.getVibrantSwatch();
                                        if (swatch != null) {
                                            int color = swatch.getRgb();
                                            itemHolder.footer.setBackgroundColor(color);
                                            int textColor = TimberUtils.getBlackWhiteColor(swatch.getTitleTextColor());
                                            itemHolder.title.setTextColor(textColor);
                                            itemHolder.artist.setTextColor(textColor);
                                        } else {
                                            Palette.Swatch mutedSwatch = palette.getMutedSwatch();
                                            if (mutedSwatch != null) {
                                                int color = mutedSwatch.getRgb();
                                                itemHolder.footer.setBackgroundColor(color);
                                                int textColor = TimberUtils.getBlackWhiteColor(mutedSwatch.getTitleTextColor());
                                                itemHolder.title.setTextColor(textColor);
                                                itemHolder.artist.setTextColor(textColor);
                                            }
                                        }


                                    }
                                });

                            }
                        }else{
                            if (isGrid) {
                                itemHolder.footer.setBackgroundColor(0);
                                if (mContext != null) {
                                    int textColorPrimary = Config.textColorPrimary(mContext, Helpers.getATEKey(mContext));
                                    itemHolder.title.setTextColor(textColorPrimary);
                                    itemHolder.artist.setTextColor(textColorPrimary);
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return null != arraylist ? arraylist.size() : 0;
    }

    public void updateDataSet(List<Album> arraylist) {
        this.arraylist = arraylist;
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title;
        protected TextView artist;
        protected ImageView albumArt;
        View footer;

        public ItemHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.album_title);
            this.artist = (TextView) view.findViewById(R.id.album_artist);
            this.albumArt = (ImageView) view.findViewById(R.id.album_art);
            this.footer = view.findViewById(R.id.footer);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            NavigationUtils.navigateToAlbum(mContext, arraylist.get(getAdapterPosition()).id,
                    new Pair<View, String>(albumArt, "transition_album_art" + getAdapterPosition()));
        }

    }


}



