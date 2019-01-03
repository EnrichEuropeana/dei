package com.example.dei.controllers;

import com.example.dei.constans.EuropeanaKeys;
import com.example.dei.helpers.ResponseBuilder;
import com.example.dei.helpers.ParameterStringBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@RestController
public class EuropeanaConnectionController {

    @RequestMapping(value = "/test")
    public void getRequest() {
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("wskey", EuropeanaKeys.key);
            parameters.put("query", "*");
            parameters.put("qf", "house");

            URL url = new URL(EuropeanaKeys.apiUrl + ParameterStringBuilder.getParamsString(parameters));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            String response = ResponseBuilder.getResponse(connection);
            System.out.println(response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
