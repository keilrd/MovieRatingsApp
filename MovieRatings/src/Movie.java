import java.util.List;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Movie {
	
	
	int mId;
	//String mName;
	//int year;
	double critRate;
	double audRate;
	int audCount;
	//String director;
	List<String> actors;
	
	
	//private final SimpleIntegerProperty movieId;
	private final SimpleStringProperty movieName;
	private final SimpleIntegerProperty movieYear;
	private final SimpleStringProperty movieDir;
	private final SimpleStringProperty movieAct;
	
	
	Movie(int mId, String mName, int year, double critRate, double audRate, int audCount, String director, List<String> actors) {
		this.mId = mId;
		//this.mName = mName;
		this.movieName = new SimpleStringProperty(mName);
		//this.year = year;
		this.movieYear = new SimpleIntegerProperty(year);
		this.critRate = critRate;
		this.audRate = audRate;
		this.audCount = audCount;
		//this.director = director;
		this.movieDir = new SimpleStringProperty(director);
		this.actors = actors;
		this.movieAct = new SimpleStringProperty(makeActString(actors));
	}
	
	public String getMovieName() {
        return this.movieName.get();
    }
	
	public void setMovieName(String mName) {
		movieName.set(mName);
	}
	
	public int getMovieYear() {
		return this.movieYear.get();
	}
	
	public String getMovieDir() {
		return this.movieDir.get();
	}
	
	public String getMovieAct() {
		return this.movieAct.get();
	}
	
	public int getMovieID() {
	    return this.mId;
	}
	
	public double getMovieCriticRating() {
	    return this.critRate;
	}
	
	public double getMovieAudRating() {
	    return this.audRate;
	}
	
	public String makeActString(List<String> actors) {
		
		String actString = "";
		
		if (actors.size() > 0) {
		
			for (int i = 0; i < actors.size() - 1; i++) {
				actString = actString + actors.get(i) + ", ";
			}
			
			actString = actString + actors.get(actors.size() - 1);
		}
		
		return actString;
	}



}