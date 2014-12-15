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
//import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class RottenTomatoes
{
	
	 	HashMap <String,String> API_methods = new HashMap <String,String>();
	    String base_api = "http://api.rottentomatoes.com";
	    String key = "tggs2dy337qu9jw28h8wdsg3";
	    
	 public RottenTomatoes()
	    {
	    	this.API_methods.put("Movie Reviews","/api/public/v1.0/movies/:id/reviews.json");
	    	this.API_methods.put("MovieSearch","/api/public/v1.0/movies.json");
	    }
	 
	public class Review 
	{
		public String critic;
		public String date;
		public String freshness;
		public String publication;
		public String quote;
	
		public String get_quote()
		{
			return quote;
		}
		
		public String get_date()
		{
			return date;
		}
	}
	
	public class Movie {		
		public String id;		
		public String title;
		public List<String> genres;	
		public String year;	
		public String mpaaRating;	
		//public Rating rating;		
		//public ReleaseDate releaseDate;		
		public String runtime;		
		public String criticsConsensus;		
		public String synopsis;	
		//public Posters posters;		
		//public Links links;		
		//public List<AbridgedCast> abridgedCast;
		public String getID() {
			return this.id;
		}
		
	}
   
    //method to get movieID from movies.
    public String getID(String movie_name) throws Exception
    {
    	String final_uri = base_api + API_methods.get("MovieSearch");
    	//?q=Gone+Girl&page_limit=1&page=1&apikey=tggs2dy337qu9jw28h8wdsg3
    	final_uri = final_uri + "?q=" + movie_name + "&page_limit=1&page=1&apikey=" + key;
    	System.out.println(final_uri);
    	HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    	HttpRequestFactory requestFactory =  HTTP_TRANSPORT.createRequestFactory();
    	GenericUrl url = new GenericUrl(final_uri);
    	HttpRequest request = requestFactory.buildGetRequest(url);
    	String response =  request.execute().parseAsString();
   	 	JsonParser parser = new JsonParser();
   	 	JsonObject jsonResponse = parser.parse(response).getAsJsonObject();
   	 	//System.out.println(jsonResponse);
   	 	JsonArray movies = jsonResponse.get("movies").getAsJsonArray();
   	 	Gson gson = new Gson();
   	 	JsonObject movie = movies.get(0).getAsJsonObject();
   	 	Movie movie_element =  gson.fromJson(movie, Movie.class);
   	 	//System.out.println(movie_element.getID());
   	 	return movie_element.getID();
    	
    	
    }
    
    //container class for getReviewsFromPage
    public class ReviewPackage
    {
    	List <String> results = new LinkedList<String>();
    	int total;
    }
    
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
   	 	//System.out.println(jsonResponse);
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
   	 	
   	 	
   	 	
   	 	
   	 	
   	 	/*System.out.println(result);
   	 	System.out.println(result.size());
   	 	for (int i = 0;i<result.size();i++)
   	 	{
   	 		System.out.println(result.get(i));
   	 	}*/
    }
    
    public void getAllReviews(String MovieName) throws Exception
    {
    	//gets the ID for movie.
    	String movie_id = getID(MovieName);
    	//after getting ID calls getReviewFromPage iteratively till all pages are covered.
    	ReviewPackage init = getReviewsFromPage(movie_id,50,1);
    	/*int number_ofCallsRequired = (int)Math.ceil((total/50));
   	 	List<String>temp = new LinkedList<String>();
   	 	for (int i = 1;i<=number_ofCallsRequired;i++)
   	 	{
   	 		temp = getReviews(movie_id,50,1+i);
   	 		result.addAll(temp);
   	 	}
   	 	retur*/
    	int total = init.total;
    	int number_ofCallsRequired = (int)Math.ceil((total/50));
    	ReviewPackage temp;
    	for (int i = 1;i<=number_ofCallsRequired;i++)
    	{
    		temp = getReviewsFromPage(movie_id,50,1+i);
    		init.results.addAll(temp.results);
    	}
    	System.out.println(init.results.size());
    	
    }
    
   
   
    



    public static void main(String[] args) throws Exception
    {
     
    	 /*HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    	 final JsonFactory JSON_FACTORY = new JacksonFactory();
    	 //String base_uri = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json?apikey=";
    	 //String key = "tggs2dy337qu9jw28h8wdsg3";
    	 //HashMap<String, String> params =new HashMap<String,String>();
    	 //int page_limit = 20;
    	 //int page = 1;
    	 //String country = "us";
    	 //params.put("page_limit", String.valueOf(page_limit));
    	 //params.put("page", String.valueOf(page));
    	 //params.put("country", country);
    	 //String  final_uri = base_uri + key;
    	 String final_uri = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json?page_limit=16&page=1&country=us&apikey=tggs2dy337qu9jw28h8wdsg3";
    	 HttpRequestFactory requestFactory =  HTTP_TRANSPORT.createRequestFactory();
    	 GenericUrl url = new GenericUrl(final_uri);
    	 //url.addQueryParams();
    	 HttpRequest request = requestFactory.buildGetRequest(url);
    	 String response =  request.execute().parseAsString();
    	 JsonParser parser = new JsonParser();
    	 JsonObject jsonResponse = parser.parse(response).getAsJsonObject();
    	 System.out.println(jsonResponse);
    	 /*System.out.println(jsonResponse.get("movies").getAsJsonArray());
    	 System.out.println(jsonResponse.get("movies").getAsJsonArray().get(0));
    	 Review review = new Gson().fromJson(jsonResponse,Review.class);
    	 review.getList();
    	 //System.out.println( request.execute().parseAsString());*/
    	
    	RottenTomatoes RT = new RottenTomatoes();
    	//RT.getID("gone girl");
    	RT.getAllReviews("gone girl");
    	 
    	 }
	}
        
    
    
/* 
 new HttpRequestInitializer() {
    		 public void initialize(HttpRequest request) {
    	            request.setParser(new JsonObjectParser(JSON_FACTORY));
    	          }
    	 } 
 */
