import java.sql.Connection;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DatabaseSettings {

	public static String[] display() {
		
		String[] dbSettings = new String[4];
		//set up the window as modal popup
		Stage window = new Stage();
		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle("Database Connection Settings");
		
		Label hostLab = new Label("Host:");
		Label portLab = new Label("Port:");
		Label dbUserLab = new Label("Database Username:");
		Label dbPassLab = new Label("Database Password:");
		
		TextField hostVal = new TextField();
		hostVal.setText("//localhost");
		TextField portVal = new TextField();
		portVal.setText("3306");
		TextField dbUserVal = new TextField();
		dbUserVal.setText("root");
		TextField dbPassVal = new TextField();
		dbPassVal.setText("12345");
		
		GridPane popLayout = new GridPane();
		popLayout.setHgap(10);
		popLayout.setVgap(10);
		
		Button acceptBtn = new Button("Accept");
		
		popLayout.add(hostLab, 0, 0, 1, 1);
		popLayout.add(hostVal, 1, 0, 1, 1);
		popLayout.add(portLab, 0, 1, 1, 1);
		popLayout.add(portVal, 1, 1, 1, 1);
		popLayout.add(dbUserLab, 0, 2, 1, 1);
		popLayout.add(dbUserVal, 1, 2, 1, 1);
		popLayout.add(dbPassLab, 0, 3, 1, 1);
		popLayout.add(dbPassVal, 1, 3, 1, 1);
		popLayout.add(acceptBtn, 1, 4, 1, 1);
		
		acceptBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				//close window and set return values
				window.close();
				
				dbSettings[0] = hostVal.getText();
				dbSettings[1] = portVal.getText();
				dbSettings[2] = dbUserVal.getText();
				dbSettings[3] = dbPassVal.getText();
			}
		});
		
		Scene scene = new Scene(popLayout);
		window.setScene(scene);
		window.showAndWait();
		
		//return values
		return dbSettings;
		
	}
}
