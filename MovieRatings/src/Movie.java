import java.util.List;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Class for a movie. Stores information including the movie's ID in the database, the title, year it was released,
 * critic rating, audience rating, and total ratings left on the movie by users.
 * @author Jenny Krewer, Mohammad Islam, Ryan Keil
 */
public class Movie {


    int mId; // ID for the movie in the database
    double critRate; // the movie's critic rating out of 10
    double audRate; // the average audience rating left by users
    int audCount; // the number of ratings included in the average audRate
    List<String> actors; // List of actors in the movie

    private final SimpleStringProperty movieName; // title of the movie
    private final SimpleIntegerProperty movieYear; // year the movie was released
    private final SimpleStringProperty movieDir; // director of the movie
    private final SimpleStringProperty movieAct; // string of actors in the movie


    /**
     * Constructor to create a new movie object
     * @param mId ID for the movie in the database
     * @param mName title of the movie
     * @param year year the movie was released
     * @param critRate the movie's critic rating out of 10
     * @param audRate the number of ratings included in the average audRate
     * @param audCount the number of ratings included in the average audRate
     * @param director director of the movie
     * @param actors List of actors in the movie
     */
    Movie(int mId, String mName, int year, double critRate, double audRate, int audCount,
        String director, List<String> actors) {
        this.mId = mId;
        // this.mName = mName;
        this.movieName = new SimpleStringProperty(mName);
        // this.year = year;
        this.movieYear = new SimpleIntegerProperty(year);
        this.critRate = critRate;
        this.audRate = audRate;
        this.audCount = audCount;
        // this.director = director;
        this.movieDir = new SimpleStringProperty(director);
        this.actors = actors;
        this.movieAct = new SimpleStringProperty(makeActString(actors));
    }

    /**
     * Returns the movie's title
     * @return movie title
     */
    public String getMovieName() {
        return this.movieName.get();
    }

    /**
     * Sets the movie name
     * @param mName the title of the movie
     */
    public void setMovieName(String mName) {
        movieName.set(mName);
    }

    /**
     * Returns the year the movie was released
     * @return the year the movie was released
     */
    public int getMovieYear() {
        return this.movieYear.get();
    }

    /**
     * Returns the director of the movie
     * @return the director
     */
    public String getMovieDir() {
        return this.movieDir.get();
    }

    /**
     * Gets a string list of actors in the movie
     * @return string of actor names
     */
    public String getMovieAct() {
        return this.movieAct.get();
    }

    /** 
     * Returns the movie's ID in the database
     * @return movie ID
     */
    public int getMovieID() {
        return this.mId;
    }
    
    /**
     * Returns the critic rating out of 10 stars
     * @return critic rating
     */
    public double getMovieCriticRating() {
        return this.critRate;
    }

    /**
     * Returns the average audience rating out of 5 stars
     * @return average audience rating
     */
    public double getMovieAudRating() {
        return this.audRate;
    }

    /**
     * Returns the number of audience ratings left on the movie
     * @return count of audience ratings
     */
    public int getMovieAudCount() {
        return this.audCount;
    }

    /**
     * Updates the audience average rating and count when a user leaves a rating.
     * @param newRating The rating left by the user
     * @param oldRating The user's previous rating for the movie. Set to 0 if the user has
     * never left a rating on the movie before
     * @return True if the rounded audience rating changed, meaning we need to update the display
     */
    public boolean updateAudRatingandCount(double newRating, double oldRating) {
        
        double origRate = this.audRate;
        
        double tempRating = this.audRate * this.audCount;
        
        tempRating = tempRating + newRating - oldRating; // if user left a rating before, make sure we don't include it
        
        // only increment audience count if this is the first time the user has left a rating
        if (oldRating == 0) {
            this.audCount++;
        }
        
        tempRating = tempRating / this.audCount;

        tempRating = Math.round(tempRating * 10) / 10.0;
        
        this.audRate = tempRating;
        
        return (Math.round(tempRating) != Math.round(origRate)); // return whether rating to display changed
    }

    /**
     * Creates a concatenated string of actors in the movie to display from the List
     * of actors in the movie
     * @param actors List of actors in the movie
     * @return String of actors in the movie
     */
    public String makeActString(List<String> actors) {

        String actString = "";

        if (actors.size() > 0) {

            for (int i = 0; i < actors.size(); i++) {
                actString = actString + actors.get(i);
                
                if (i == 2 || i == actors.size() - 1) {
                    break;
                }
                
                actString += ", ";
            }
            
        }

        return actString;
    }

}
