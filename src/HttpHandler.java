import java.io.*;
import java.net.Socket;
import java.util.List;

public class HttpHandler
{

    private BufferedReader serverReader;
    private BufferedWriter clientWriter;

    private File file;
    private List<File> cachedFiles;

    private String headers;
    private String response;

    public HttpHandler(Socket serverSocket, Socket clientSocket, File file, List<File> cachedFiles, List<String> words)
    {
        try
        {
            this.serverReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            this.clientWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

        this.file = file;
        this.cachedFiles = cachedFiles;

        initHeaders();
        initResponse();

        HTMLParser htmlParser = new HTMLParser(words);
        response = htmlParser.parseDangerousWords(response);
    }

    public void sendData()
    {
        send();
        cachePage();
    }

    private void cachePage()
    {
        //Zapisujemy tresc do pliku
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
        //Wysylamy naglowki i cialo odpowiedzi do przegladarki
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

    private void initHeaders()
    {
        //Czytamy az do wystapienia pierwszej pustej linii i zapisujemy wszystkie naglowki do zmiennej
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
        //Czytamy reszte i zapisujemy body odpowiedzi
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
