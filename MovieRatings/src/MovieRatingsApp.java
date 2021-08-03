import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * Main Class for the Movie Ratings JavaFX Application
 * @author Jenny Krewer, Mohammad Islam, Ryan Keil
 */
public class MovieRatingsApp extends Application {

    String searches[] = {"Movie", "Actor", "Director"}; // options to search the database
    Connection conn; // the connection to the mySQL database
    boolean loggedIn = false; // boolean to keep track of whether or not someone is logged in
    String loggedInName = ""; // username for the logged in user
    int loggedInId = 0; // user ID for the logged in user
    static Image STAR; // image for filled in stars used for ratings
    static Image EMPTY_STAR; // image for empty in stars used for ratings

    TableView<Movie> movieTable = new TableView<Movie>(); // view to display movies found in the database
    ObservableList<Movie> movieData = FXCollections.observableArrayList(); // list of movies to display

    /**
     * Main method. Calls start to run the application
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Gets the connection to the mySQL server
     * @return Connect to the server
     * @throws SQLException if unable to connect
     */
    public Connection getConnection() throws SQLException {
        Connection con = null;
        Properties connectionProps = new Properties();

        // define some database properties
        connectionProps.put("user", "root");
        connectionProps.put("password", "12345");

        // specify database
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/movie_ratings",
            connectionProps);

