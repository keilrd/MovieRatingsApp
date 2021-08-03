import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;

/**
 * Class to show a simple error message popup
 *
 */
public class AlertPopup {
	
	/**
	 * Displays a popup error message
	 * @param title of the popup window
	 * @param message message to be shown
	 * @param btnText title of the button
	 */
	public static void display(String title, String message, String btnText) {
		//set up window
		Stage window = new Stage();
		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle(title);
		window.setMinWidth(250);
		
		//create a new label
		Label popMsg = new Label();
		popMsg.setText(message);
		
		//button for the popup 
		Button popBtn = new Button(btnText);
		popBtn.setOnAction(e -> window.close());
		
		//layout for the popup
		VBox popLayout = new VBox(10);
		popLayout.getChildren().addAll(
			popMsg,
			popBtn
		);
		popLayout.setAlignment(Pos.CENTER);
		
		//setup scene
		Scene scene = new Scene(popLayout);
		window.setScene(scene);
		window.showAndWait();
	}

}
