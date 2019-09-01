package com.example.movies;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.movies.database.AppDatabase;
import com.example.movies.model.Movie;
import com.example.movies.utils.Constants;
import com.example.movies.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private AppDatabase mDb;
    private int selectedItem;
    private MenuItem menuItem;
    private Movie[] movies;
    private ImageAdapter mImageAdapter;
    private Parcelable mListState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mRecyclerView = findViewById (R.id.recycler_view);

        // Using a Grid Layout Manager
        mLayoutManager = new GridLayoutManager(this, Constants.GRID_NUM_OF_COLUMNS);
        mRecyclerView.getRecycledViewPool ().clear ();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mDb = AppDatabase.getInstance (getApplicationContext ());

        if(savedInstanceState != null) {
            selectedItem = savedInstanceState.getInt("OPTION");
        }
        // Check if online
        if (isOnline ()) {
            if (selectedItem == R.id.popular_setting) {
                new FetchDataAsyncTask ().execute(Constants.POPULAR_QUERY_PARAM);
            }
            else if (selectedItem == R.id.top_rated_setting) {
                new FetchDataAsyncTask().execute(Constants.TOP_RATED_QUERY_PARAM);
            }
            else if (selectedItem == R.id.favorite_movie_setting){
                setUpViewModel (); // Favorite Movies
            }
            else{
                new FetchDataAsyncTask().execute(Constants.POPULAR_QUERY_PARAM);
            }
        } else {
            Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_TEXT, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState (outState);

        outState.putInt("OPTION", selectedItem);
        // Save list state
        mListState = mLayoutManager.onSaveInstanceState();
        outState.putParcelable("LIST_STATE_KEY", mListState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        selectedItem = outState.getInt ("OPTION");

        // Retrieve list state and list/item positions
//        if(outState != null)
            mListState = outState.getParcelable("LIST_STATE_KEY");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mListState != null) {
            mLayoutManager.onRestoreInstanceState(mListState);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater ();
        inflater.inflate(R.menu.menu_main, menu);
        switch (selectedItem){
            case R.id.popular_setting:
                menuItem = menu.findItem(R.id.popular_setting);
                menuItem.setChecked (true);
                break;

            case R.id.top_rated_setting:
                menuItem = menu.findItem(R.id.top_rated_setting);
                menuItem.setChecked (true);
                break;

            case R.id.favorite_movie_setting:
                menuItem = menu.findItem(R.id.popular_setting);
                menuItem.setChecked (true);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId ();
        if (id == R.id.popular_setting) {
            selectedItem = id;
            item.setVisible (true);
            new FetchDataAsyncTask ().execute(Constants.POPULAR_QUERY_PARAM);
            return true;
        }
        if (id == R.id.top_rated_setting) {
            selectedItem = id;
            item.setVisible (true);
            new FetchDataAsyncTask().execute(Constants.TOP_RATED_QUERY_PARAM);
            return true;
        }
        if (id == R.id.favorite_movie_setting){
            selectedItem = id;
            item.setVisible (true);
            setUpViewModel (); // Favorite Movies
            return true;
        }

        return super.onOptionsItemSelected (item);
    }

    public void setUpViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getMovies ().observe(this, new Observer<Movie[]>() {
            @Override
            public void onChanged(@Nullable Movie[] movies) {
                mImageAdapter.notifyDataSetChanged ();
                mImageAdapter.setMovies (movies);
            }
        });
    }

    public Movie[] makeMoviesDataToArray(String moviesJsonResults) throws JSONException {

        // Get results as an array
        JSONObject moviesJson = new JSONObject(moviesJsonResults);
        JSONArray resultsArray = moviesJson.getJSONArray(Constants.RESULTS_QUERY_PARAM);

        // Create array of Movie objects that stores data from the JSON string
        movies = new Movie[resultsArray.length()];

        // Go through movies one by one and get data
        for (int i = 0; i < resultsArray.length(); i++) {
            // Initialize each object before it can be used
            movies[i] = new Movie();

            // Object contains all tags we're looking for
            JSONObject movieInfo = resultsArray.getJSONObject(i);

            // Store data in movie object
            movies[i].setOriginalTitle(movieInfo.getString(Constants.ORIGINAL_TITLE_QUERY_PARAM));
            movies[i].setPosterPath(Constants.MOVIEDB_IMAGE_BASE_URL + movieInfo.getString(Constants.POSTER_PATH_QUERY_PARAM));
            movies[i].setOverview(movieInfo.getString(Constants.OVERVIEW_QUERY_PARAM));
            movies[i].setVoterAverage(movieInfo.getDouble(Constants.VOTER_AVERAGE_QUERY_PARAM));
            movies[i].setReleaseDate(movieInfo.getString(Constants.RELEASE_DATE_QUERY_PARAM));
            movies[i].setMovieId (movieInfo.getInt (Constants.MOVIE_ID_QUERY_PARAM));
        }
        return movies;
    }

    public class FetchDataAsyncTask extends AsyncTask<String, Void, Movie[]> {
        public FetchDataAsyncTask() {
            super();
        }

        @Override
        protected Movie[] doInBackground(String... params) {
            // Holds data returned from the API
            String movieSearchResults;

            try {
                URL url = JsonUtils.buildUrl(params);
                movieSearchResults = JsonUtils.getResponseFromHttpUrl(url);

                if(movieSearchResults == null) {
                    return null;
                }
                return makeMoviesDataToArray (movieSearchResults);
            } catch (IOException e) {
                return null;
            } catch (JSONException e) {
                e.printStackTrace ();
            }
            return null;
        }

        protected void onPostExecute(Movie[] movies) {
            mImageAdapter = new ImageAdapter(MainActivity.this, movies);
            mRecyclerView.setAdapter(mImageAdapter);
        }
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec(Constants.INTERNET_CHECK_COMMAND);
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        //handle click on sort settings
//
//        if (id == R.id.action_sort_settings) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            final SharedPreferences.Editor editor=sharedPreferences.edit();
//            int selected = 0;
//            sort_type = sharedPreferences.getString("sort_type", "popular");
//            if(sort_type.equals("popular"))
//                selected = 0;
//            else if(sort_type.equals("top_rated"))
//                selected = 1;
//            builder.setTitle(R.string.dialog_title);
//            builder.setSingleChoiceItems(R.array.sort_types, selected,
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            if (which == 0)
//                                editor.putString("sort_type", "popular");
//                            else if (which == 1)
//                                editor.putString("sort_type", "top_rated");
//                        }
//                    });
//            builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//                    //user clicked save
//                    editor.commit();
//                }
//            });
//            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//                    //user clicked cancel
//                }
//            });
//            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialog) {
//                    //refresh activity
//                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//                    startActivity(intent);
//                }
//            });
//            AlertDialog dialog = builder.create();
//            dialog.show();
//        }
//        return super.onOptionsItemSelected(item);
//    }
//

}
