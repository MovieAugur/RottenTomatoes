import java.util.*;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.Key;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;



public class RottenTomatoes
{
		//stores urls for different calls.
	 	HashMap <String,String> API_methods = new HashMap <String,String>();
	 	//base api for evry call
	    String base_api = "http://api.rottentomatoes.com";
	    //api key
	    String key = "tggs2dy337qu9jw28h8wdsg3";
	    
	 public RottenTomatoes()
	    {
		 //initializes the hash table with urls.
	    	this.API_methods.put("Movie Reviews","/api/public/v1.0/movies/:id/reviews.json");
	    	this.API_methods.put("MovieSearch","/api/public/v1.0/movies.json");
	    }
	 
	public class Review 
	{
		//used for mapping json object to a a java class.
		public String critic;
		public String date;
		public String freshness;
		public String publication;
		//quote is same as review string.
		public String quote;
		//returns quote
		public String get_quote()
		{
			return quote;
		}
		//returns date
		public String get_date()
		{
			return date;
		}
	}
	
	public class Ratings
	{
		public String critics_rating;
		public int critics_score;
		public String audience_rating;
		public int audience_score;
	}
	
	public class Images
	{
		String thumbnail;
		String profile;
		String detailed;
		String original;
	}
	
	public class Movie {		
		public String id;		
		public String title;
		public List<String> genres;	
		public String year;	
		public String mpaaRating;	
		public Ratings ratings;			
		public String runtime;		
		public String criticsConsensus;		
		public String synopsis;	
		public Images posters;
		
		public String getID() {
			return this.id;
		}
		
	}
   
    /*method to get movieID from movies.
	  returns Movie object.*/
    public Movie get_movie_info(String movie_name) throws Exception
    {
    	//create the final url by appending key,page limit and page number.
    	String final_uri = base_api + API_methods.get("MovieSearch");
    	final_uri = final_uri + "?q=" + movie_name + "&page_limit=1&page=1&apikey=" + key;
    	//System.out.println(final_uri);
    	HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    	HttpRequestFactory requestFactory =  HTTP_TRANSPORT.createRequestFactory();
    	GenericUrl url = new GenericUrl(final_uri);
    	HttpRequest request = requestFactory.buildGetRequest(url);
    	String response =  request.execute().parseAsString();
   	 	JsonParser parser = new JsonParser();
   	 	JsonObject jsonResponse = parser.parse(response).getAsJsonObject();
   	 	JsonArray movies = jsonResponse.get("movies").getAsJsonArray();
   	 	Gson gson = new Gson();
   	 	JsonObject movie = movies.get(0).getAsJsonObject();
   	 	Movie movie_element =  gson.fromJson(movie, Movie.class);
   	 	return movie_element;
    	
    	
    }
    
    //container class for getReviewsFromPage
    public class ReviewPackage
    {
    	List <String> results = new LinkedList<String>();
    	int total;
    }
    /*Returns all the reviews from a single page wrapped in a class 
     * which also contains total number of reviews to be used to 
     * make subsequent calls to get all reviews. */
    public ReviewPackage getReviewsFromPage(String movie_id,int page_limit,int page) throws Exception
    {
    	String final_uri = base_api + API_methods.get("Movie Reviews");
    	final_uri = final_uri.replaceAll(":id", movie_id);
    	final_uri = final_uri+"?review_type=all&page_limit=" + String.valueOf(page_limit) 
    			+ "&page=" + String.valueOf(page) + "&country=us&apikey=" + key;
    	System.out.println(final_uri);
    	ReviewPackage pack = new ReviewPackage();
    	HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    	HttpRequestFactory requestFactory =  HTTP_TRANSPORT.createRequestFactory();
    	GenericUrl url = new GenericUrl(final_uri);
    	HttpRequest request = requestFactory.buildGetRequest(url);
    	String response =  request.execute().parseAsString();
   	 	JsonParser parser = new JsonParser();
   	 	JsonObject jsonResponse = parser.parse(response).getAsJsonObject();
   	 	pack.total = jsonResponse.get("total").getAsInt();
   	 	JsonArray review_array = jsonResponse.getAsJsonArray("reviews");
   	 	Gson gson = new Gson();
   	 	for (int i = 0; i < review_array.size(); i++)
   	 	{
			JsonObject review = review_array.get(i).getAsJsonObject();
			Review review_element =  gson.fromJson(review, Review.class);
			pack.results.add(review_element.get_quote());
		}
   	 	
   	 	return pack;
   	 	//number of calls to API to get all reviews, page has maximum limit of 50.
    }
    
    /*given movie name returns a list of all the reviews of the movie*/
    public List<String> getAllReviews(String MovieName) throws Exception
    {
    	//gets the ID for movie.
    	String movie_id = get_movie_info(MovieName).id;
    	//after getting ID calls getReviewFromPage iteratively till all pages are covered.
    	ReviewPackage init = getReviewsFromPage(movie_id,50,1);
    	int total = init.total;
    	int number_ofCallsRequired = (int)Math.ceil((total/50));
    	ReviewPackage temp;
    	for (int i = 1;i<=number_ofCallsRequired;i++)
    	{
    		temp = getReviewsFromPage(movie_id,50,1+i);
    		init.results.addAll(temp.results);
    	}
    	System.out.println(init.results.size());
    	return init.results;
    	
    }
    /*returns a class containing urls for the psoters*/
    public Ratings get_movie_rating(String movie_name) throws Exception
    { 
    	Ratings rating = get_movie_info(movie_name).ratings;
    	return rating;
    	
    }
    /*given movie name and destination file path retrieves the poster for 
     * the movie and stores it in the given location.*/
    public void get_movie_posters(String movie_name,String destination_file) throws Exception
    { 
    	Images posters = get_movie_info(movie_name).posters;
    	String poster_url = posters.detailed.replaceAll("_tmb", "_det");
    	//String destinationFile = "image.jpg";
    	URL url = new URL(poster_url);
    	InputStream is = url.openStream();
    	OutputStream os = new FileOutputStream(destination_file);
    	byte[] b = new byte[2048];
    	int length;
    	while ((length = is.read(b)) != -1) {
			os.write(b, 0, length);
		}
    	is.close();
		os.close();
    }
   
   /*contains example calls which were used to unit test functionality
    * can also be used as reference to make calls from external modules.*/ 
    public static void main(String[] args) throws Exception
    {
     
    	RottenTomatoes RT = new RottenTomatoes();
    	
    	/*RT.get_movie_posters("gone girl","image.jpg");*/
    	
    	/*Ratings rating = RT.get_movie_rating("gone girl");
    	System.out.println("audience score " + String.valueOf(rating.audience_score));
    	System.out.println("critics score " + String.valueOf(rating.critics_score));*/
    	
    	/*NLP analyser = new NLP();
    	int temp_sentiment;
    	String review;
    	//RT.getID("gone girl");
    	List <String> reviews = new LinkedList<String>();
    	reviews = RT.getAllReviews("dolphin tale 2");
    	for (int i = 0;i<reviews.size();i++)
    	{
    		review = reviews.get(i);
    		temp_sentiment = analyser.analyse(review);
    		System.out.println(reviews.get(i) + " ::: "+ String.valueOf(temp_sentiment));
    	}*/
    	 
	 }
	}
        
    
