import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;
import java.sql.*;

public class CreateUserPopup {
	public static String[] display(Connection conn) {
		Stage window = new Stage();
		
		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle("Create User");
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
		
		TextField userNameField = new TextField();
		userNameField.setMinWidth(360);
		PasswordField userPw1Field = new PasswordField();
		PasswordField userPw2Field = new PasswordField();
		userPw2Field.setPromptText("Confirm password");
		TextField favGenreField = new TextField();
		TextField favDirField = new TextField();
		TextField favActorField = new TextField();
		
		Button acceptBtn = new Button("Accept");
		Button cancelBtn = new Button("Cancel");
		
		GridPane labelFieldPane = new GridPane();
		labelFieldPane.setHgap(10);
		labelFieldPane.setVgap(10);

        labelFieldPane.add(userName, 0, 0, 1, 1);
        labelFieldPane.add(userNameField, 1, 0, 1, 1);
        labelFieldPane.add(userPw1Label, 0, 1, 1, 1);
        labelFieldPane.add(userPw1Field, 1, 1, 1, 1);
        labelFieldPane.add(userPw2Field, 1, 2, 1, 1);
        labelFieldPane.add(favGenre, 0, 3, 1, 1);
        labelFieldPane.add(favGenreField, 1, 3, 1, 1);
        labelFieldPane.add(favDir, 0, 4, 1, 1);
        labelFieldPane.add(favDirField, 1, 4, 1, 1);
        labelFieldPane.add(favActor, 0, 5, 1, 1);
        labelFieldPane.add(favActorField, 1, 5, 1, 1);
		
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
		
		//TODO update
		userValues[0] = "";
		userValues[1] = "";
		return userValues;
	}

}
