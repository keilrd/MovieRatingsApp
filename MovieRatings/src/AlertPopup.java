import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;

public class AlertPopup {
	public static void display(String title, String message, String btnText) {
		Stage window = new Stage();
		
		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle(title);
		window.setMinWidth(250);
		
		Label popMsg = new Label();
		popMsg.setText(message);
		
		Button popBtn = new Button(btnText);
		popBtn.setOnAction(e -> window.close());
		
		VBox popLayout = new VBox(10);
		popLayout.getChildren().addAll(
			popMsg,
			popBtn
		);
		popLayout.setAlignment(Pos.CENTER);
		
		Scene scene = new Scene(popLayout);
		window.setScene(scene);
		window.showAndWait();
	}

}
