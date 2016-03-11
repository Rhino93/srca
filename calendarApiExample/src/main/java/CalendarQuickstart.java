import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;
//import com.mysql.jdbc.Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.*;
import java.util.Arrays;
import java.util.List;

import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;

public class CalendarQuickstart {
    /** Application name. */
    private static final String APPLICATION_NAME =
        "Google Calendar API Java Quickstart";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
        System.getProperty("user.home"), ".credentials/calendar-java-quickstart.json");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/calendar-java-quickstart.json
     */
    private static final List<String> SCOPES =
        Arrays.asList(CalendarScopes.CALENDAR);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
            CalendarQuickstart.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public static com.google.api.services.calendar.Calendar
        getCalendarService() throws IOException {
        Credential credential = authorize();
        return new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException {
        // Build a new authorized API client service.
        // Note: Do not confuse this class with the
        //   com.google.api.services.calendar.model.Calendar class.
        com.google.api.services.calendar.Calendar service =
            getCalendarService();

        //create a new event here
        Event newEvent = new Event()
        		.setSummary("A New Test Event")
        		.setLocation("2200th ave chestnut, IL")
        		.setDescription("THIS IS A HEW TESTING EVENT RIGHT HERE");
        
        DateTime startDateTime = new DateTime(System.currentTimeMillis()+7200000);
        DateTime endDateTime = new DateTime(System.currentTimeMillis() + 7200000 + 7200000);
        EventDateTime newStart = new EventDateTime()
        		.setDateTime(startDateTime)
        		.setTimeZone("America/Chicago");
        
        EventDateTime newEnd = new EventDateTime()
        		.setDateTime(endDateTime)
        		.setTimeZone("America/Chicago");
        
        newEvent.setStart(newStart);
        newEvent.setEnd(newEnd);
        
        String calendarID = "primary";
        //newEvent = service.events().insert(calendarID, newEvent).execute();
        System.out.printf("Event created: %s\n", newEvent.getHtmlLink());
        //end creating new event
        
        
        
        
        //test calendar list here
        String pageToken = null;
        do {
          CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
          List<CalendarListEntry> items = calendarList.getItems();

          for (CalendarListEntry calendarListEntry : items) {
            System.out.println(calendarListEntry.getSummary());
          }
          pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);
        //done
        
        
        //test event.watch
        
        
        
        
        
        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(0);
        Events events = service.events().list("primary")
            .setMaxResults(30)
            .setTimeMin(now)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute();
        List<Event> items = events.getItems();
        if (items.size() == 0) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s, %s, (%s)\n", event.getSummary(), event.getId(), start);
            }
        }
        
        //write the event name to a database
        System.out.println("Loading driver...");
        try{
        	Class.forName("com.mysql.jdbc.Driver");
        	System.out.println("Driver loaded!");
        }catch(ClassNotFoundException e){
        	throw new IllegalStateException("Cannot find the driver in the classpath", e);
        }
        
        String url = "jdbc:mysql://localhost:3306/rcdb";
        String username = "JavaAdmin";
        String password = "admin";
        
        System.out.println("Connecting to database...");
        Connection connection;
        try{
        	connection = DriverManager.getConnection(url, username, password);
        	System.out.println("Database connected!");
        	
        	Statement statement = connection.createStatement();
        	
        	//database connection was successful, now iterate over that same list again
            if (items.size() == 0) {
                System.out.println("No events found to add to the database.");
            } else {
                System.out.println("Inserting events into database...");
                for (Event event : items) {
                    
                	
                	//'INSERT INTO IGNORE' tries to add the entry, but ignores any errors if it fails - including from duplicated keys.
                	String query = "INSERT IGNORE INTO events " + 
        					"VALUES('" + event.getId() + "', '" + event.getSummary() + "'); ";
                	
                	
                    System.out.printf("Executing sql query: %s\n", query);
                    statement.executeUpdate(query); 
                }
                System.out.println("Done!");
            }
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        }catch(SQLException e){
        	throw new IllegalStateException("Could not connect to database!", e);
        }
        
        
        
        
        
        
        
    }

}