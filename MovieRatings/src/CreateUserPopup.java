import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import java.sql.*;


/**
 * Class for the create user and edit profile popup
 *
 */
public class CreateUserPopup {
	
	//define some global variables
	static volatile String userNameValue = ""; //username
	static volatile String userPassValue = ""; //user password
	static volatile String favGenreValue = ""; //the users favorite genre
	static volatile String favDirValue = ""; //the users favorite director name
	static volatile String favDirId = ""; //the users favorite director id
	static volatile String favActorValue = ""; //the users favorite actor name 
	static volatile String favActorId = ""; //the users favorite actor id
	
	/**
	 * Displays a create/edit user popup. 
	 * @param conn database connection
	 * @param editMode enter false for create new user or true to edit user
	 * @param the userid of the currently logged in user. Pass 0 if editMode = false. 
	 * @return an array of strings containing userid and username for login context
	 */
	public static String[] display(Connection conn, boolean editMode, int userId) {
		//set up the window as modal popup
		Stage window = new Stage();
		window.initModality(Modality.APPLICATION_MODAL);
		
		//set popup titel
		if (editMode) {
			//if we are in edit profile mode
			window.setTitle("Edit Profile");
		} else {
			//if we are in create user mode
			window.setTitle("Create User");
		}
		
		//define min and max height and width
		window.setMinWidth(500);
		window.setMinHeight(300);
		window.setMaxWidth(500);
		window.setMaxHeight(325);
		
		//initiate elements
		String[] userValues = new String[2];
		
		//setup labels for various fields
		Label userName = new Label("Username:");
		Label userPw1Label = new Label("Password:");
		Label favGenre = new Label("Favorite Genre:");
		Label favDir= new Label("Favorite Director:");
		Label favActor = new Label("Favorite Actor:");
		Label errorLabel = new Label("");
		
		//set all global variables to null
		userNameValue = "";
		userPassValue = "";
		favGenreValue = "";
		favDirValue = "";
		favDirId = "";
		favActorValue = "";
		favActorId = "";
		
		//if editing profile get current values from the database
		if(editMode) {
			//set up query to get user profile
			String query = "SELECT * FROM users u, actors a, directors d WHERE user_id = " + userId + " and u.fav_act = a.act_id and u.fav_dir = d.dir_id";
			
			try (Statement stmt = conn.createStatement()){
				//execute query
				ResultSet rs = stmt.executeQuery(query);
				String userPass = "";
				
				//get values returned from query
				if (rs.next()) {
					//get data from query
					userNameValue = rs.getString("USERNAME");
					userPassValue = rs.getString("PASSWORD");
					favGenreValue = rs.getString("FAV_GENRE");
					favDirValue = rs.getString("DIR_NAME");
					favDirId = rs.getString("DIR_ID");
					favActorValue = rs.getString("ACT_NAME");
					favActorId = rs.getString("ACT_ID");
				} 
			}catch(SQLException e) {
				System.out.println(e);
			}
		}
		
		
		//Set default users strings to null so we can return if user closes popup
		if(editMode) {
			userValues[0] = String.valueOf(userId);
			userValues[1] = userNameValue; 
		}else {
			userValues[0] = "";
			userValues[1] = "";
		}
		
		//set up username field
		TextField userNameField = new TextField();
		userNameField.setMinWidth(360);
		userNameField.setText(userNameValue);
		
		//set up password fields
		PasswordField userPw1Field = new PasswordField();
		PasswordField userPw2Field = new PasswordField();
		userPw2Field.setPromptText("Confirm password");
		userPw1Field.setText(userPassValue);
		userPw2Field.setText(userPassValue);
		
		//set up favorites selectors
		TextField favGenreField = new TextField();
		Button genreSearch = new Button("Search");
		favGenreField.setMinWidth(310);
		favGenreField.setText(favGenreValue);
		
		TextField favDirField = new TextField();
		Button dirSearch = new Button("Search");
		favDirField.setMinWidth(310);
		favDirField.setText(favDirValue);
		
		TextField favActorField = new TextField();
		Button actSearch = new Button("Search");
		favActorField.setMinWidth(310);
		favActorField.setText(favActorValue);
		
		//set up accept/cancel buttons
		Button acceptBtn = new Button("Accept");
		Button cancelBtn = new Button("Cancel");
		
		//genre search button action
		genreSearch.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				String[] searchResults;
				
				//call generic search for genres
				searchResults = SearchRecords.display(conn, "Genres", favGenreField.getText());
				//set value in the favorite genre field
				favGenreField.setText(searchResults[1]);
				//set value in global
				favGenreValue = searchResults[0];
				
			}
		});
		
		//actor search button action
		actSearch.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				String[] searchResults;
				
				//call generic search for actors
				searchResults = SearchRecords.display(conn, "Actors", favActorField.getText());
				//set value in the favorite actors field
				favActorField.setText(searchResults[1]);
				//set actor id in the global
				favActorId = searchResults[0];
				//set actor name in the global
				favActorValue = searchResults[1];
				
			}
		});
		
		//director search button action
		dirSearch.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				String[] searchResults;
				
				//call generic search for director
				searchResults = SearchRecords.display(conn, "Directors", favDirField.getText());
				//set value in the favorite director field
				favDirField.setText(searchResults[1]);
				//set director id in the global
				favDirId = searchResults[0];
				//set director name in the global
				favDirValue = searchResults[1];
				
			}
		});
		
		//accept button handler
		acceptBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				boolean error = false;
				int checkUser = 0;
				String query = "";
				
				//field validation
				if(userNameField.getText().compareTo("") == 0) {
					//blank username
					AlertPopup.display("Error", "Username is required!", "Go back");
					error = true;
				} else if (!error && (userPw1Field.getText().compareTo("") == 0)) {
					//blank password
					AlertPopup.display("Error", "Password is required!", "Go back");
					error = true;
				}else if (!error && (userPw1Field.getText().compareTo(userPw2Field.getText()) != 0)) {
					//passwords do not match
					AlertPopup.display("Error", "Passwords do not match!", "Go back"); 
					error = true;
				}else if (favGenreValue.compareTo("") == 0 || favDirValue.compareTo("") == 0|| favActorValue.compareTo("") == 0) {
					//favorites are not populated
					AlertPopup.display("Error","Favorites are required!","Go back");
					error = true;
				}else if (!error) {
					//check that username is not already taken
					//build query
					query = "SELECT user_id FROM users WHERE username = '" + userNameField.getText() + "';";
					try (Statement stmt = conn.createStatement()){
						//execute query
						ResultSet rs = stmt.executeQuery(query);
						//if value is returned
						if(rs.next()) {
							checkUser = rs.getInt("USER_ID");
						}
					}catch(SQLException e) {
						System.out.println(e);
					}
					
					if (!editMode && (checkUser > 0)) {
						//throw error if in create user mode and user is already in table
						error = true;
						AlertPopup.display("Error", "Username is already taken!", "Go back");
					} else if (editMode && (checkUser > 0 ) && (checkUser != userId)) {
						//throw error if in update profile mode, user mode is in the table, and it not for the current user
						error = true;
						AlertPopup.display("Error", "Username is already taken!", "Go back");
					}
				}
				if (!error) {
					//if we pass all validation
					//save user info
					//set username and password globals
					userNameValue = userNameField.getText();
					userPassValue = userPw1Field.getText();
					
					if(editMode) {
						//if in edit profile mode we need to update a row
						//generate query
						query = "update users set username = ?, password = ?, fav_act = ?, fav_dir = ?, fav_genre = ? WHERE user_ID = ?";
						
						try {
							//execute query
							PreparedStatement preparedStmt = conn.prepareStatement(query);
							preparedStmt.setString(1, userNameValue);
						    preparedStmt.setString(2, userPassValue);
						    preparedStmt.setString(3, favActorId);
						    preparedStmt.setString(4, favDirId);
						    preparedStmt.setString(5, favGenreValue);
						    preparedStmt.setInt(6, userId);
						    preparedStmt.execute();
						    
						} catch (SQLException e) {
							e.printStackTrace();
						}
						
						//set return values to userid and current username
						userValues[0] = String.valueOf(userId);
						userValues[1] = userNameValue;
						
					} else {
						//if in create user mode we need to insert a new row
						//generate query to get the max user id from the database
						String queryMax = "SELECT MAX(user_id) FROM users;";
						int maxUser = 0;
						
						//excute max user id query
						try (Statement stmt = conn.createStatement()){
							//execute query
							ResultSet rs = stmt.executeQuery(queryMax);
							if(rs.next()) {
								maxUser = rs.getInt("MAX(USER_ID)");
								maxUser++;
							}
						}catch(SQLException e) {
							System.out.println(e);
						}
						
						//generate query to save new user
						query = " insert into users (user_id, username, password, fav_act, fav_dir, fav_genre)"
						        + " values (?, ?, ?, ?, ?, ?)";
						
						//execute query to save new user
						PreparedStatement preparedStmt;
						try {
							preparedStmt = conn.prepareStatement(query);
							preparedStmt.setInt(1, maxUser);
						    preparedStmt.setString(2, userNameValue);
						    preparedStmt.setString(3, userPassValue);
						    preparedStmt.setString(4, favActorId);
						    preparedStmt.setString(5, favDirId);
						    preparedStmt.setString(6, favGenreValue);
						    preparedStmt.execute();
						} catch (SQLException e) {
							e.printStackTrace();
						}
						
						//set return values to userid and current username
						userValues[0] = String.valueOf(maxUser);
						userValues[1] = String.valueOf(userNameValue);
					}
					window.close();
				}
			}
		});
		
		//handler for cancel button
		cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				//close window and set return values
				window.close();
				
				if (editMode) {
					//if in edit profil mode return the existing user id and username
					userValues[0] = String.valueOf(editMode);
					userValues[1] = userNameValue;
				} else {
					//if in create user mode return null
					userValues[0] = "";
					userValues[1] = "";
				}
			}
		});
		
		//set up gridpane for the labels and fields
		GridPane labelFieldPane = new GridPane();
		labelFieldPane.setHgap(10);
		labelFieldPane.setVgap(10);
		
		//set up hbox for favorite fields and buttons
		HBox genreHbox = new HBox();
		genreHbox.getChildren().addAll(favGenreField, genreSearch);
		HBox actHbox = new HBox();
		actHbox.getChildren().addAll(favActorField, actSearch);
		HBox dirHbox = new HBox();
		dirHbox.getChildren().addAll(favDirField, dirSearch);

		//add elements to the gridpane
        labelFieldPane.add(userName, 0, 0, 1, 1);
        labelFieldPane.add(userNameField, 1, 0, 1, 1);
        labelFieldPane.add(userPw1Label, 0, 1, 1, 1);
        labelFieldPane.add(userPw1Field, 1, 1, 1, 1);
        labelFieldPane.add(userPw2Field, 1, 2, 1, 1);
        labelFieldPane.add(favGenre, 0, 3, 1, 1);
        labelFieldPane.add(genreHbox, 1, 3, 1, 1);
        labelFieldPane.add(favDir, 0, 4, 1, 1);
        labelFieldPane.add(dirHbox, 1, 4, 1, 1);
        labelFieldPane.add(favActor, 0, 5, 1, 1);
        labelFieldPane.add(actHbox, 1, 5, 1, 1);

		//set up the buttons box
		HBox buttonHbox = new HBox(10);
		buttonHbox.setAlignment(Pos.BASELINE_RIGHT);
		buttonHbox.getChildren().addAll(
			acceptBtn,
			cancelBtn
		);
		
		//add all elements to a vbox
		VBox popLayout = new VBox(10);
		popLayout.setPadding(new Insets(10,10,10,10));
		popLayout.getChildren().addAll(
			labelFieldPane,
			errorLabel,
			buttonHbox
		);
		popLayout.setAlignment(Pos.CENTER);
		 
		//display scene
		Scene scene = new Scene(popLayout);
		window.setScene(scene);
		window.showAndWait();
		
		//return values
		return userValues;
	}

}