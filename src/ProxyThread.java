import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ProxyThread extends Thread
{
    private Socket clientSocket;
    private ArrayList<String> words;
    private String cachePath;
    private RequestParser requestParser;
    private Scanner inFromClient;
    private BufferedWriter outToClient;

    ProxyThread(Socket clientSocket, ArrayList<String> words, String cachePath)
    {
        this.clientSocket = clientSocket;
        this.words = words;
        this.cachePath = cachePath;
        try
        {
            inFromClient = new Scanner(clientSocket.getInputStream());
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
        StringBuilder reqBulider = new StringBuilder();
        while (inFromClient.hasNextLine())
        {
            String line = inFromClient.nextLine();
            if(line.equals(""))
                break;
            reqBulider.append(line);
            reqBulider.append("\n");
        }
        String req = reqBulider.toString();

        requestParser = new RequestParser(req);

        System.out.println("Old request:\n" + requestParser.getRequest());

        requestParser.parseUrl();

        System.out.println("New request: \n" + requestParser.getRequest());

        if(requestParser.getConnectionType().equals("CONNECT"))
            System.out.println("Connect not implemented yet");
        else
        {
            if(isCashed())
                handleCached();
            else
                handleNonCached();
        }
    }

    void handleConnect()
    {
        //TODO implement handling https connection

        // Connect jakby informuje nas, że chce nawiązać połączenie na danym sockecie i potem go używać w przyszłości do pozostałych rządania
        String lineConnecter = "HTTP/1.0 200 Connection established\r\n" +
                "Proxy-Agent: ProxyServer/1.0\r\n" +
                "\r\n";

        try
        {
            outToClient.write(lineConnecter);
            outToClient.flush();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        String host = requestParser.getHost();
        int port = requestParser.getPort();

    }

    void handleNonCached()
    {
        try
        {
            Socket serverSocket = new Socket(requestParser.getHost(),requestParser.getPort());
            BufferedWriter outServer = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));
            outServer.write(requestParser.getRequest() + "\r\n");
            outServer.flush();

            TransferBytesThread bytesThread = new TransferBytesThread(serverSocket,clientSocket);
            bytesThread.start();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    void handleCached()
    {
        //TODO implement sending cached file
    }

    boolean isCashed()
    {
        //TODO check if file is cached
        return false;
    }
}
