


import twitter4j.*; 
import twitter4j.auth.OAuth2Token; 
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.TwitterStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;





import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class downloadTweet {
	
	private static final String CONSUMER_KEY	= "zoX1gMukbhnkQ273dzwO0vBpq"; 
	private static final String CONSUMER_SECRET = "lPFTj3ZESiN6RdIHkqC2witRbB6JWnfO9PRzScDVzAy5g8nmNR"; 
	private static int TWEETS_PER_QUERY = 10;
	private static int MAX_QUERIES = 5;
	private static String SEARCH_TERM;
	public static void main(String[] args) {
		//downloadTweet.SEARCH_TERM = args[0];
		//System.out.println(downloadTweet.SEARCH_TERM);
		// TODO Auto-generated method stub

		List<String> cities = downloadTweet.getCities();
		System.out.println(cities);
		System.exit(0);
		
		System.out.println("Donwloader Twet for CNR-IRPI");
		if(args.length > 0){
			if(args[0].equals("--help")){
				System.out.println("Donwloader Twet must have 3 or more params\n");
				System.out.println("The last two params is the numbers of query and the tweets per query");
				System.exit(0);
			}
			if(args.length >= 4){
				int k = 0;
				downloadTweet.SEARCH_TERM = args[0];
				int searchTermsParameters = args.length -2;			
				for (int i = 1; i < searchTermsParameters; i++){
					k = i;
					downloadTweet.SEARCH_TERM = downloadTweet.SEARCH_TERM + " " + args[i];
				}
				int queryNumbers = Integer.parseInt(args[k+1]);		
				int tweetsPerQuery = Integer.parseInt(args[k+2]);
				downloadTweet.MAX_QUERIES = queryNumbers;
				downloadTweet.TWEETS_PER_QUERY = tweetsPerQuery;
			}
			else if(args.length == 3){
				downloadTweet.SEARCH_TERM = args[0];
				int queryNumbers = Integer.parseInt(args[1]);		
				int tweetsPerQuery = Integer.parseInt(args[2]);
				downloadTweet.MAX_QUERIES = queryNumbers;
				downloadTweet.TWEETS_PER_QUERY = tweetsPerQuery;
			}
			else{
				System.out.println("Error! DownloadTweet must have specific params, type --help for instructions");
				System.exit(0);
			}
		}else{
			System.out.println("Error! DownloadTweet must have specific params, type --help for instructions");
			System.exit(0);
		}
			
		int	totalTweets = 0;
		long maxID = -1;
		Twitter twitter = getTwitter();
		File cinguetii = new File("cinguettii.txt'");
		FileOutputStream serializable = null;
		
		try {
			Map<String, RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus("search");
			RateLimitStatus searchTweetsRateLimit = rateLimitStatus.get("/search/tweets"); 
			
			/*System.out.printf("You have %d calls remaining out of %d, Limit resets in %d seconds\n", 
					searchTweetsRateLimit.getRemaining(), 
					searchTweetsRateLimit.getLimit(), 
					searchTweetsRateLimit.getSecondsUntilReset()); */
			for (int queryNumber=0;queryNumber < MAX_QUERIES; queryNumber++){
				//System.out.printf("\n\n!!! Starting loop %d\n\n", queryNumber);
				
				if (searchTweetsRateLimit.getRemaining() == 0){
					//System.out.printf("!!! Sleeping for %d seconds due to rate limits\n", searchTweetsRateLimit.getSecondsUntilReset());
					Thread.sleep((searchTweetsRateLimit.getSecondsUntilReset()+2) * 1000l); 
				}
				Query q = new Query(SEARCH_TERM);	
				q.setCount(TWEETS_PER_QUERY);
				q.setLang("it");
				
				
				if (maxID != -1) {
					q.setMaxId(maxID - 1); 
				}	
				
				QueryResult r = twitter.search(q);
				
				if (r.getTweets().size() == 0) {
					break;
				}
				
				Connection c = null;
			      Statement stmt = null;
				
				for (Status s: r.getTweets()){
					
					totalTweets++;
					if (maxID == -1 || s.getId() < maxID){
						maxID = s.getId(); 
					}
					
					
					//TODO FILTRI CI SONO GIA TUTTI I METODI 
					
					
					/**
					 * a rude filter, do not print if the tweet is a REtweet
					 * print geoLocation if there is
					 */
					if(s.isRetweeted() == false){
						
						System.out.println("User: "+s.getUser().getScreenName());
						System.out.println("Created at: "+s.getCreatedAt().toString());
						System.out.println("Said: "+s.getText().replaceAll("http"+"*"+" ", " "));

						

						if(s.getGeoLocation() != null){
							System.out.println("Latitude: "+s.getGeoLocation().getLatitude());
							System.out.println("Longitude: "+s.getGeoLocation().getLongitude());
						}
						
						
						String text = downloadTweet.validateText(s.getText());
						
						      try {
						         Class.forName("org.postgresql.Driver");
						         c = DriverManager
						            .getConnection("jdbc:postgresql://localhost:5432/cnrtweet", "postgres", "123456");
						         c.setAutoCommit(false);
						         System.out.println("Opened database successfully");
						         stmt = c.createStatement(); 
						         
						         String sql = "INSERT INTO provacnr (tweetuser,tweettext,tweettime)"
						               + "VALUES ('"+s.getUser().getScreenName()+"', '"+text+"','{"+s.getCreatedAt().toString()+"}S');";
						         stmt.executeUpdate(sql);
	
						         stmt.close();
						         c.commit();
						         c.close();
						      } catch (Exception e){
						    	  System.err.println( e.getClass().getName()+": "+ e.getMessage() );
						    	  System.exit(0);
						         
						      }
							
						
						
						/*
						System.out.printf("At %s, @%-20s said: %s\n",
								s.getCreatedAt().toString(),
								s.getUser().getScreenName(), 
								cleanText(s.getText()));
						System.out.println(args[0]);
						System.out.println(downloadTweet.SEARCH_TERM);
						*/
						ArrayList <String> tweets = new ArrayList <String> (); 
						tweets.add(s.getText());
						/*
						try {
							serializable = new FileOutputStream(cinguetii, true);
						}catch(Exception e){
							System.out.println("file not found");
						}
						try {
							ObjectOutputStream servePerScrive = new ObjectOutputStream(serializable);
							servePerScrive.writeObject(tweets);
							servePerScrive.flush();
							servePerScrive.close();
							FileInputStream apriFlusso = new FileInputStream(cinguetii);
							ObjectInputStream servePerLegge = new ObjectInputStream(apriFlusso);
							ArrayList <String> arrayLeggi = new ArrayList <>(); 
							arrayLeggi = (ArrayList <String>) servePerLegge.readObject();
							System.out.println("ecco array leggi");
							System.out.println(arrayLeggi);
						}catch(InvalidClassException e){
							System.out.println("a");
							
						}catch(Exception e){
							System.out.println("Error");
						}
						*/
											
						System.out.println(totalTweets);
										
					}
				}
				searchTweetsRateLimit = r.getRateLimitStatus();	
			}
		}
		catch (Exception e){
			System.out.println("That didn't work well...wonder why?"); 
			e.printStackTrace(); 
		}
	}
	
	public static String cleanText(String text){ 
		text = text.replace("\n", "\\n"); 
		text = text.replace("\t", "\\t"); 
		return text;
	}
	
	
	public static OAuth2Token getOAuth2Token() {
		OAuth2Token token = null;
		ConfigurationBuilder cb;
		cb = new ConfigurationBuilder();
		cb.setApplicationOnlyAuthEnabled(true);
		cb.setOAuthConsumerKey(CONSUMER_KEY).setOAuthConsumerSecret(CONSUMER_SECRET);
		try {
			token = new TwitterFactory(cb.build()).getInstance().getOAuth2Token();
		} catch (Exception e) {
			System.out.println("Could not get OAuth2 token");
			e.printStackTrace();
			System.exit(0);
		}
		return token;
	}
	public static Twitter getTwitter() { 
		OAuth2Token token; 
		token = getOAuth2Token(); 
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setApplicationOnlyAuthEnabled(true); 
		cb.setOAuthConsumerKey(CONSUMER_KEY); 
		cb.setOAuthConsumerSecret(CONSUMER_SECRET);
		cb.setOAuth2TokenType(token.getTokenType());
		cb.setOAuth2AccessToken(token.getAccessToken()); 
		return new TwitterFactory(cb.build()).getInstance(); 
	}
	
	public static String validateText(String tweetText){
		
		
		String text = tweetText.toUpperCase();
		text = text.replaceAll("\"","").replaceAll("\'", "");
		String[] parts = text.split(" ");
		String cleanText = "" ;
		for (int i = 0; i < parts.length; i++){
			if (parts[i].contains("HTTP")){
				parts[i] = null;
			}else{
				cleanText = cleanText + " " + parts[i] + " ";
			}			
		}	
		return cleanText;
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> getCities(){
		JSONParser parser = new JSONParser();
		List<String> cities = new Vector<String>();

		try {
			Object jsonObject = parser.parse(new FileReader("C:\\Users\\giulio\\Desktop\\CountriesToCitiesJSON\\countriesToCities.json"));

			JSONObject citiesJson = (JSONObject)  jsonObject;
			cities.addAll(citiesJson.values());
			

		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		
	
	return cities;
	
	}
}
