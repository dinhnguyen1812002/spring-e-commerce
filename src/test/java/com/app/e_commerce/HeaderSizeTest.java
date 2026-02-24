package com.app.e_commerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class HeaderSizeTest {

    @LocalServerPort
    private int port;

    @Test
    public void testLargeHeader() throws IOException {
        String urlString = "http://localhost:" + port + "/";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Try 10KB. Default is 8KB.
        StringBuilder largeValue = new StringBuilder();
        for (int i = 0; i < 10 * 1024; i++) {
            largeValue.append("a");
        }
        
        connection.setRequestProperty("X-Large-Header", largeValue.toString());
        
        int responseCode = connection.getResponseCode();
        System.out.println("Response status: " + responseCode);
        
        assertThat(responseCode).isNotEqualTo(400);
    }
}
