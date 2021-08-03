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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SearchRecords {
	
	/**
	 * 
	 * @param conn connection to the mySQL database
	 * @param recordType the type of search (Genre, Actors, Directors)
	 * @param userSearch search text added by user
	 * @return array of string with the select records ID and display name
	 */
	public static String[] display(Connection conn, String recordType, String userSearch) {
		//set up window
		Stage window = new Stage();
		window.setMinWidth(200);
		window.setMaxWidth(500);
		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle("Search " + recordType);
		
		//declare misc variables
		String[] searchResults = new String[2]; //string that is returned from function
		boolean genreSearch = false; //boolean for if we are searching genres
		String query = "";
		
		//set up table, columns, and data
		TableView<SearchResult> searchTable = new TableView<SearchResult>();
		ObservableList<SearchResult> searchData = FXCollections.observableArrayList();
		
		TableColumn<SearchResult, String> recName = new TableColumn<SearchResult, String>(recordType);
		TableColumn<SearchResult, String> recDesc = new TableColumn<SearchResult, String>("Description");
		recName.setCellValueFactory(new PropertyValueFactory<SearchResult, String>("recordName"));
		recDesc.setCellValueFactory(new PropertyValueFactory<SearchResult, String>("recordDesc"));
		
		//set up scrollpane for the grid
		ScrollPane searchGridScroll = new ScrollPane();
		searchTable.setEditable(false);
		searchGridScroll.setContent(searchTable);
		
		//select search column based on the table being searched
		String searchCol;
		String recIdCol;
		
		//set some variable based on record type
		if (recordType.compareTo("Genres") == 0) {
			//searching genre
			genreSearch = true;
			//set database column names
			searchCol = "Genre";
			recIdCol= "Genre";
			//makes space for additional description column
			recName.prefWidthProperty().bind(searchTable.widthProperty().multiply(.1));
			recDesc.prefWidthProperty().bind(searchTable.widthProperty().multiply(.9));
			searchTable.setPrefWidth(1000);
		} else if (recordType.compareTo("Actors") == 0) {
			//searching actors
			//set database column names
			searchCol = "Act_name";
			recIdCol = "Act_id";
		} else if (recordType.compareTo("Directors") == 0) {
			//searching directors
			//set database column names
			searchCol = "Dir_name";
			recIdCol = "Dir_id";
		} else {
			//if we didn't hit a know search type set column to null
			searchCol = "";
			recIdCol = "";
		}
		
		//split the user string by spaces
		String[] userQuery = userSearch.split("\\s+");
		
		//check the number of substrings
		if (userQuery.length == 0) {
			//if there are not any substrings we have a 
			query = "SELECT * FROM " + recordType + ";";
		} else {
			//if there are multiple search strings create query with multiple where searches
			String queryLine = "SELECT * FROM " + recordType + " WHERE ";
			query = queryLine;
			for (int i = 0; i < userQuery.length; i++) {
				query = query + searchCol + " " + "LIKE '%" + userQuery[i] + "%'";
				if (i < userQuery.length - 1) {
					query = query + " AND ";
				} else {
					query = query + "ORDER BY " + searchCol + ";";
				}
			}
			
		}
		
		//connection to database
		try (Statement stmt = conn.createStatement()){
			//execute query
			ResultSet rs = stmt.executeQuery(query);
			
			//want to return a max of 100 records
			//initialize counter
			int count = 0;
			
			//read each line returned from the database
			while(rs.next()) {
				//get the record ID and display name from query
				String recordId = rs.getString(recIdCol);
				String recordName = rs.getString(searchCol);
				String recordDesc = "";
				if (genreSearch) {
					//if it is a genre search also get the genre description
					recordDesc = rs.getString("Description");
				}
				//add data to array
				searchData.add(new SearchResult(recordId, recordName,recordDesc));
				count++;
				if (count > 100) {
					break;
				}
			}
			
		} catch(SQLException e) {
			System.out.println(e);
		}
		
		//add data to grid
		searchTable.setItems(searchData);
		//add display name column to grid
		searchTable.getColumns().add(recName);
		
		if (genreSearch) {
			//if genre search, add description column to grid
			searchTable.getColumns().add(recDesc);
		}
		
		//auto select the first row in the grid
		searchTable.getSelectionModel().selectFirst();
		
		//create buttons to accept/cancel
		Button acceptBtn = new Button("Accept");
		Button cancelBtn = new Button("Cancel");
		
		//action for accept button
		acceptBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				//get the selected row
				SearchResult selectedRecord = searchTable.getSelectionModel().getSelectedItem();
				if(selectedRecord != null) {
					//set return values based on selected row
					searchResults[0] = selectedRecord.getRecordId();
					searchResults[1] = selectedRecord.getRecordName();
				} else {
					//if no row is selected return null
					searchResults[0] = "";
					searchResults[1] = "";
				}
				//close the window
				window.close();
			}
		});
		
		//action for cancel button
		cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				//close window and return null
				window.close();
				searchResults[0] = "";
				searchResults[1] = "";
			}
		});
		
		//set up hbox for the buttons 
		HBox buttonHbox = new HBox(10);
		buttonHbox.setAlignment(Pos.BASELINE_RIGHT);
		buttonHbox.getChildren().addAll(
			acceptBtn,
			cancelBtn
		);
		
		//set up popup layout
		VBox popLayout = new VBox(10);
		popLayout.setPadding(new Insets(10,10,10,10));
		popLayout.getChildren().addAll(
			searchGridScroll,
			buttonHbox
		);
		
		//create and set scene
		Scene scene = new Scene(popLayout);
		window.setScene(scene);
		window.showAndWait();
		
		return searchResults;
	}

	
	/**
	 * Class for the search result so we can display in a grid
	 *
	 */
	public static class SearchResult{
		//variables
		private SimpleStringProperty recordId;
		private SimpleStringProperty recordName;
		private SimpleStringProperty recordDesc;
		
		/**
		 * constructor for the search result
		 * @param rId record id
		 * @param rName record display name
		 * @param rDesc record description, only used for genre
		 */
		private SearchResult(String rId, String rName, String rDesc) {
			this.recordId = new SimpleStringProperty(rId);
			this.recordName = new SimpleStringProperty(rName);
			this.recordDesc = new SimpleStringProperty(rDesc);
		}
		
		/**
		 * get the record id
		 * @return record id
		 */
		public String getRecordId() {
			return this.recordId.get();
		}
		
		/**
		 * set the record id
		 * @param rId record id
		 */
		public void setRecordId(String rId) {
			this.recordId.set(rId);
		}
		
		/**
		 * get the record name
		 * @return record name
		 */
		public String getRecordName() {
			return this.recordName.get();
		}
		
		/**
		 * set the record name
		 * @param rName record name
		 */
		public void setRecordName(String rName) {
			this.recordName.set(rName);
		}
		
		/**
		 * get the genre description
		 * @return genre description
		 */
		public String getRecordDesc() {
			return this.recordDesc.get();
		}
		
		/**
		 * set the genre description
		 * @param rDesc genre description
		 */
		public void setRecordDesc(String rDesc) {
			this.recordDesc.set(rDesc);
		}
	}



}

