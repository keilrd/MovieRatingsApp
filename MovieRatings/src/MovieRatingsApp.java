import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;
import java.util.Properties;


public class MovieRatingsApp extends Application {
	
	String searches[] = {"Movies", "Actors", "Directors"};
	Connection conn;
	boolean loggedIn = false;
	String loggedInName = "";
	int loggedInId = 0;

	public static void main(String[] args) {
		launch(args);
	}
	
	public Connection getConnection() throws SQLException {

	    Connection con = null;
	    Properties connectionProps = new Properties();
	    connectionProps.put("user", "root");
	    connectionProps.put("password", "12345");
	    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/movie_ratings",connectionProps);
	    System.out.println("Connected to database");
	    return con;
	}
	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("MovieRatings");
		
		//Parent Vbox
		VBox parentVbox = new VBox();
		
		//Top bar
		HBox topBar = new HBox(10);
		topBar.setPadding(new Insets(10,10,10,10));
		
		Pane spacer = new Pane();
		topBar.setHgrow(spacer, Priority.ALWAYS);
		spacer.setMinSize(10, 1);
		
		
		//Menu bar
		MenuBar menuBar = new MenuBar();
		
		Menu mainMenu = new Menu("Menu");
		
		MenuItem addMovie = new MenuItem("Add Movie");
		addMovie.setDisable(true);
		MenuItem addAct = new MenuItem("Add Actor");
		addAct.setDisable(true);
		MenuItem addDir = new MenuItem("Add Director");
		addDir.setDisable(true);
		MenuItem profile = new MenuItem("Edit Profile");
		profile.setDisable(true);
		
		MenuItem logout = new MenuItem("Logout");
		logout.setDisable(true);
		
		MenuItem exit = new MenuItem("Exit");
		
		
		mainMenu.getItems().addAll(
			addMovie,
			addAct,
			addDir,
			profile,
			logout,
			exit
		);
		
		menuBar.getMenus().add(mainMenu);
		
		//Login bar
		//Welcome user banner
		Label userLabel = new Label("");
		
		//Username textfield
		TextField userTextField = new TextField();
		userTextField.setPromptText("Username");
		
		//Password textfield
		PasswordField userPwField = new PasswordField();
		userPwField.setPromptText("Password");
		
		//Login button
		Button loginBtn = new Button();
		loginBtn.setText("Login");
		
		//Create user button
		Button createBtn = new Button();
		createBtn.setText("Create User");
		
		topBar.getChildren().addAll(
			menuBar,
			spacer,
			userTextField,
			userPwField,
			loginBtn,
			createBtn
		);
		
		//Search bar
		HBox searchBar = new HBox(10);
		searchBar.setAlignment(Pos.CENTER);
		searchBar.setPadding(new Insets(0,10,10,10));
		
		TextField searchField = new TextField();
		searchField.setPromptText("Search");
		searchField.setPrefWidth(700);
		
		ComboBox<String> searchOptions = new ComboBox<String>(FXCollections.observableArrayList(searches));
		searchOptions.setValue(searches[0]);
		searchOptions.setMinWidth(90);
		
		Button searchBtn = new Button();
		searchBtn.setText("Search");
		searchBtn.setMinWidth(70);
		
		searchBar.getChildren().addAll(
			searchField,
			searchOptions,
			searchBtn
		);
		
		parentVbox.getChildren().addAll(
			topBar,
			searchBar
		);
		
		
		//button action
		//login button action
		loginBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				//query to get user id and password for the provided username
				String query = "select user_id, password from users where username = '" + userTextField.getText() + "'";
				
				try (Statement stmt = conn.createStatement()){
					//execute query
					ResultSet rs = stmt.executeQuery(query);
					int error = 0;
					int userId = 0;
					String userPass = "";
					if (rs.next()) {
						//get data from query
						userId = rs.getInt("USER_ID");
						userPass = rs.getString("PASSWORD");
						//check that the user entered passord matches the password from the query
						//if it does not match show error message
						if (userPass.compareTo(userPwField.getText())!=0) {
							error = 1;
						} else {
							//if username/password matches one in database set logged in context
							loggedIn = true;
							loggedInName = userTextField.getText();
							loggedInId = userId;
							
							addMovie.setDisable(false);
							addAct.setDisable(false);
							addDir.setDisable(false);
							profile.setDisable(false);
							logout.setDisable(false);
							
							userLabel.setText("Welcome " + userTextField.getText());
							
							//remove the login fields/buttons and show welcome message
							topBar.getChildren().removeAll(userTextField,
									userPwField,
									loginBtn,
									createBtn
								);
							topBar.getChildren().add(userLabel);
						}
						
						//clear the username/password fields
						userTextField.setText("");
						userPwField.setText("");
						
					} else {
						error =1;
					}
					
					//show error message if we encountered an error
					if (error == 1) {
						AlertPopup.display("Error", "Incorrect username and password", "Ok");
					}
						
				} catch(SQLException e) {
					System.out.println(e);
				}
				
			}
		});
		
		//create user button action
		createBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				System.out.println("Create button pressed");
				CreateUserPopup.display(conn);
			}
		});
		
		//menu actions
		//add movie menu button action
		addMovie.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				System.out.println("add movie menu");
			}
		});
		
		//add actor menu button action
		addAct.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				System.out.println("add actor menu");
			}
		});
		
		//add director menu button action
		addDir.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				System.out.println("add director menu");
			}
		});
		
		//edit profile menu button action
		profile.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				System.out.println("edit profile menu");
			}
		});
		
		//logout menu button action
		logout.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				loggedIn = false;
				loggedInName = "";
				loggedInId = 0;
				
				addMovie.setDisable(true);
				addAct.setDisable(true);
				addDir.setDisable(true);
				profile.setDisable(true);
				logout.setDisable(true);
				userLabel.setText("");
				
				topBar.getChildren().remove(userLabel);
				topBar.getChildren().addAll(userTextField,
						userPwField,
						loginBtn,
						createBtn
				);
				
			}
		});
		
		//exit menu button action
		exit.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				System.exit(0);
			}
		});
		
		
		//Set stage
		primaryStage.setMinWidth(600);
		primaryStage.setMinHeight(400);
		primaryStage.setScene(new Scene(parentVbox, 1200, 800));
		conn = getConnection();
		primaryStage.show();
		
	}

}
