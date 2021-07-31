import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
	public static String[] display(Connection conn, String recordType, String userSearch) {
		Stage window = new Stage();
		window.setMinWidth(200);
		window.setMaxWidth(500);
		
		String[] searchResults = new String[2];
		
		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle("Search " + recordType);
		
		TableView<SearchResult> searchTable = new TableView<SearchResult>();
		ObservableList<SearchResult> searchData = FXCollections.observableArrayList();
		
		ScrollPane searchGridScroll = new ScrollPane();
		searchTable.setEditable(false);
		searchGridScroll.setContent(searchTable);
		TableColumn<SearchResult, String> recName = new TableColumn<SearchResult, String>("Display Name");
		recName.setCellValueFactory(new PropertyValueFactory<SearchResult, String>("recordName"));
		
		//select search column based on the table being searched
		String searchCol;
		String recIdCol;
		
		if (recordType.compareTo("Genres") == 0) {
			searchCol = "Genre";
			recIdCol= "Genre";
		} else if (recordType.compareTo("Actors") == 0) {
			searchCol = "Act_name";
			recIdCol = "Act_id";
		} else if (recordType.compareTo("Directors") == 0) {
			searchCol = "Dir_name";
			recIdCol = "Dir_id";
		} else {
			searchCol = "";
			recIdCol = "";
		}
		
		String query = "";
		
		String[] userQuery = userSearch.split("\\s+");
		
		if (userQuery.length == 0) {
			query = "SELECT * FROM " + recordType;
		} else {
			String queryLine = "SELECT * FROM " + recordType + " WHERE " + searchCol + " ";
			for (int i = 0; i < userQuery.length; i++) {
				query = query + queryLine + "LIKE '%" + userQuery[i] + "%'";
				if (i < userQuery.length - 1) {
					query = query + " INTERSECT ";
				} else {
					query = query + ";";
				}
			}
			
		}
		
		try (Statement stmt = conn.createStatement()){
			//execute query
			ResultSet rs = stmt.executeQuery(query);
			
			int count = 0;
			
			while(rs.next()) {
				String recordId = rs.getString(recIdCol);
				String recordName = rs.getString(searchCol);
				searchData.add(new SearchResult(recordId, recordName));
				count++;
				if (count > 100) {
					break;
				}
			}
			
		} catch(SQLException e) {
			System.out.println(e);
		}
		
		searchTable.setItems(searchData);
		searchTable.getColumns().addAll(recName);
		searchTable.getSelectionModel().selectFirst();
		
		Button acceptBtn = new Button("Accept");
		Button cancelBtn = new Button("Cancel");
		
		acceptBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				
				SearchResult selectedRecord = searchTable.getSelectionModel().getSelectedItem();
				if(selectedRecord != null) {
					searchResults[0] = selectedRecord.getRecordId();
					searchResults[1] = selectedRecord.getRecordName();
				} else {
					searchResults[0] = "";
					searchResults[1] = "";
				}
				
				window.close();
			}
		});
		
		cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				window.close();
				searchResults[0] = "";
				searchResults[1] = "";
			}
		});
		
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

	public static class SearchResult{
		private SimpleStringProperty recordId;
		private SimpleStringProperty recordName;
		
		private SearchResult(String rId, String rName) {
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

