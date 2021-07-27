import java.util.ArrayList;
import java.util.List;

public class Movie {
	
	int mId;
	String mName;
	int year;
	double critRate;
	double audRate;
	int audCount;
	String director;
	List<String> actors;
	
	
	private Movie(int mId, String mName, int year, double critRate, double audRate, int audCount, String director, List<String> actors) {
		this.mId = mId;
		this.mName = mName;
		this.year = year;
		this.critRate = critRate;
		this.audRate = audRate;
		this.audCount = audCount;
		this.director = director;
		this.actors = actors;
	}
}
