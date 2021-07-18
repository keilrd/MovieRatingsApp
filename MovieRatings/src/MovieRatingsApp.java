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
		exit.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				System.exit(0);
			}
		});
		
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
		//Username textfield
		TextField userTextField = new TextField();
		userTextField.setPromptText("Username");
		
		//Password textfield
		PasswordField userPwField = new PasswordField();
		userPwField.setPromptText("Password");
		
		//Login button
		Button loginBtn = new Button();
		loginBtn.setText("Login");
		loginBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				String query = "select user_id, password from users where username = '" + userTextField.getText() + "'";
				System.out.println(query);
				try (Statement stmt = conn.createStatement()){
					ResultSet rs = stmt.executeQuery(query);
					int error = 0;
					int userId = 0;
					String userPass = "";
					if (rs.next()) {
						userId = rs.getInt("USER_ID");
						userPass = rs.getString("PASSWORD");
						System.out.println("userId: " + userId);
						System.out.println("Password: " + userPass);
						if (userPass.compareTo(userPwField.getText())!=0) {
							error = 1;
						}
					} else {
						error =1;
					}
					
					if (error == 1) {
						AlertPopup.display("Error", "Incorrect username and password", "Ok");
					}
						
				} catch(SQLException e) {
					System.out.println(e);
				}
				
			}
		});
		
		//Create user button
		Button createBtn = new Button();
		createBtn.setText("Create User");
		createBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				System.out.println("Create button pressed");
				System.out.println("login: " + userTextField.getText());
				System.out.println("password: " + userPwField.getText());
			}
		});
		
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
		
		//Set stage
		primaryStage.setMinWidth(600);
		primaryStage.setMinHeight(400);
		primaryStage.setScene(new Scene(parentVbox, 1200, 800));
		conn = getConnection();
		primaryStage.show();
		
	}

}
