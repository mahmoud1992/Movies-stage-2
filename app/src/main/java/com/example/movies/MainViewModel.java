package com.example.movies;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.example.movies.database.AppDatabase;
import com.example.movies.model.Movie;

public class MainViewModel extends AndroidViewModel{

    private LiveData<Movie[]> movies;

    public MainViewModel(@NonNull Application application) {
        super (application);
        AppDatabase database = AppDatabase.getInstance (this.getApplication ());
        movies = database.movieDao ().loadAllMovies ();
    }

    public LiveData<Movie[]> getMovies() {
        return movies;
    }
}
