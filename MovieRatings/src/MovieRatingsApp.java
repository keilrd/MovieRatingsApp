import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import javax.swing.JScrollPane;
import javafx.scene.input.MouseEvent;

public class MovieRatingsApp extends Application {

    String searches[] = {"Movie", "Actor", "Director"};
    Connection conn;
    boolean loggedIn = false;
    String loggedInName = "";
    int loggedInId = 0;
    static Image STAR;
    static Image EMPTY_STAR;

    TableView<Movie> movieTable = new TableView<Movie>();
    ObservableList<Movie> movieData = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

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

    private VBox getReport(Movie movie, VBox reportBox) {

        reportBox.getChildren().clear();
        reportBox.setPadding(new Insets(10, 20, 10, 20));
        reportBox.setPrefWidth(1200);

        if (movie == null) {
            reportBox.getChildren().add(new Label("Select a movie to view information"));
            return reportBox;
        }

        HBox title = new HBox();
        title.setAlignment(Pos.BOTTOM_LEFT);
        title.setPadding(new Insets(0, 0, 5, 0));

        Label movieTitle = new Label(movie.getMovieName() + " (" + movie.getMovieYear() + ")");
        movieTitle.setFont(new Font(25));
        movieTitle.setPadding(new Insets(0, 10, 10, 5));

        title.getChildren().add(movieTitle);
        title.getChildren().add(new Label(" Directed by " + movie.getMovieDir()));

        reportBox.getChildren().add(title);

        HBox paneBox = new HBox();
        paneBox.setPrefWidth(1200);

        VBox ratingsPane = getRatingsPane(paneBox, movie);
        getActorsPane(paneBox, movie);
        getLocationsPane(paneBox, movie);

        reportBox.getChildren().add(paneBox);

        // add user rating
        if (loggedIn) {
            displayUserRating(reportBox, movie, ratingsPane);
        }


        return reportBox;
    }

    VBox getRatingsPane(HBox paneBox, Movie movie) {

        VBox ratingsVBox = new VBox();
        ratingsVBox.setPrefWidth(400);
        ratingsVBox.setAlignment(Pos.CENTER);


        displayRating(movie.getMovieCriticRating(), 10, ratingsVBox, "Critic Rating");
        displayRating(movie.getMovieAudRating(), 5, ratingsVBox, "Audience Rating");

        paneBox.getChildren().add(ratingsVBox);

        return ratingsVBox;
    }

    private void displayRating(double rating, int total, VBox ratingsVBox, String label) {

        Label ratingLabel = new Label(label + " (" + rating + "/" + total + ")");
        ratingLabel.setFont(new Font(20));
        ratingLabel.setMaxWidth(400);
        ratingLabel.setAlignment(Pos.BASELINE_CENTER);
        ratingLabel.setPadding(new Insets(5));

        HBox ratingHBox = new HBox();
        ratingHBox.setPrefWidth(400);
        ratingHBox.setPadding(new Insets(0, 0, 10, 0));

        rating = Math.round(rating);

        int i = 0;

        while (i < rating) {
            ratingHBox.getChildren().add(getStarImage(total));
            i++;
        }

        while (i < total) {
            ratingHBox.getChildren().add(getEmptyStarImage(total));
            i++;
        }

        ratingsVBox.getChildren().addAll(ratingLabel, ratingHBox);


        return;
    }

