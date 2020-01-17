import java.io.*;
import java.net.Socket;
import java.util.List;

public class ProxyThread extends Thread
{
    private Socket clientSocket;

    private List<String> words;
    private List<File> cachedFiles;
    private String cachePath;

    private RequestParser requestParser;

    private BufferedReader clientReader;
    private BufferedWriter clientWriter;

    private final String CONNECT_TYPE = "CONNECT";

    ProxyThread(Socket clientSocket, List<String> words, String cachePath, List<File> cachedFiles)
    {
        this.clientSocket = clientSocket;

        this.words = words;
        this.cachePath = cachePath;
        this.cachedFiles = cachedFiles;

        try
        {
            clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        this.start();
    }

    @Override
    public void run()
    {
        StringBuilder requestBuilder = new StringBuilder();

        //Wczytujemy caly request do StringBuildera
        String line;
        try
        {
            while (!(line = clientReader.readLine()).equals(""))
            {
                requestBuilder.append(line);
                requestBuilder.append("\n");
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

        requestParser = new RequestParser(requestBuilder.toString());

        //Tworzymy zmienna typu File przed parsowaniem requesta
        File file = new File(cachePath + "/" + requestParser.getFileName());

        requestParser.parseUrl();

        if(requestParser.getConnectionType().equals(CONNECT_TYPE))
            handleConnect();
        else
        {
            if(isCashed(file))
                handleCached(file);
            else
                handleNonCached(file);
        }
    }

    void handleConnect()
    {
        //wysyłam informacje do przeglądarki że mogę rozpocząć tunelowanie tcp
        String lineConnector = "HTTP/1.0 200 OK\r\n" +
                "\r\n";

        try
        {
            clientWriter.write(lineConnector);
            clientWriter.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        String host = requestParser.getHost();
        int port = requestParser.getPort();

        try
        {
            //Tworzymy polaczenie z serwerem docelowym i rozpoczynamy tunelowanie tcp
            Socket serverSocket = new Socket(host,port);
            new TCPTunnel(serverSocket, clientSocket).start();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

    }

    void handleNonCached(File file)
    {
        try
        {
            //Tworzymy polaczenie z serwerem docelowym i wysylamy mu zparsowany request z przegladarki
            Socket serverSocket = new Socket(requestParser.getHost(),requestParser.getPort());
            BufferedWriter serverWriter = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));

            serverWriter.write(requestParser.getRequest() + "\r\n");
            serverWriter.flush();

            //W konstruktorze klasy HttpHandler filtrujemy html, cachujemy strone i wysylamy dane z powrotem do przegladarki
            HttpHandler httpHandler = new HttpHandler(serverSocket, clientSocket, file, cachedFiles, words);
            httpHandler.sendData();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    void handleCached(File file)
    {
        //Czytame dane z pliku i wysylamy je do przegladarki
        //TODO edytuj odpowiedz zeby mozna bylo stwierdzic ze jest z cache'a
        try
        {
            FileReader fileReader = new FileReader(file);
            int read;
            StringBuilder responseBuilder = new StringBuilder();
            while ((read = fileReader.read()) >= 0)
            {
                responseBuilder.append((char) read);
            }
            clientWriter.write("HTTP/1.0 200 OK\r\n");
            clientWriter.flush();
            clientWriter.write(responseBuilder.toString());
            clientWriter.write("\r\n");
            clientWriter.flush();
            fileReader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    boolean isCashed(File cached)
    {
        if (!cached.exists())
            return false;

        for (File file : cachedFiles)
        {
            if(file.getPath().equals(cached.getPath()))
                return true;
        }

        return false;
    }
}
