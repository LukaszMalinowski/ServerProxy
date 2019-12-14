import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
        return path;
    }
}
