package com.example.dei.helpers;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;

public class ResponseBuilder {

    public static String getResponse(HttpURLConnection connection) throws IOException {

        StringBuilder fullResponseBuilder = new StringBuilder();
        int status = connection.getResponseCode();
        System.out.println("status: " + status);
        Reader streamReader = null;

        if (connection.getResponseCode() > 299)
            streamReader = new InputStreamReader(connection.getErrorStream());
        else
            streamReader = new InputStreamReader(connection.getInputStream());

        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        fullResponseBuilder.append("Response: ").append(content);
        connection.disconnect();

        return fullResponseBuilder.toString();
    }
}
