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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import javax.swing.JScrollPane;

public class MovieRatingsApp extends Application {

    String searches[] = {"Movie", "Actor", "Director"};
    Connection conn;
    boolean loggedIn = false;
    String loggedInName = "";
    int loggedInId = 0;

    TableView<Movie> movieTable = new TableView<Movie>();
    ObservableList<Movie> movieData = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    public Connection getConnection() throws SQLException {
        Connection con = null;
        Properties connectionProps = new Properties();

        // define some database properties
        // TODO update mysql username and password
        connectionProps.put("user", "root");
        connectionProps.put("password", "12345");

        // specify database
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/movie_ratings",
            connectionProps);

        // TODO remove dubug statement for connection to database
        System.out.println("Connected to database");

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



            // Movie(int mId, String mName, int year, double critRate, double audRate, int audCount,
            // String director, List<String> actors)
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

        TextArea movieInfo = new TextArea();
        String text = "Movie info!";

        /*
         * - critic rating - user rating - director - actors - filming locations - buttons to rate
         * the movie
         */

        // movieInfo.setText(text);
        // movieInfo.setPrefHeight(250);
        // movieInfo.setPrefWidth(1159);

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

        getRatingsPane(paneBox, movie);
        getActorsPane(paneBox, movie);
        getLocationsPane(paneBox, movie);

        reportBox.getChildren().add(paneBox);
        return reportBox;
    }

    void getRatingsPane(HBox paneBox, Movie movie) {

        VBox ratingsVBox = new VBox();
        ratingsVBox.setPrefWidth(400);
        ratingsVBox.setAlignment(Pos.CENTER);
        
        double criticRating = movie.getMovieCriticRating();

        Label criticRatingLabel = new Label("Critic Rating (" + criticRating + "/10)");
        criticRatingLabel.setFont(new Font(25));
        criticRatingLabel.setMaxWidth(400);
        criticRatingLabel.setAlignment(Pos.BASELINE_CENTER);
        criticRatingLabel.setPadding(new Insets(5));
         
        HBox criticRatingBox = displayRating(criticRating, 10);
        criticRatingBox.setPadding(new Insets(0, 0, 10, 0));
        
        double audienceRating = movie.getMovieAudRating();

        Label audienceRatingLabel = new Label("Audience Rating (" + audienceRating + "/5)");
        audienceRatingLabel.setFont(new Font(25));
        audienceRatingLabel.setMaxWidth(400);
        audienceRatingLabel.setAlignment(Pos.BASELINE_CENTER);
        audienceRatingLabel.setPadding(new Insets(5));
        
        HBox audienceRatingBox = displayRating(audienceRating,5);

        ratingsVBox.getChildren().addAll(criticRatingLabel, criticRatingBox, audienceRatingLabel, audienceRatingBox);


        paneBox.getChildren().add(ratingsVBox);

        return;
    }
    
    private HBox displayRating(double rating, int total) {
        
        HBox ratingHBox = new HBox();
        ratingHBox.setPrefWidth(400);
        
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
   
        
        
        return ratingHBox;
    }

    private ImageView getStarImage(int total) {
        
        ImageView starImage = null;

        try {
            starImage =
                new ImageView(new Image(getClass().getResourceAsStream("star.png")));
            starImage.setFitWidth(400/total);
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
            starImage =
                new ImageView(new Image(getClass().getResourceAsStream("empty_star.png")));
            starImage.setFitWidth(400/total);
            starImage.setPreserveRatio(true);

        } catch (NullPointerException e) {
            Alert missingImageAlert =
                new Alert(AlertType.ERROR, "An Image Used by the Application is Missing");
            missingImageAlert.setHeaderText("Image Not Found");
            missingImageAlert.show();

        }
        
        return starImage;
        
    }

    void getActorsPane(HBox paneBox, Movie movie) {

        ScrollPane actorsPane = new ScrollPane();
        actorsPane.setPrefHeight(250);
        actorsPane.setMaxWidth(400);

        VBox actorsVBox = new VBox();

        // add actors to display
        for (String actor : movie.actors) {

            Hyperlink actorLink = new Hyperlink(actor);

            actorLink.setOnAction(e -> {

                // TODO add movies for actor query here

                System.out.println(actor + " was clicked!");
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



    @Override
    public void start(Stage primaryStage) throws Exception {
        conn = getConnection();
        primaryStage.setTitle("MovieRatings");
        
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
                
                if(newUserValue[0].compareTo("") != 0) {
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

            }
        });

        // exit menu button action
        exit.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                System.exit(0);
            }
        });

        
		//Search button action
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
				String btnOption = (String)searchOptions.getValue();
				System.out.println(btnOption);
				System.out.println(searchFieldText);
				switch(btnOption) {
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
							if (mId > 0){
								actQuery = "SELECT act_name FROM movie_actors m, actors a WHERE m.act_id = a.act_id and mov_id = " + mId + " Order by Ranking DESC limit 3";
								CallableStatement stmt2 = conn.prepareCall(actQuery);
								ResultSet rs2 = stmt2.executeQuery(actQuery);
								while(rs2.next()) {
									actors.add(rs2.getString("ACT_NAME"));
								}
							}
							
							listMovies.add(new Movie(mId, mName, year, critRate, audRate, audCount, director, actors));

							}
						
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
					case "Actor":
						System.out.println("I'm searching based on Actors");
						query = "{CALL GetByActor(?)}";
						
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
							if (mId > 0){
								actQuery = "SELECT act_name FROM movie_actors m, actors a WHERE m.act_id = a.act_id and mov_id = " + mId + " Order by Ranking DESC limit 3";
								CallableStatement stmt2 = conn.prepareCall(actQuery);
								ResultSet rs2 = stmt2.executeQuery(actQuery);
								while(rs2.next()) {
									actors.add(rs2.getString("ACT_NAME"));
								}
							}
							
							listMovies.add(new Movie(mId, mName, year, critRate, audRate, audCount, director, actors));

							}
						
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
						
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
								if (mId > 0){
									actQuery = "SELECT act_name FROM movie_actors m, actors a WHERE m.act_id = a.act_id and mov_id = " + mId + " Order by Ranking DESC limit 3";
									CallableStatement stmt2 = conn.prepareCall(actQuery);
									ResultSet rs2 = stmt2.executeQuery(actQuery);
									while(rs2.next()) {
										actors.add(rs2.getString("ACT_NAME"));
									}
								}
								
								listMovies.add(new Movie(mId, mName, year, critRate, audRate, audCount, director, actors));

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
