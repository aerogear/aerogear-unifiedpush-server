package org.jboss.aerogear.unifiedpush.rest.config;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by matzew on 12/5/17.
 */
public class Test {

    public static void main(String... args) throws URISyntaxException, MalformedURLException {

        String urlString = "hTtpS://localhost:443/auth";

        URL url = new URL(urlString);


        if (url.getPort() == url.getDefaultPort()) {

            String urlPort = ":"+url.getPort();

            System.out.println(url.toExternalForm().replace(urlPort, ""));
        }

  //      url.


        System.out.println("DA -> " + url);

        System.out.println("DA -> " + url.toExternalForm());



    }

}
