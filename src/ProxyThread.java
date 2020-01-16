import java.io.*;
import java.net.Socket;
import java.util.List;

public class ProxyThread extends Thread
{
    private Socket clientSocket;
    private List<String> words;
    private String cachePath;
    private RequestParser requestParser;
    private List<File> cachedFiles;

    private BufferedReader inFromClient;
    private BufferedWriter outToClient;

    private final String CONNECT_TYPE = "CONNECT";

    ProxyThread(Socket clientSocket, List<String> words, String cachePath, List<File> cachedFiles)
    {
        this.clientSocket = clientSocket;
        this.words = words;
        this.cachePath = cachePath;
        this.cachedFiles = cachedFiles;
        try
        {
            inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToClient = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
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
        StringBuilder reqBuilder = new StringBuilder();

        String line;
        try
        {
            while (!(line = inFromClient.readLine()).equals(""))
            {
                reqBuilder.append(line);
                reqBuilder.append("\n");
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

        requestParser = new RequestParser(reqBuilder.toString());

        File file = new File(cachePath + "/" + requestParser.getFileName());

        requestParser.parseUrl();
        requestParser.setConnectionClose();

//        System.out.println(requestParser.getRequest());


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
        String lineConnector = "HTTP/1.0 200 OK\r\n" +
                "\r\n";

        try
        {
            //wysyłam informacje do przeglądarki że mogę rozpocząć tunelowanie tcp
            outToClient.write(lineConnector);
            outToClient.flush();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        String host = requestParser.getHost();
        int port = requestParser.getPort();

        try
        {
            Socket serverSocket = new Socket(host,port);
            new TCPTunel(serverSocket, clientSocket).start();
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
            Socket serverSocket = new Socket(requestParser.getHost(),requestParser.getPort());
            BufferedWriter outServer = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));

            System.out.println(requestParser.getRequest());
            outServer.write(requestParser.getRequest() + "\r\n");
            outServer.flush();

//            HttpHandlerr httpHandlerr = new HttpHandlerr(serverSocket, clientSocket);
            HttpHandler httpHandler = new HttpHandler(serverSocket, clientSocket, file, cachedFiles);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    void handleCached(File file)
    {
        System.out.println("handle cached " + file);
        try
        {
            FileReader fileReader = new FileReader(file);
            int read;
            StringBuilder responseBuilder = new StringBuilder();
            while ((read = fileReader.read()) >= 0)
            {
                responseBuilder.append((char) read);
            }
            outToClient.write("HTTP/1.0 200 OK\r\n");
            outToClient.flush();
            outToClient.write(responseBuilder.toString());
            outToClient.write("\r\n");
            outToClient.flush();
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
