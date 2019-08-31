package com.example.movies;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.movies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private static Movie[] mMovies;
    private Context mContext;

    public ImageAdapter(Context mContext, Movie[] mMovies) {
        this.mMovies = mMovies;

        if (mContext == null) {
            try{
                Thread.sleep(1000);
            } catch(InterruptedException exception){
                exception.printStackTrace();
            }
        } else {
            this.mContext = mContext;
        }
    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Create a new view
        ImageView v = (ImageView) LayoutInflater.from(parent.getContext ())
                .inflate (R.layout.image_thumb_view, parent, false);

        ViewHolder vh = new ViewHolder (v);
        return vh;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;

        public ViewHolder(ImageView v) {
            super(v);
            mImageView = v;
        }
    }
    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder viewHolder, final int position) {
        Picasso picasso = Picasso.get();
        //picasso.setLoggingEnabled(true);
        picasso.load(mMovies[position].getPosterPath())
                .fit()
                .error(R.mipmap.ic_launcher)
                .placeholder(R.mipmap.ic_launcher_round)
                .into((ImageView) viewHolder.mImageView.findViewById (R.id.image_view));

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DetailActivity.class);
                intent.putExtra("movie", mMovies[position]);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMovies == null || mMovies.length == 0 ? -1 : mMovies.length;
//        if (mMovies == null || mMovies.length == 0) {
//            return -1;
//        }
//        return mMovies.length;
    }

    public void setMovies(Movie[] movies) {
        mMovies = movies;
    }
}
