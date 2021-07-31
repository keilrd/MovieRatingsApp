import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SearchRecords {
	public static String[] display(String recordType, String userSearch) {
		Stage window = new Stage();
		window.setMinWidth(200);
		window.setMaxWidth(500);
		
		String[] searchResults = new String[2];
		
		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle("Search " + recordType);
		
		TableView<searchResult> searchTable = new TableView<searchResult>();
		ObservableList<searchResult> searchData = FXCollections.observableArrayList();
		
		ScrollPane searchGridScroll = new ScrollPane();
		searchTable.setEditable(false);
		searchGridScroll.setContent(searchTable);
		TableColumn<searchResult, String> recName = new TableColumn<searchResult, String>("Display Name");
		recName.setCellValueFactory(new PropertyValueFactory<searchResult, String>("recordName"));
		
		
		searchTable.setItems(searchData);
		searchTable.getColumns().addAll(recName);
		
		Button acceptBtn = new Button("Accept");
		Button cancelBtn = new Button("Cancel");
		
		HBox buttonHbox = new HBox(10);
		buttonHbox.setAlignment(Pos.BASELINE_RIGHT);
		buttonHbox.getChildren().addAll(
			acceptBtn,
			cancelBtn
		);
		
		VBox popLayout = new VBox(10);
		popLayout.setPadding(new Insets(10,10,10,10));
		popLayout.getChildren().addAll(
			searchGridScroll,
			buttonHbox
		);
		
		Scene scene = new Scene(popLayout);
		window.setScene(scene);
		window.showAndWait();
		
		System.out.printf("usersValues");
		return searchResults;
	}

	public class searchResult{
		private SimpleStringProperty recordId;
		private SimpleStringProperty recordName;
		
		private searchResult(String rId, String rName) {
			this.recordId = new SimpleStringProperty(rId);
			this.recordName = new SimpleStringProperty(rName);
		}
		
		public String getRecordId() {
			return this.recordId.get();
		}
		
		public void setRecordId(String rId) {
			this.recordId.set(rId);
		}
		
		public String getRecordName() {
			return this.recordName.get();
		}
		
		public void setRecordName(String rName) {
			this.recordName.set(rName);
		}
	}



}
