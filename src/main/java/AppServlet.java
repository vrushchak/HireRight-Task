import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import pojo.Geo;
import pojo.Weather;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/app")
public class AppServlet extends HttpServlet {

    private String jsonPath = "http://localhost:8080/data.json";

    private final static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String zipInput = request.getParameter("zipInput");
        String geo = searchDataShort(zipInput, "zip");
        printOnPage(geo, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String city = request.getParameter("city");
        String weather = searchDataShort(city, "weather");
        printOnPage(weather, response);
    }

    private void printOnPage(String type, HttpServletResponse response) throws IOException {
        if (type != null) {
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().println(type);
            response.setStatus(200);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String searchDataShort(String request, String searchType) {
        String result = null;

        if (request == null || request.equals("")) {
            log.log(Level.WARNING, "Empty request");
            return result;
        }

        // StreamingAPI for fast iterate with big data
        try (JsonReader jsonReader = new JsonReader(
                new InputStreamReader(
                        new URL(jsonPath).openStream()))) {

            Gson gson = new GsonBuilder().create();

            jsonReader.beginArray();

            while (jsonReader.hasNext()) {

                if (searchType.equals("weather")) {

                    Weather weather = gson.fromJson(jsonReader, Weather.class);

                    System.out.println(weather.getCity());

                    if (weather.getCity() != null && weather.getCity().equals(request)) {
                        result = gson.toJson(weather);
                        log.info("Found a record by request: " + request);
                        while (jsonReader.hasNext()) {
                            jsonReader.skipValue();
                        }
                    }

                } else if (searchType.equals("zip")) {

                    Geo geo = gson.fromJson(jsonReader, Geo.class);

                    if (geo.getZip() == Integer.parseInt(request.trim())) {
                        result = gson.toJson(geo);
                        log.info("Found a record by request: " + request);
                        while (jsonReader.hasNext()) {
                            jsonReader.skipValue();
                        }
                    }

                }

            }
            jsonReader.endArray();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.log(Level.WARNING, "Unsupported Encoding Exception. Request: " + request);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.log(Level.WARNING, "File not found. File: " + jsonPath);
        } catch (IOException e) {
            e.printStackTrace();
            log.log(Level.WARNING, "Error. Request: " + request);
        }

        if (result == null) {
            log.log(Level.WARNING, "Result didn't find. Request: " + request);
        }

        return result;
    }
}
