package com.example.movies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.movies.model.Movie;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private Movie[] movies;
    private TextView authorTV;
    private TextView contentsTV;
    private Context context;

    public ReviewAdapter(Movie[] movies, Context context) {
        this.context = context;
        this.movies = movies;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.movie_review, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        viewHolder.authorTV.setText(String.valueOf(movies[position].getReviewAuthor()));
        viewHolder.contentsTV.setText (String.valueOf (movies[position].getReviewContents()));

        viewHolder.reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(movies[position].getReviewUrl ()));
                context.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return movies == null || movies.length == 0 ? -1 : movies.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView authorTV;
        TextView contentsTV;
        Button reviewButton;

        public ViewHolder(ConstraintLayout itemView) {
            super (itemView);

            authorTV = itemView.findViewById (R.id.reviewAuthorTextView);
            contentsTV = itemView.findViewById (R.id.reviewContentTextView);
            reviewButton = itemView.findViewById (R.id.fullReviewButton);
        }
    }
}
