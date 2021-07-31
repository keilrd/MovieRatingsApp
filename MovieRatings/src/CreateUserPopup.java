import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import java.sql.*;

public class CreateUserPopup {
	
	/**
	 * Displays a create/edit user popup. 
	 * @param conn database connection
	 * @param editMode enter false for create new user or true to edit user
	 * @param the userid of the currently logged in user. Pass 0 if editMode = false. 
	 * @return an array of strings containing userid and username for login context
	 */
	public static String[] display(Connection conn, boolean editMode, int userId) {
		Stage window = new Stage();
		
		window.initModality(Modality.APPLICATION_MODAL);
		if (editMode) {
			window.setTitle("Edit Profile");
		} else {
			window.setTitle("Create User");
		}
		window.setMinWidth(500);
		window.setMinHeight(300);
		window.setMaxWidth(500);
		window.setMaxHeight(325);
		
		//initiate elements
		String[] userValues = new String[2];
		
		Label userName = new Label("Username:");
		Label userPw1Label = new Label("Password:");
		Label favGenre = new Label("Favorite Genre:");
		Label favDir= new Label("Favorite Director:");
		Label favActor = new Label("Favorite Actor:");
		Label errorLabel = new Label("");
		
		String userNameValue = "";
		String userPassValue = "";
		String favGenreValue = "";
		String favDirValue = "";
		String favActorValue = "";
		
		//if editing profile get current values from the database
		if(editMode) {
			String query = "SELECT * FROM users u, actors a, directors d WHERE user_id = " + userId + " and u.fav_act = a.act_id and u.fav_dir = d.dir_id";
			
			try (Statement stmt = conn.createStatement()){
				//execute query
				ResultSet rs = stmt.executeQuery(query);
				String userPass = "";
				if (rs.next()) {
					//get data from query
					userNameValue = rs.getString("USERNAME");
					userPassValue = rs.getString("PASSWORD");
					favGenreValue = rs.getString("FAV_GENRE");
					favDirValue = rs.getString("DIR_NAME");
					favActorValue = rs.getString("ACT_NAME");
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
		
		//set up favorite selectors
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
		
		Button acceptBtn = new Button("Accept");
		Button cancelBtn = new Button("Cancel");
		
		genreSearch.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				SearchRecords.display("Genre", favGenreField.getText());
			}
		});
		
		
		acceptBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				//TODO add query to check username is not already taken
				//TODO try to save the user to database
				//TODO update userValues
				System.out.println("accept button clicked");
				
				window.close();
			}
		});
		
		cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				window.close();
				userValues[0] = "";
				userValues[1] = "";
			}
		});
		
		GridPane labelFieldPane = new GridPane();
		labelFieldPane.setHgap(10);
		labelFieldPane.setVgap(10);
		
		HBox genreHbox = new HBox();
		genreHbox.getChildren().addAll(favGenreField, genreSearch);
		HBox actHbox = new HBox();
		actHbox.getChildren().addAll(favActorField, actSearch);
		HBox dirHbox = new HBox();
		dirHbox.getChildren().addAll(favDirField, dirSearch);

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

		
		HBox buttonHbox = new HBox(10);
		buttonHbox.setAlignment(Pos.BASELINE_RIGHT);
		buttonHbox.getChildren().addAll(
			acceptBtn,
			cancelBtn
		);
		
		VBox popLayout = new VBox(10);
		popLayout.setPadding(new Insets(10,10,10,10));
		popLayout.getChildren().addAll(
			labelFieldPane,
			errorLabel,
			buttonHbox
		);
		popLayout.setAlignment(Pos.CENTER);
		
		Scene scene = new Scene(popLayout);
		window.setScene(scene);
		window.showAndWait();
		
		return userValues;
	}

}