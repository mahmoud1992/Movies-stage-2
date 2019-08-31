package com.example.movies;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.movies.database.AppDatabase;
import com.example.movies.model.Movie;

public class MovieDetailsViewModel extends ViewModel {
    private LiveData<Movie> movie;

    public MovieDetailsViewModel(AppDatabase database, int movieId) {
        movie = database.movieDao ().loadMovieById (movieId);
    }

    public LiveData<Movie> getMovie() {
        return movie;
    }
}
