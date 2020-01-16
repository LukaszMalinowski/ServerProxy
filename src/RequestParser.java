import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestParser
{
    private String request;
    private Pattern hostPattern = Pattern.compile("Host: (.*)");

    RequestParser(String request)
    {
        this.request = request;
    }

    public String getRequest()
    {
        return request;
    }

    public String getConnectionType()
    {
        return request.substring(0,request.indexOf(' '));
    }

    public String getHost()
    {
        String host = getHostWithPort();

        //sprawdzamy czy nazwa hosta posiada numer portu na końcu, jeśli tak to ucinamy
        if(host.contains(":"))
            host = host.substring(0, host.indexOf(':'));

        return host;
    }

    public String getFileName()
    {
        String fileName = request.substring(request.indexOf(' ') + 1,
                request.indexOf(' ', request.indexOf(' ') + 1));

        if(fileName.contains("http://"))
            fileName = fileName.substring(7);

        if(fileName.contains(":"))
            fileName = fileName.substring(0, fileName.indexOf(':'));

        fileName = fileName.replace('/', '_');

        return fileName;
    }

    public String getUrl()
    {
        String url = "";
        url = request.substring(0, request.indexOf('\n'));

        url = url.substring(url.indexOf(' ') + 1, url.lastIndexOf(' '));

        if(url.contains("http://"))
            url = url.substring(7);

        if(url.indexOf('/') != -1)
            url = url.substring(url.indexOf('/'));


        else
            url = "/";

        return url;
    }

    public int getPort()
    {
        String host = getHostWithPort();
        int port = 80;

        if(host.contains(":"))
            port = Integer.parseInt(host.substring(host.indexOf(':') + 1));

        return port;
    }

    private String getHostWithPort()
    {
        String host = "";
        Matcher hostMatcher = hostPattern.matcher(request);

        if(hostMatcher.find())
        {
            host = hostMatcher.group(1);
        }

        //ucinamy "http://" jesli jest
        if(host.contains("http://"))
            host = host.substring(7);

        return host;
    }

    public void parseUrl()
    {
        String firstline = request.substring(0, request.indexOf('\n'));

        firstline = firstline.replace(
                firstline.substring(firstline.indexOf(' ') + 1, firstline.lastIndexOf(' ')),
                this.getUrl());

        request = request.substring(request.indexOf('\n') + 1);

        request = firstline + "\n" + request;

        if(request.contains("Accept-Encoding:"))
        {
            String replaceLine = request.substring(
                    request.indexOf("Accept-Encoding:"),
                    request.indexOf("\n",request.indexOf("Accept-Encoding:")));

            request = request.replace(replaceLine,"Accept-Encoding: identity");
        }
    }

    public void setConnectionClose()
    {
        if(request.contains("Connection: keep-alive"))
            request = request.replace("Connection: keep-alive" , "Connection: close");
    }
}
