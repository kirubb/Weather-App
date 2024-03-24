import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Scanner;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class WeatherApp {

    public static JSONObject getWeatherData(String locationName){

    JSONArray locationData = getLocationData(locationName);

    JSONObject location = (JSONObject) locationData.get(0);
    double latitude = (double) location.get("latitude");
    double longitude = (double) location.get("longitude");

    String urlString = "https://api.open-meteo.com/v1/forecast?" +
            "latitude=" + latitude + "&longitude=" + longitude +
            "&hourly=temperature_2m,relativehumidity_2m,weathercode,windspeed_10m&timezone=America%2FLos_Angeles";

    try {
        //call pai and get response
        HttpURLConnection conn = fetchApiResponse(urlString);

        if (conn.getResponseCode() != 200) {
            System.out.println("Error : Could not connect to API");
            return null;
        }

        StringBuilder resultJson = new StringBuilder();
        Scanner scanner = new Scanner(conn.getInputStream());
        while (scanner.hasNext()) {
            resultJson.append(scanner.nextLine());

        }
        scanner.close();

        conn.disconnect();

        JSONParser parser = new JSONParser();
        JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

        JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");

        JSONArray time =(JSONArray) hourly.get("time");
        int index = findIndexOfCurrentTime(time);


        JSONArray temperatureData =(JSONArray) hourly.get("temperature_2m");
        double temperature = (double) temperatureData.get(index);

        JSONArray weathercode = (JSONArray) hourly.get("weathercode");
        String weatherCondition = convertWeatherCode((long) weathercode.get(index));

        JSONArray relativeHumidity = (JSONArray) hourly.get("relativehumidity_2m");
        long humidity  = (long) relativeHumidity.get(index);

        JSONArray windspeedData = (JSONArray) hourly.get("windspeed_10m");
        double windspeed = (double) windspeedData.get(index);

        JSONObject weatherData = new JSONObject();
        weatherData.put("temperature", temperature);
        weatherData.put("weather_condition", weatherCondition);
        weatherData.put("humidity", humidity);
        weatherData.put("windspeed",windspeed);

        return weatherData;



    }catch(Exception e) {
        e.printStackTrace();
    }





    return null;
}
// retrieves geolocation coordinates for the given location name
public static JSONArray getLocationData(String locationName) {

        //replace whitespace with + to adhere to api call rules
    locationName = locationName.replaceAll(" ","+");

    //build api url with location parameter

    String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
            locationName + "&count=10&language=en&format=json";

    try{
        //call api and get response
        HttpURLConnection conn = fetchApiResponse(urlString);

        //check response status

        //200 means successful connection

        if(conn.getResponseCode()!=200){
            System.out.println("Error : could not connect to API");
            return null;
        }else{
            //store api results
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());

            //read and store the json data results in our string builder

            while(scanner.hasNext()){
                resultJson.append(scanner.nextLine());
            }

            //close scanner

            scanner.close();

            //close url connection

            conn.disconnect();

            //parse the json string into a json object
            JSONParser parser = new JSONParser();
            JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            //get list of location data the api generated from the location name

            JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
            return locationData;
            // we get data in an array which we then store a json array
        }



    }catch(Exception e) {
        e.printStackTrace();
    }
    return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString){
        try {
            //attempt to create a connection
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();


            // set request method to get

            conn.setRequestMethod("GET");

            //CONNECT TO OUR API
            conn.connect();
            return conn;
        }catch(IOException e) {
            e.printStackTrace();
        }


        //couldn't find location
            return null;




    }

    private static int findIndexOfCurrentTime(JSONArray timeList){
        String currentTime = getCurrentTime();

        for(int i=0; i<timeList.size();i++){
            String time = (String) timeList.get(i);
            if(time.equalsIgnoreCase(currentTime)){
                return i;
            }
        }

    return 0;
    }

 public static String getCurrentTime(){
        LocalDateTime currentDateTime = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;

    }

    private static String convertWeatherCode(long weathercode){
        String weatherCondition ="";
        if (weathercode==0L){
            weatherCondition = "Clear";
        }else if (weathercode <=3L && weathercode > 0L){

            weatherCondition="Cloudy";

        }else if ((weathercode >= 51L && weathercode <=67L)|| (weathercode >=80L && weathercode <= 99L)){
            weatherCondition = "Rain";
        }else if (weathercode >=71L && weathercode <= 77L){
            weatherCondition ="Snow";
        }

        return weatherCondition;
    }
}


