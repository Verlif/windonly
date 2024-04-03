package idea.verlif.windonly.utils;

import idea.verlif.windonly.WindonlyException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtil {

    private static final Pattern PATTERN_TITLE = Pattern.compile("<title>(.*?)</title>");

    public static String getTitleFromURL(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = PATTERN_TITLE.matcher(line);
                    if (matcher.find()) {
                        return matcher.group(1);
                    }
                }
            }
        } catch (IOException e) {
            throw new WindonlyException(e);
        }
        return urlString;
    }

}
