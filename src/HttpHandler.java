import java.io.*;
import java.net.Socket;
import java.util.List;

public class HttpHandler
{
    private Socket clientSocket;
    private Socket serverSocket;

    private BufferedReader serverReader;
    private BufferedWriter clientWriter;

    private File file;
    private List<File> cachedFiles;
    private List<String> words;

    private String headers;
    private String response;

    public HttpHandler(Socket serverSocket, Socket clientSocket, File file, List<File> cachedFiles, List<String> words)
    {
        this.clientSocket = clientSocket;
        this.serverSocket = serverSocket;

        try
        {
            this.serverReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            this.clientWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }

        this.file = file;
        this.cachedFiles = cachedFiles;
        this.words = words;

        //TODO wczytaj wszystko do jednego stringa

        initHeaders();
        initResponse();

        HTMLParser htmlParser = new HTMLParser(words);
        System.out.println("response przed:\n" + response);
        response = htmlParser.parseDangerousWords(response);
        System.out.println("response po:\n" + response);
    }

    public void sendData()
    {
        send();
//        cachePage();
    }

    private void cachePage()
    {
        try
        {
            if (file.createNewFile())
            {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(response);
                fileWriter.close();
                cachedFiles.add(file);
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    private void send()
    {
        try
        {
            clientWriter.write(headers);
            clientWriter.write("\r\n");
            clientWriter.flush();
            clientWriter.write(response);
            clientWriter.write("\n\r");
            clientWriter.flush();
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public String getHeaders()
    {
        return headers;
    }

    public String getResponse()
    {
        return response;
    }

    private void initHeaders()
    {
        //TODO przeczytaj tutaj readlinem
        StringBuilder headersBulider = new StringBuilder();
        try
        {
            String line;
            while (!(line = serverReader.readLine()).equals(""))
            {
                headersBulider.append(line);
                headersBulider.append("\n");
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        headers = headersBulider.toString();
    }

    private void initResponse()
    {
        //TODO tutaj zostaw
        int character;
        StringBuilder responseBuilder = new StringBuilder();
        try
        {
            while ((character = serverReader.read()) > 0)
            {
                responseBuilder.append((char) character);
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        response = responseBuilder.toString();
    }
}
