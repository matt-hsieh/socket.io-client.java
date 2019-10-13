package io.socket.client;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Url {

    private static Pattern PATTERN_HTTP = Pattern.compile("^http|ws$");
    private static Pattern PATTERN_HTTPS = Pattern.compile("^(http|ws)s$");

    private Url() {}

    public static URL parse(String uri) throws URISyntaxException {
        return parse(new URI(uri));
    }

    public static URL parse(URI uri) {
        String protocol = uri.getScheme();
        if (protocol == null || !protocol.matches("^https?|wss?$")) {
            protocol = "https";
        }

        int port = uri.getPort();
        if (port == -1) {
            if (PATTERN_HTTP.matcher(protocol).matches()) {
                port = 80;
            } else if (PATTERN_HTTPS.matcher(protocol).matches()) {
                port = 443;
            }
        }

        String path = uri.getRawPath();
        if (path == null || path.length() == 0) {
            path = "/";
        }

        String userInfo = uri.getRawUserInfo();
        String query = uri.getRawQuery();
        String fragment = uri.getRawFragment();
        try {
            return new URL(protocol + "://"
                    + (userInfo != null ? userInfo + "@" : "")
                    + extractHost(uri)
                    + (port != -1 ? ":" + port : "")
                    + path
                    + (query != null ? "?" + query : "")
                    + (fragment != null ? "#" + fragment : ""));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String extractId(String url) throws MalformedURLException {
        return extractId(new URL(url));
    }

    public static String extractId(URL url) {
        String protocol = url.getProtocol();
        int port = url.getPort();
        if (port == -1) {
            if (PATTERN_HTTP.matcher(protocol).matches()) {
                port = 80;
            } else if (PATTERN_HTTPS.matcher(protocol).matches()) {
                port = 443;
            }
        }
        return protocol + "://" + url.getHost() + ":" + port;
    }

    private static String extractHost(URI uri)
    {
        // Extract the host part from the URI.
        String host = uri.getHost();

        if (host != null)
        {
            return host;
        }

        // According to https://github.com/TakahikoKawasaki/nv-websocket-client/issues/74, URI.getHost() method returns null in
        // the following environment when the host part of the URI is
        // a host name.
        //
        //   - Samsung Galaxy S3 + Android API 18
        //   - Samsung Galaxy S4 + Android API 21
        //
        // The following is a workaround for the issue.

        // Extract the host part from the authority part of the URI.
        host = extractHostFromAuthorityPart(uri.getRawAuthority());

        if (host != null)
        {
            return host;
        }

        // Extract the host part from the entire URI.
        return extractHostFromEntireUri(uri.toString());
    }


    private static String extractHostFromAuthorityPart(String authority)
    {
        // If the authority part is not available.
        if (authority == null)
        {
            // Hmm... This should not happen.
            return null;
        }

        // Parse the authority part. The expected format is "[id:password@]host[:port]".
        Matcher matcher = Pattern.compile("^(.*@)?([^:]+)(:\\d+)?$").matcher(authority);

        // If the authority part does not match the expected format.
        if (!matcher.matches())
        {
            // Hmm... This should not happen.
            return null;
        }

        // Return the host part.
        return matcher.group(2);
    }


    private static String extractHostFromEntireUri(String uri)
    {
        if (uri == null)
        {
            // Hmm... This should not happen.
            return null;
        }

        // Parse the URI. The expected format is "scheme://[id:password@]host[:port][...]".
        Matcher matcher = Pattern.compile("^\\w+://([^@/]*@)?([^:/]+)(:\\d+)?(/.*)?$").matcher(uri);

        // If the URI does not match the expected format.
        if (!matcher.matches())
        {
            // Hmm... This should not happen.
            return null;
        }

        // Return the host part.
        return matcher.group(2);
    }

}
