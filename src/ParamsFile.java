import java.io.*;

public class ParamsFile
{
    private File file;
    private final int DEFAULT_PORT = 8080;

    ParamsFile(File file)
    {
        this.file = file;
    }

    int getProxyPort()
    {
        //Czytamy pierwsza linie pliku i wyciagamy numer portu
        int port;
        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String proxyPort = reader.readLine();
            proxyPort = proxyPort.substring(proxyPort.indexOf("=")+1);
            port = Integer.parseInt(proxyPort);
        }
        catch (Exception ex)
        {
            port = DEFAULT_PORT;
        }
        return port;
    }

    String[] getWords()
    {
        //Czytamy druga linie pliku i wyciagamy slowa
        String wordsLine = "";
        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            reader.readLine();
            wordsLine = reader.readLine();
            wordsLine = wordsLine.substring(wordsLine.indexOf("=") + 1);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return wordsLine.split(";");
    }

    String getCachePath()
    {
        //Czytamy trzecia linie pliku i wyciagamy lokalizacje plikow do cachowania
        String path = "";
        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            reader.readLine();
            reader.readLine();
            String pathLine = reader.readLine();
            path = pathLine.substring(pathLine.indexOf("\"") + 1, pathLine.length() - 1);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        File file = new File(path);

        if (!file.exists())
            file.mkdir();

        return path;
    }
}