        return con;
    }

    /**
     * Returns a random list of movies to display when the application is first launched.
     * @return ObservableList of movies to display
     */
    private ObservableList<Movie> getRandMovies() {
        ObservableList<Movie> listMovies = FXCollections.observableArrayList();
        Random rand = new Random();
        String query;
        String dirQuery;
        String actQuery;
        int movieId;

        while (listMovies.size() < 20) {
            movieId = rand.nextInt(70000 - 1) + 1;

            query = "SELECT * FROM movies WHERE mov_id = " + movieId;

            try (Statement stmt = conn.createStatement()) {
                // execute query
                ResultSet rs = stmt.executeQuery(query);
                int mId;
                String mName;
                int year;
                double critRate;
                double audRate;
                int audCount;
                String dirId;
                String director;
                List<String> actors = new ArrayList<String>();


                if (rs.next()) {
                    director = "";

                    mId = rs.getInt("MOV_ID");
                    mName = rs.getString("TITLE");
                    year = rs.getInt("YEAR");
                    critRate = rs.getDouble("CRITIC_RATE");
                    audRate = rs.getDouble("AUD_RATE");
                    audCount = rs.getInt("AUD_COUNT");
                    dirId = rs.getString("DIR_ID");

                    if (dirId.compareTo("") != 0) {
                        dirQuery = "SELECT dir_name FROM directors WHERE dir_id = '" + dirId + "'";
                        ResultSet rs2 = stmt.executeQuery(dirQuery);
                        if (rs2.next()) {
                            director = rs2.getString("DIR_NAME");
                        }
                    }
                    if (mId > 0) {
                        actQuery =
                            "SELECT act_name FROM movie_actors m, actors a WHERE m.act_id = a.act_id and mov_id = "
                                + mId;
                        ResultSet rs3 = stmt.executeQuery(actQuery);
                        while (rs3.next()) {
                            actors.add(rs3.getString("ACT_NAME"));
                        }
                    }

                    listMovies.add(
                        new Movie(mId, mName, year, critRate, audRate, audCount, director, actors));
                }
            } catch (SQLException e) {
                System.out.println(e);
            }

        }


        return listMovies;
    }

    /**
     * Updates a VBox with information about the selected movie to display in the report. Returns the
     * VBox for ratings to use to update the ratings display if it is changed by the user
     * @param movie The movie to display the report for
     * @param reportBox The VBox for the Report
     * @return the VBox where critic and audience ratings information is displayed
     */
    private VBox getReport(Movie movie, VBox reportBox) {

        reportBox.getChildren().clear(); // clear the report to reload
        reportBox.setPadding(new Insets(10, 20, 10, 20));
        reportBox.setPrefWidth(1200);

        // display message if no movie is selected
        if (movie == null) {
            reportBox.getChildren().add(new Label("Select a movie to view information"));
            return reportBox;
        }

        // add movie title and director to top of report
        HBox title = new HBox();
        title.setAlignment(Pos.BOTTOM_LEFT);
        title.setPadding(new Insets(0, 0, 5, 0));

        Label movieTitle = new Label(movie.getMovieName() + " (" + movie.getMovieYear() + ")");
        movieTitle.setFont(new Font(25));
        movieTitle.setPadding(new Insets(0, 10, 10, 5));

        title.getChildren().add(movieTitle);
        title.getChildren().add(new Label(" Directed by " + movie.getMovieDir()));

        reportBox.getChildren().add(title);

        // add HBox for ratings, actors, genres, and locations
        HBox paneBox = new HBox();
        paneBox.setPrefWidth(1200);

        VBox ratingsPane = getRatingsPane(paneBox, movie); // column for ratings
        getActorsPane(paneBox, movie); // column for actors)
        getGenresPane(paneBox, movie); // column for genres
        getLocationsPane(paneBox, movie); // column for location data

        reportBox.getChildren().add(paneBox); // add HBox with 4 columns

        // add user rating if someone is logged in
        if (loggedIn) {
            displayUserRating(reportBox, movie, ratingsPane);
        }


        return reportBox;
    }

    /**
     * Creates, populates, and returns a VBox to display ratings information in the report
     * @param paneBox The HBox for the Report
     * @param movie The movie to display the report for
     * @return The VBox for the ratings
     */
    VBox getRatingsPane(HBox paneBox, Movie movie) {

        // create VBox for ratings column
        VBox ratingsVBox = new VBox();
        ratingsVBox.setPrefWidth(400);
        ratingsVBox.setAlignment(Pos.CENTER);

        // add labels and images for critic and audience ratings
        displayRating(movie.getMovieCriticRating(), 10, ratingsVBox, "Critic Rating");
        displayRating(movie.getMovieAudRating(), 5, ratingsVBox, "Audience Rating");
        
        // add count of audience ratings to the end
        Label audCountLabel = new Label(movie.getMovieAudCount() + " audience ratings");
        ratingsVBox.getChildren().add(audCountLabel);

        paneBox.getChildren().add(ratingsVBox); // add the ratings column to the report

        return ratingsVBox; // return the ratings column so we can use it when a user updates their rating
    }

    /**
     * Adds a rating (audience or critic) to the ratings VBox, including a label and images for the stars
     * @param rating The number of stars to display
     * @param total The total number of stars the rating is out of
     * @param ratingsVBox The ratings VBox to update with the information
     * @param label The label to use for the rating. The label will be appended with " (<rating>/<total>)"
     */
    private void displayRating(double rating, double total, VBox ratingsVBox, String label) {

        Label ratingLabel = new Label(label + " (" + rating + "/" + total + ")"); // label to display rating title and number
        ratingLabel.setFont(new Font(20));
        ratingLabel.setMaxWidth(400);
        ratingLabel.setAlignment(Pos.BASELINE_CENTER);
        ratingLabel.setPadding(new Insets(5));

        HBox ratingHBox = new HBox(); // add stars image
        ratingHBox.setPrefWidth(400);
        ratingHBox.setPadding(new Insets(0, 0, 10, 0));

        rating = Math.round(rating); // round the rating to use to display stars

        int i = 0;

        // add filled in stars
        while (i < rating) {
            ratingHBox.getChildren().add(getStarImage(total));
            i++;
        }

        // add empty stars
        while (i < total) {
            ratingHBox.getChildren().add(getEmptyStarImage(total));
            i++;
        }

        // at the the ratings column
        ratingsVBox.getChildren().addAll(ratingLabel, ratingHBox);


        return;
    }

    /**
     * Adds the logged in users rating to the report
     * @param reportBox The VBox for the report
     * @param movie The movie to display the rating for
     * @param ratingsPane The VBox where the audience and critic ratings are displayed
     */
    private void displayUserRating(VBox reportBox, Movie movie, VBox ratingsPane) {

        int[] timestamp = {0, 0, 0, 0, 0, 0};
        int rating = getUserRating(movie, timestamp); // get the user's current rating for the movie

        // display the rating in a label
        Label ratingLabel = new Label("My Rating (" + rating + "/5)");
        ratingLabel.setFont(new Font(25));
        ratingLabel.setMaxWidth(400);
        ratingLabel.setAlignment(Pos.CENTER);
        ratingLabel.setPadding(new Insets(5));

        // create HBox for stars for user's rating
        HBox ratingHBox = new HBox();
        ratingHBox.setPrefWidth(1200);
        ratingHBox.setPadding(new Insets(0, 0, 10, 0));
        ratingHBox.setAlignment(Pos.CENTER);

        ratingHBox.getChildren().add(ratingLabel);
        
        // display stars
        int i = 0;

        while (i < rating) {

            ImageView starImage = getStarImage(5);
            final int newRating = i + 1;

            starImage.setPickOnBounds(true);
            
            // clicking star changes the rating to that number of stars
            starImage.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event) {

                    updateUserRating(newRating, ratingHBox, movie, ratingsPane);
                                                                   
                    event.consume();

                }
            });

            ratingHBox.getChildren().add(starImage); // add stars to HBox
            i++;
        }

        // add empty stars
        while (i < 5) {

            ImageView emptyStarImage = getEmptyStarImage(5);
            final int newRating = i + 1;

            emptyStarImage.setPickOnBounds(true);

            // clicking an empty star increases the rating to that number of stars
            emptyStarImage.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {

                        updateUserRating(newRating, ratingHBox, movie, ratingsPane);
                        event.consume();

                    }
                });
            
            ratingHBox.getChildren().add(emptyStarImage); // add stars to Hbox
            
            i++;
        }

        // if there's a rating, display the time the user left it
        if (rating > 0) {
            String timeLabelStr = timestamp[1] + "/" + timestamp[0] + "/" + timestamp[2] + " at " + timestamp[3] + ":" + timestamp[4];
            Label timeLabel = new Label(timeLabelStr);
            ratingHBox.getChildren().add(timeLabel);
        }
        
        // otherwise, add a blank label so we have it to update when they do leave a rating
        else {
            ratingHBox.getChildren().add(new Label());
        }
        
        
        reportBox.getChildren().addAll(ratingHBox);


        return;
    }

    /**
     * Updates a users rating when they change it for a movie in the report. Updates, the movie object and
     * the user_ratings and movies tables in the database. The movies table is updated with the new average
     * rating including the new rating from the user.
     * @param newRating The rating the user chose that needs to be added or updated.
     * @param userRatingHBox The HBox where the user rating is displayed
     * @param movie the movie to update the rating for
     * @param ratingsPane VBox where the critic and audience ratings are displayed
     */
    private void updateUserRating(int newRating, HBox userRatingHBox, Movie movie, VBox ratingsPane) {

        int mId = movie.getMovieID();

        // get current time to save time stamp of the rating
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        int hour = now.getHour();
        int minute = now.getMinute();
        int second = now.getSecond();

        float oldUserRating = 0; // default to this being the first rating the user has left for the movie

        // update database
        try (Statement stmt = conn.createStatement()) {

            String query = "SELECT Rating FROM user_ratings WHERE User_id = " + loggedInId
                + " and mov_id = " + mId;

            ResultSet result = stmt.executeQuery(query);
            
            boolean changed = false;

            if (result.next()) {
                
                oldUserRating = result.getFloat("Rating");

                String updateQuery =
                    "update user_ratings set Rating = ?, Time_day = ?, Time_month = ?, Time_year = ?, Time_hour = ?, Time_min = ?, Time_sec = ? WHERE user_ID = ? and mov_id = ?";


                PreparedStatement preparedStmt = conn.prepareStatement(updateQuery);
                preparedStmt.setFloat(1, newRating);
                preparedStmt.setInt(2, day);
                preparedStmt.setInt(3, month);
                preparedStmt.setInt(4, year);
                preparedStmt.setInt(5, hour);
                preparedStmt.setInt(6, minute);
                preparedStmt.setInt(7, second);
                preparedStmt.setInt(8, loggedInId);
                preparedStmt.setInt(9, mId);
                preparedStmt.execute();

            }

            else {

                String updateQuery =
                    " insert into user_ratings (user_id, mov_id, Rating, Time_day, Time_month, Time_year, Time_hour, Time_min, Time_sec)"
                        + " values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement preparedStmt;
                preparedStmt = conn.prepareStatement(updateQuery);
                preparedStmt.setInt(1, loggedInId);
                preparedStmt.setInt(2, mId);
                preparedStmt.setFloat(3, newRating);
                preparedStmt.setInt(4, day);
                preparedStmt.setInt(5, month);
                preparedStmt.setInt(6, year);
                preparedStmt.setInt(7, hour);
                preparedStmt.setInt(8, minute);
                preparedStmt.setInt(9, second);
                preparedStmt.execute();


            }

            // update movie's audience rating
            changed = movie.updateAudRatingandCount(newRating, oldUserRating);

            // update movie table with new audience rating
            String movUpdateQuery =
                "update movies set Aud_rate = ?, Aud_count = ? WHERE mov_ID = ?";

            PreparedStatement movPreparedStmt = conn.prepareStatement(movUpdateQuery);
            movPreparedStmt.setDouble(1, movie.getMovieAudRating());
            movPreparedStmt.setInt(2, movie.getMovieAudCount());
            movPreparedStmt.setInt(3, movie.getMovieID());

            movPreparedStmt.execute();


            // update user rating display after a user rates a movie
            Label userRateLabel = (Label) userRatingHBox.getChildren().get(0); // user rating label
            userRateLabel.setText( "My Rating (" + newRating + "/5)");
            
            for (int i = 1; i <= 5; i++) {

                ImageView image = (ImageView) userRatingHBox.getChildren().get(i);

                if (i <= newRating) {

                    image.setImage(STAR);
                }

                else {

                    image.setImage(EMPTY_STAR);
                }
                
            }
            
            // If this is the first time the user rated the movie, the user count was updated so update the display
            if (oldUserRating == 0) {              
                
                Label audCountLabel = (Label) ratingsPane.getChildren().get(4); // label for audience rating count
                    audCountLabel.setText(movie.getMovieAudCount() + " audience ratings");
            }
            
            // update the timestamp displayed
            Label ratingTime = (Label) userRatingHBox.getChildren().get(6);
            ratingTime.setText(month + "/" + day + "/" + year + " at " + hour + ":" + minute);
            
            // update audience rating display if the average rating changed
            if (changed) {
                
                Label audRateLabel = (Label) ratingsPane.getChildren().get(2); // label for avg audience rating
                audRateLabel.setText( "Audience Rating (" + movie.getMovieAudRating() + "/5.0)");
                
                HBox audRateHBox = (HBox) ratingsPane.getChildren().get(3); // HBox with stars
                
                for (int i = 0; i < 5; i++) {

                    ImageView image = (ImageView) audRateHBox.getChildren().get(i); // get the current star

                    if (i < movie.getMovieAudRating()) {

                        image.setImage(STAR); // add star up to the rating value
                    }

                    else {

                        image.setImage(EMPTY_STAR); // add empty stars at the end
                    }
                    
                }
            }

        } catch (SQLException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }


    }

    /**
     * Creates and returns an image view for a star to display a rating
     * @param total the total number of stars that will be displayed in the rating. Used to determine
     * the size of the star ImageView
     * @return The ImageView for the star
     */
    private ImageView getStarImage(double total) {

        ImageView starImage = null;

        try {
            starImage = new ImageView(STAR);
            starImage.setFitWidth(400 / total); // size stars based on how many need to display
            starImage.setPreserveRatio(true);

        } catch (NullPointerException e) {
            Alert missingImageAlert =
                new Alert(AlertType.ERROR, "An Image Used by the Application is Missing");
            missingImageAlert.setHeaderText("Image Not Found");
            missingImageAlert.show();

        }

        return starImage;

    }

    /**
     * Creates and returns an image view for an unfilled star to display a rating
     * @param total the total number of stars that will be displayed in the rating. Used to determine
     * the size of the star ImageView
     * @return The ImageView for the unfilled star
     */
    private ImageView getEmptyStarImage(double total) {

        ImageView starImage = null;

        try {
            starImage = new ImageView(EMPTY_STAR);
            starImage.setFitWidth(400 / total); // size image based on the number that need to display
            starImage.setPreserveRatio(true);


        } catch (NullPointerException e) {
            Alert missingImageAlert =
                new Alert(AlertType.ERROR, "An Image Used by the Application is Missing");
            missingImageAlert.setHeaderText("Image Not Found");
            missingImageAlert.show();

        }

        return starImage;

    }

    /**
     * Gets the logged in user's rating for a movie from the mySQL database
     * @param movie the movie to get the rating for
     * @param timestamp array of date information from when the rating was placed - day, month, year, hour, minute, second
     * @return the rating the user left for the move. 0 If they haven't left a rating for the movie.
     */
    private int getUserRating(Movie movie, int[] timestamp) {

        int mId = movie.getMovieID();
        int rating = 0; // default rating to 0 stars

        // get user rating from database
        try (Statement stmt = conn.createStatement()) {

            String query = "SELECT Rating, Time_day, Time_month, Time_year, Time_hour, Time_min, Time_sec FROM user_ratings WHERE User_id = " + loggedInId
                + " and mov_id = " + mId;

            ResultSet result = stmt.executeQuery(query);

            while (result.next()) {

                rating = result.getInt("Rating");
                timestamp[0] = result.getInt("Time_day");
                timestamp[1] = result.getInt("Time_month");
                timestamp[2] = result.getInt("Time_year");
                timestamp[3] = result.getInt("Time_hour");
                timestamp[4] = result.getInt("Time_min");
                timestamp[5] = result.getInt("Time_sec");
                

            }

            
        } catch (SQLException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }

        return rating;
    }

    /**
     * Creates and adds a VBox for the genres for the movie to the report. Includes links for each
     * genre used to search the database for other movies with that genre.
     * @param paneBox The HBox for the report the Genres VBox will be added to
     * @param movie the movie to populate genres for.
     */
    private void getGenresPane(HBox paneBox, Movie movie) {

        ScrollPane genresPane = new ScrollPane();
        genresPane.setPrefHeight(250);
        genresPane.setMaxWidth(200);

        VBox genresVBox = new VBox();

        // search database and add genres to display
        int mId = movie.getMovieID();

        try (Statement stmt = conn.createStatement()) {

            String query =
                "SELECT Genre FROM movie_genres WHERE mov_id = "
                    + mId;

            ResultSet result = stmt.executeQuery(query);
            
            int genreCount = 0;

            while (result.next()) {
                
                genreCount++;

                String genre = result.getString("Genre");
                Hyperlink genreLink = new Hyperlink(genre);

                genreLink.setOnAction(e -> {

                    ObservableList<Movie> listMovies = FXCollections.observableArrayList();
                    getMoviesByGenre(genre, listMovies);
                    movieTable.setItems(listMovies);

                });

                genresVBox.getChildren().add(genreLink);


            }
            
            if (genreCount == 0) {
                
                Label genreLabel = new Label("No genres listed");
                genreLabel.setPadding(new Insets(5));
                genresVBox.getChildren().add(genreLabel);
                
            }

        } catch (SQLException e) {
            System.out.println(e);
        }

        genresPane.setContent(genresVBox);

        Label genresLabel = new Label("Genres");
        genresLabel.setFont(new Font(15));
        genresLabel.setMaxWidth(200);
        genresLabel.setAlignment(Pos.BASELINE_CENTER);
        genresLabel.setPadding(new Insets(5));

        VBox genreVBox = new VBox();
        genreVBox.getChildren().addAll(genresLabel, genresPane);
        genreVBox.setPrefWidth(200);

        paneBox.getChildren().add(genreVBox);

        return;
    }
    
    /** 
     * Creates and adds a VBox for the actors in the movie to the report. Includes links for each
     * actor used to search the database for other movies the actor is in.
     * @param paneBox The HBox for the report the Actors VBox will be added to
     * @param movie the movie to populate actors from.
     */
    private void getActorsPane(HBox paneBox, Movie movie) {

        ScrollPane actorsPane = new ScrollPane();
        actorsPane.setPrefHeight(250);
        actorsPane.setMaxWidth(200);

        VBox actorsVBox = new VBox();

        // add actors to display
        for (String actor : movie.actors) {

            Hyperlink actorLink = new Hyperlink(actor);

            actorLink.setOnAction(e -> {

                ObservableList<Movie> listMovies = FXCollections.observableArrayList();
                getMoviesByActor(actor, listMovies, false);
                movieTable.setItems(listMovies);

            });

            actorsVBox.getChildren().add(actorLink);
        }

        actorsPane.setContent(actorsVBox);

        Label actorsLabel = new Label("Actors");
        actorsLabel.setFont(new Font(15));
        actorsLabel.setMaxWidth(200);
        actorsLabel.setAlignment(Pos.BASELINE_CENTER);
        actorsLabel.setPadding(new Insets(5));

        VBox actorVBox = new VBox();
        actorVBox.getChildren().addAll(actorsLabel, actorsPane);
        actorVBox.setPrefWidth(200);

        paneBox.getChildren().add(actorVBox);

        return;
    }

    /**
     * Creates and adds a VBox for the locations the movie was filmed at to the report.
     * @param paneBox The HBox for the report the locations VBox will be added to
     * @param movie the movie to populate the locations for.
     */
    void getLocationsPane(HBox paneBox, Movie movie) {

        ScrollPane LocationsPane = new ScrollPane();
        LocationsPane.setPrefHeight(250);
        LocationsPane.setMaxWidth(400);

        VBox locationsVBox = new VBox();

        int mId = movie.getMovieID();

        try (Statement stmt = conn.createStatement()) {

            String query =
                "SELECT Loc_data1, Loc_data2, Loc_data3, Loc_data4 FROM movie_locations m, locations l WHERE m.loc_id = l.loc_id and mov_id = "
                    + mId;

            ResultSet result = stmt.executeQuery(query);
            
            int locCount = 0;

            while (result.next()) {
                String text = "";

                for (int i = 4; i > 0; i--) {

                    String locData = result.getString("LOC_DATA" + i);

                    if (locData.length() > 0) {

                        if (text != "") {
                            text = text + ", ";
                        }

                        text = text + locData;
                    }

                }

                if (text != "") {
                    locCount++;
                    Label locationLabel = new Label(text);
                    locationLabel.setPadding(new Insets(5));

                    locationsVBox.getChildren().add(locationLabel);
                }

            }
            
            if (locCount == 0) {
                
                Label locationLabel = new Label("No filming location data available");
                locationLabel.setPadding(new Insets(5));
                locationsVBox.getChildren().add(locationLabel);
                
            }

        } catch (SQLException e) {
            System.out.println(e);
        }

        LocationsPane.setContent(locationsVBox);

        Label locationsLabel = new Label("Filming Locations");
        locationsLabel.setFont(new Font(15));
        locationsLabel.setMaxWidth(400);
        locationsLabel.setAlignment(Pos.BASELINE_CENTER);
        locationsLabel.setPadding(new Insets(5));

        VBox locationPaneVBox = new VBox();
        locationPaneVBox.getChildren().addAll(locationsLabel, LocationsPane);
        locationPaneVBox.setPrefWidth(400);

        paneBox.getChildren().add(locationPaneVBox);
    }

    /**
     * Searches the database for the movies an actor was in. Used when a user clicks a link for 
     * an actor in the report, or when a user searches for an actor with the search bar.
     * @param searchFieldText The string to search for based on the actor's name
     * @param listMovies The ObservableList to add the movies to that will be used to display the results.
     * @param includeSimilar set to one to include actors with a name like the search term, false for exact matches only
     */
    private void getMoviesByActor(String searchFieldText, ObservableList<Movie> listMovies, boolean includeSimilar) {

        String query = "";
        
        if (includeSimilar) {
            query = "{CALL GetByActor(?)}";
        }
        else {
            query = "{CALL GetByExactActor(?)}";
        }

        try {
            CallableStatement stmt = conn.prepareCall(query);
            stmt.setString(1, searchFieldText);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int mId = rs.getInt("MOV_ID");
                String mName = rs.getString("TITLE");
                int year = rs.getInt("YEAR");
                double critRate = rs.getDouble("CRITIC_RATE");
                double audRate = rs.getDouble("AUD_RATE");
                int audCount = rs.getInt("AUD_COUNT");
                String director = rs.getString("DIR_NAME");

                List<String> actors = new ArrayList<String>();

                if (mId > 0) {
                    String actQuery =
                        "SELECT act_name FROM movie_actors m, actors a WHERE m.act_id = a.act_id and mov_id = "
                            + mId + " Order by Ranking DESC";
                    CallableStatement stmt2 = conn.prepareCall(actQuery);
                    ResultSet rs2 = stmt2.executeQuery(actQuery);
                    while (rs2.next()) {
                        actors.add(rs2.getString("ACT_NAME"));
                    }
                }

                listMovies.add(
                    new Movie(mId, mName, year, critRate, audRate, audCount, director, actors));

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Searches the database for movies with a given genre. Used when a user clicks a link for 
     * a genre in the report.
     * @param genre The title of the genre
     * @param listMovies The ObservableList to add the movies to that will be used to display the results.
     */
    private void getMoviesByGenre(String genre, ObservableList<Movie> listMovies) {

        String query = "{CALL GetByGenre(?)}";

        try {
            CallableStatement stmt = conn.prepareCall(query);
            stmt.setString(1, genre);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int mId = rs.getInt("MOV_ID");
                String mName = rs.getString("TITLE");
                int year = rs.getInt("YEAR");
                double critRate = rs.getDouble("CRITIC_RATE");
                double audRate = rs.getDouble("AUD_RATE");
                int audCount = rs.getInt("AUD_COUNT");
                String director = rs.getString("DIR_NAME");

                List<String> actors = new ArrayList<String>();

                if (mId > 0) {
                    String actQuery =
                        "SELECT act_name FROM movie_actors m, actors a WHERE m.act_id = a.act_id and mov_id = "
                            + mId + " Order by Ranking DESC";
                    CallableStatement stmt2 = conn.prepareCall(actQuery);
                    ResultSet rs2 = stmt2.executeQuery(actQuery);
                    while (rs2.next()) {
                        actors.add(rs2.getString("ACT_NAME"));
                    }
                }

                listMovies.add(
                    new Movie(mId, mName, year, critRate, audRate, audCount, director, actors));

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
     /**
     * Starts the JavaFx application.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        conn = getConnection();
        primaryStage.setTitle("MovieRatings");

        try {
            STAR = new Image(getClass().getResourceAsStream("star.png"));
            EMPTY_STAR = new Image(getClass().getResourceAsStream("empty_star.png"));

        } catch (NullPointerException e) {
            Alert missingImageAlert =
                new Alert(AlertType.ERROR, "An Image Used by the Application is Missing");
            missingImageAlert.setHeaderText("Image Not Found");
            missingImageAlert.show();

        }

        // Parent Vbox
        VBox parentVbox = new VBox();

        // Top bar
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10, 10, 10, 10));

        Pane spacer = new Pane();
        topBar.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinSize(10, 1);


        // Menu bar
        MenuBar menuBar = new MenuBar();

        Menu mainMenu = new Menu("Menu");

        MenuItem profile = new MenuItem("Edit Profile");
        profile.setDisable(true);

        MenuItem logout = new MenuItem("Logout");
        logout.setDisable(true);

        MenuItem exit = new MenuItem("Exit");


        mainMenu.getItems().addAll(profile, logout, exit);

        menuBar.getMenus().add(mainMenu);

        // Login bar
        // Welcome user banner
        Label userLabel = new Label("");

        // Username textfield
        TextField userTextField = new TextField();
        userTextField.setPromptText("Username");

        // Password textfield
        PasswordField userPwField = new PasswordField();
        userPwField.setPromptText("Password");

        // Login button
        Button loginBtn = new Button();
        loginBtn.setText("Login");

        // Create user button
        Button createBtn = new Button();
        createBtn.setText("Create User");

        topBar.getChildren().addAll(menuBar, spacer, userTextField, userPwField, loginBtn,
            createBtn);

        // Search bar
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER);
        searchBar.setPadding(new Insets(0, 10, 10, 10));

        TextField searchField = new TextField();
        searchField.setPromptText("Search");
        searchField.setPrefWidth(700);

        ComboBox<String> searchOptions =
            new ComboBox<String>(FXCollections.observableArrayList(searches));
        searchOptions.setValue(searches[0]);
        searchOptions.setMinWidth(90);

        Button searchBtn = new Button();
        searchBtn.setText("Search");
        searchBtn.setMinWidth(70);
        
        Button searchfavBtn = new Button();
        searchfavBtn.setText("Search Favorites");
        searchfavBtn.setMinWidth(90);
        searchfavBtn.setVisible(loggedIn);

        searchBar.getChildren().addAll(searchField, searchOptions, searchBtn, searchfavBtn);

        // scrollable grid
        ScrollPane movieGridScroll = new ScrollPane();
        movieTable.setEditable(false);
        movieGridScroll.setContent(movieTable);
        TableColumn<Movie, String> mtitle = new TableColumn<Movie, String>("Title");
        mtitle.setMinWidth(400);
        TableColumn<Movie, Integer> myear = new TableColumn<Movie, Integer>("Year");
        myear.setMinWidth(100);
        TableColumn<Movie, String> director = new TableColumn<Movie, String>("Director");
        director.setMinWidth(200);
        TableColumn<Movie, ArrayList<String>> actors =
            new TableColumn<Movie, ArrayList<String>>("Actors");
        actors.setMinWidth(495);

        mtitle.setCellValueFactory(new PropertyValueFactory<Movie, String>("movieName"));
        myear.setCellValueFactory(new PropertyValueFactory<Movie, Integer>("movieYear"));
        director.setCellValueFactory(new PropertyValueFactory<Movie, String>("movieDir"));
        actors.setCellValueFactory(new PropertyValueFactory<Movie, ArrayList<String>>("movieAct"));

        movieData = getRandMovies();

        movieTable.setItems(movieData);
        movieTable.getColumns().addAll(mtitle, myear, director, actors);

        // report
        /**
        ScrollPane movieReportScroll = new ScrollPane();
        movieReportScroll.setMinHeight(325);
        */

        VBox movieReport = new VBox();


        movieTable.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() > 0) {
                if (movieTable.getSelectionModel().getSelectedItem() != null) {
                    getReport(movieTable.getSelectionModel().getSelectedItem(), movieReport);
                }
            }
        });

        // set default selection and report content
        if (movieTable.getItems().size() > 0) {
            movieTable.getSelectionModel().selectFirst();
            getReport(movieTable.getSelectionModel().getSelectedItem(), movieReport);
        }

        parentVbox.getChildren().addAll(topBar, searchBar, movieGridScroll, movieReport);


        // button action
        // login button action
        loginBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                // query to get user id and password for the provided username
                String query = "select user_id, password from users where username = '"
                    + userTextField.getText() + "'";

                try (Statement stmt = conn.createStatement()) {
                    // execute query
                    ResultSet rs = stmt.executeQuery(query);
                    int error = 0;
                    int userId = 0;
                    String userPass = "";
                    if (rs.next()) {
                        // get data from query
                        userId = rs.getInt("USER_ID");
                        userPass = rs.getString("PASSWORD");
                        // check that the user entered passord matches the password from the query
                        // if it does not match show error message
                        if (userPass.compareTo(userPwField.getText()) != 0) {
                            error = 1;
                        } else {
                            // if username/password matches one in database set logged in context
                            loggedIn = true;
                            loggedInName = userTextField.getText();
                            loggedInId = userId;

                            profile.setDisable(false);
                            logout.setDisable(false);

                            userLabel.setText("Welcome " + userTextField.getText());

                            // remove the login fields/buttons and show welcome message
                            topBar.getChildren().removeAll(userTextField, userPwField, loginBtn,
                                createBtn);
                            topBar.getChildren().add(userLabel);
                        }

                        // clear the username/password fields
                        userTextField.setText("");
                        userPwField.setText("");
                        if (movieTable.getSelectionModel().getSelectedItem() != null) {
                            getReport(movieTable.getSelectionModel().getSelectedItem(),
                                movieReport);
                        }

                    } else {
                        error = 1;
                    }

                    // show error message if we encountered an error
                    if (error == 1) {
                        AlertPopup.display("Error", "Incorrect username and password", "Ok");
                    }

                } catch (SQLException e) {
                    System.out.println(e);
                }
                
                searchfavBtn.setVisible(loggedIn);

            }
        });

        // create user button action
        createBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                String[] newUserValue = CreateUserPopup.display(conn, false, 0);

                if (newUserValue[0].compareTo("") != 0) {
                    loggedInId = Integer.parseInt(newUserValue[0]);
                    loggedInName = newUserValue[1];
                    loggedIn = true;
                    
                    profile.setDisable(false);
                    logout.setDisable(false);

                    userLabel.setText("Welcome " + loggedInName);

                    // remove the login fields/buttons and show welcome message
                    topBar.getChildren().removeAll(userTextField, userPwField, loginBtn,
                        createBtn);
                    topBar.getChildren().add(userLabel);
                }
            }
        });

        // menu actions
        // edit profile menu button action
        profile.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                String[] updateUserValue = CreateUserPopup.display(conn, true, loggedInId);
                if (updateUserValue[1].compareTo(loggedInName) != 0) {
                    loggedInName = updateUserValue[1];
                    userLabel.setText("Welcome " + loggedInName);
                    
                }
            }
        });

        // logout menu button action
        logout.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {
                loggedIn = false;
                loggedInName = "";
                loggedInId = 0;

                profile.setDisable(true);
                logout.setDisable(true);
                userLabel.setText("");

                topBar.getChildren().remove(userLabel);
                topBar.getChildren().addAll(userTextField, userPwField, loginBtn, createBtn);

                if (movieTable.getSelectionModel().getSelectedItem() != null) {
                    getReport(movieTable.getSelectionModel().getSelectedItem(), movieReport);
                }
                searchfavBtn.setVisible(loggedIn);

            }
        });

        // exit menu button action
        exit.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                System.exit(0);
            }
        });


        // Search button action
        searchBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                // all the items we want in the GUI
                ObservableList<Movie> listMovies = FXCollections.observableArrayList();
                listMovies.clear();
                int mId;
                String mName;
                int year;
                double critRate;
                double audRate;
                int audCount;
                String director;
                String actQuery;
                String query;
                List<String> actors = new ArrayList<String>();

                String searchFieldText = searchField.getText();
                String btnOption = (String) searchOptions.getValue();
                switch (btnOption) {
                    case "Movie":
                        query = "{CALL GetByMovie(?)}";
                        try {
                            CallableStatement stmt = conn.prepareCall(query);
                            stmt.setString(1, searchFieldText);
                            ResultSet rs = stmt.executeQuery();
                            while (rs.next()) {
                                mId = rs.getInt("MOV_ID");
                                mName = rs.getString("TITLE");
                                year = rs.getInt("YEAR");
                                critRate = rs.getDouble("CRITIC_RATE");
                                audRate = rs.getDouble("AUD_RATE");
                                audCount = rs.getInt("AUD_COUNT");
                                director = rs.getString("DIR_NAME");
                                actors.clear();
                                if (mId > 0) {
                                    actQuery =
                                        "SELECT act_name FROM movie_actors ma, actors a WHERE ma.act_id = a.act_id and ma.mov_id = "
                                            + mId + " Order by ma.Ranking DESC";
                                    CallableStatement stmt2 = conn.prepareCall(actQuery);
                                    ResultSet rs2 = stmt2.executeQuery(actQuery);
                                    while (rs2.next()) {
                                        actors.add(rs2.getString("ACT_NAME"));
                                    }
                                }

                                listMovies.add(new Movie(mId, mName, year, critRate, audRate,
                                    audCount, director, actors));

                            }

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        break;

                    case "Actor":
                        
                        getMoviesByActor(searchFieldText, listMovies, true);

                        break;

                    case "Director":
                        query = "{CALL GetByDirector(?)}";
                        try {
                            CallableStatement stmt = conn.prepareCall(query);
                            stmt.setString(1, searchFieldText);
                            ResultSet rs = stmt.executeQuery();
                            while (rs.next()) {
                                mId = rs.getInt("MOV_ID");
                                mName = rs.getString("TITLE");
                                year = rs.getInt("YEAR");
                                critRate = rs.getDouble("CRITIC_RATE");
                                audRate = rs.getDouble("AUD_RATE");
                                audCount = rs.getInt("AUD_COUNT");
                                director = rs.getString("DIR_NAME");
                                actors.clear();
                                if (mId > 0) {
                                    actQuery =
                                        "SELECT act_name FROM movie_actors ma, actors a WHERE ma.act_id = a.act_id and ma.mov_id = "
                                            + mId + " Order by ma.Ranking DESC";
                                    CallableStatement stmt2 = conn.prepareCall(actQuery);
                                    ResultSet rs2 = stmt2.executeQuery(actQuery);
                                    while (rs2.next()) {
                                        actors.add(rs2.getString("ACT_NAME"));
                                    }
                                }

                                listMovies.add(new Movie(mId, mName, year, critRate, audRate,
                                    audCount, director, actors));

                            }

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
                movieTable.setItems(listMovies);
                return;
            }
        });
        
        searchfavBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	ObservableList<Movie> listMovies = FXCollections.observableArrayList();
                listMovies.clear();
                int mId;
                String mName;
                int year;
                double critRate;
                double audRate;
                int audCount;
                String dirId;
                String director;
                String actQuery;
                String query;
                List<String> actors = new ArrayList<String>();


                System.out.println("I'm searching based on favorites");
                query = "{CALL GetByFavorites(?)}";
                try {
                	CallableStatement stmt = conn.prepareCall(query);
                    stmt.setInt(1, loggedInId);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                    	mId = rs.getInt("MOV_ID");
                        mName = rs.getString("TITLE");
                        year = rs.getInt("YEAR");
                        critRate = rs.getDouble("CRITIC_RATE");
                        audRate = rs.getDouble("AUD_RATE");
                        audCount = rs.getInt("AUD_COUNT");
                        dirId = rs.getString("DIR_ID");
                        director = rs.getString("DIR_NAME");
                        actors.clear();
                        if (mId > 0) {
                        	actQuery = "SELECT act_name FROM movie_actors ma, actors a WHERE ma.act_id = a.act_id and ma.mov_id = "
                        			+ mId + " Order by ma.Ranking DESC";
                            CallableStatement stmt2 = conn.prepareCall(actQuery);
                            ResultSet rs2 = stmt2.executeQuery(actQuery);
                            while (rs2.next()) {
                            	actors.add(rs2.getString("ACT_NAME"));
                            }
                        }

                        listMovies.add(new Movie(mId, mName, year, critRate, audRate,
                              audCount, director, actors));

                    }

                } catch (SQLException e) {
                	e.printStackTrace();
                }

                movieTable.setItems(listMovies);
                
            }
        });
        
        // Set stage
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        primaryStage.resizableProperty().set(false);
        primaryStage.setScene(new Scene(parentVbox, 1200, 800));
        primaryStage.show();

    }
}