    private void displayUserRating(VBox reportBox, Movie movie, VBox ratingsPane) {

        double rating = getUserRating(movie);

        Label ratingLabel = new Label("My Rating (" + rating + "/5)");
        ratingLabel.setFont(new Font(25));
        ratingLabel.setMaxWidth(400);
        ratingLabel.setAlignment(Pos.CENTER);
        ratingLabel.setPadding(new Insets(5));

        HBox ratingHBox = new HBox();
        ratingHBox.setPrefWidth(1200);
        ratingHBox.setPadding(new Insets(0, 0, 10, 0));
        ratingHBox.setAlignment(Pos.CENTER);

        ratingHBox.getChildren().add(ratingLabel);

        rating = Math.round(rating);

        int i = 0;

        while (i < rating) {

            ImageView starImage = getStarImage(5);
            final float newRating = i + 1;

            starImage.setPickOnBounds(true);

            starImage.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event) {

                    updateUserRating(newRating, ratingHBox, movie, ratingsPane); // TODO test that instant and
                                                                    // rating are updated in
                                                                    // database
                    event.consume();

                }
            });

            ratingHBox.getChildren().add(starImage);
            i++;
        }

        while (i < 5) {

            ImageView emptyStarImage = getEmptyStarImage(5);
            final float newRating = i + 1;

            emptyStarImage.setPickOnBounds(true);

            emptyStarImage.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {

                        updateUserRating(newRating, ratingHBox, movie, ratingsPane);
                        event.consume();

                    }
                });
            ratingHBox.getChildren().add(emptyStarImage);
            i++;
        }

        reportBox.getChildren().addAll(ratingHBox);


        return;
    }

    private void updateUserRating(float newRating, HBox ratingHBox, Movie movie, VBox ratingsPane) {

        int mId = movie.getMovieID();

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


            // update user rating display
            
            Label userRateLabel = (Label) ratingHBox.getChildren().get(0); // user rating label
            userRateLabel.setText( "My Rating (" + newRating + "/" + 5 + ")");
            
            for (int i = 1; i <= 5; i++) {

                ImageView image = (ImageView) ratingHBox.getChildren().get(i);

                if (i <= newRating) {

                    image.setImage(STAR);
                }

                else {

                    image.setImage(EMPTY_STAR);
                }
                
            }
            
            // update audience rating display if the average rating changed
            if (changed) {
                
                Label audRateLabel = (Label) ratingsPane.getChildren().get(2); // label for audience rating
                audRateLabel.setText( "Audience Rating (" + movie.getMovieAudRating() + "/" + 5 + ")");
                
                HBox audRateHBox = (HBox) ratingsPane.getChildren().get(3);
                
                for (int i = 0; i < 5; i++) {

                    ImageView image = (ImageView) audRateHBox.getChildren().get(i);

                    if (i < movie.getMovieAudRating()) {

                        image.setImage(STAR);
                    }

                    else {

                        image.setImage(EMPTY_STAR);
                    }
                    
                }
            }

        } catch (SQLException e) {
            System.out.println(e); // TODO error about unable to update?
        } catch (Exception e) {
            System.out.println(e);
        }


    }

    private ImageView getStarImage(int total) {

        ImageView starImage = null;

        try {
            starImage = new ImageView(STAR);
            starImage.setFitWidth(400 / total);
            starImage.setPreserveRatio(true);

        } catch (NullPointerException e) {
            Alert missingImageAlert =
                new Alert(AlertType.ERROR, "An Image Used by the Application is Missing");
            missingImageAlert.setHeaderText("Image Not Found");
            missingImageAlert.show();

        }

        return starImage;

    }


    private ImageView getEmptyStarImage(int total) {

        ImageView starImage = null;

        try {
            starImage = new ImageView(EMPTY_STAR);
            starImage.setFitWidth(400 / total);
            starImage.setPreserveRatio(true);


        } catch (NullPointerException e) {
            Alert missingImageAlert =
                new Alert(AlertType.ERROR, "An Image Used by the Application is Missing");
            missingImageAlert.setHeaderText("Image Not Found");
            missingImageAlert.show();

        }

        return starImage;

    }

    private double getUserRating(Movie movie) {

        int mId = movie.getMovieID();
        double rating = 0;

        try (Statement stmt = conn.createStatement()) {

            String query = "SELECT Rating FROM user_ratings WHERE User_id = " + loggedInId
                + " and mov_id = " + mId;

            ResultSet result = stmt.executeQuery(query);

            String ratingResult = "";

            while (result.next()) {

                ratingResult = result.getString("Rating");

            }

            if (ratingResult.length() == 0) {
                return 0;
            }

            rating = Double.parseDouble(ratingResult);

        } catch (SQLException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }

        return rating;
    }

    private void getActorsPane(HBox paneBox, Movie movie) {

        ScrollPane actorsPane = new ScrollPane();
        actorsPane.setPrefHeight(250);
        actorsPane.setMaxWidth(400);

        VBox actorsVBox = new VBox();

        // add actors to display
        for (String actor : movie.actors) {

            Hyperlink actorLink = new Hyperlink(actor);

            actorLink.setOnAction(e -> {

                ObservableList<Movie> listMovies = FXCollections.observableArrayList();
                getMoviesByActor(actor, listMovies);
                movieTable.setItems(listMovies);

                System.out.println(actor + " was clicked!"); // TODO remove

            });

            actorsVBox.getChildren().add(actorLink);
        }

        actorsPane.setContent(actorsVBox);

        Label actorsLabel = new Label("Actors");
        actorsLabel.setFont(new Font(15));
        actorsLabel.setMaxWidth(400);
        actorsLabel.setAlignment(Pos.BASELINE_CENTER);
        actorsLabel.setPadding(new Insets(5));

        VBox actorVBox = new VBox();
        actorVBox.getChildren().addAll(actorsLabel, actorsPane);
        actorVBox.setPrefWidth(400);

        paneBox.getChildren().add(actorVBox);

        return;
    }

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
                    Label locationLabel = new Label(text);
                    locationLabel.setPadding(new Insets(5));

                    locationsVBox.getChildren().add(locationLabel);
                }

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

    private void getMoviesByActor(String searchFieldText, ObservableList<Movie> listMovies) {

        String query = "{CALL GetByActor(?)}";

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
                String dirId = rs.getString("DIR_ID");
                String director = rs.getString("DIR_NAME");

                List<String> actors = new ArrayList<String>();

                if (mId > 0) {
                    String actQuery =
                        "SELECT act_name FROM movie_actors m, actors a WHERE m.act_id = a.act_id and mov_id = "
                            + mId + " Order by Ranking DESC limit 3";
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

        searchBar.getChildren().addAll(searchField, searchOptions, searchBtn);

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
                String dirId;
                String director;
                String actQuery;
                String query;
                List<String> actors = new ArrayList<String>();

                String searchFieldText = searchField.getText();
                String btnOption = (String) searchOptions.getValue();
                System.out.println(btnOption);
                System.out.println(searchFieldText);
                switch (btnOption) {
                    case "Movie":
                        System.out.println("I'm searching based on movies");
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
                                dirId = rs.getString("DIR_ID");
                                director = rs.getString("DIR_NAME");
                                actors.clear();
                                if (mId > 0) {
                                    actQuery =
                                        "SELECT act_name FROM movie_actors m, actors a WHERE m.act_id = a.act_id and mov_id = "
                                            + mId + " Order by Ranking DESC limit 3";
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
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        break;

                    case "Actor":

                        System.out.println("I'm searching based on Actors");

                        getMoviesByActor(searchFieldText, listMovies);

                        break;

                    case "Director":
                        System.out.println("I'm searching based on Directors");
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
                                dirId = rs.getString("DIR_ID");
                                director = rs.getString("DIR_NAME");
                                actors.clear();
                                if (mId > 0) {
                                    actQuery =
                                        "SELECT act_name FROM movie_actors m, actors a WHERE m.act_id = a.act_id and mov_id = "
                                            + mId + " Order by Ranking DESC limit 3";
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
                            // TODO Auto-generated catch block
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
        // Set stage
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        primaryStage.resizableProperty().set(false);
        primaryStage.setScene(new Scene(parentVbox, 1200, 800));
        primaryStage.show();

    }
}
