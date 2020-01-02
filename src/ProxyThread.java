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
        StringBuilder reqBuilder = new StringBuilder();

        while (inFromClient.hasNextLine())
        {
            String line = inFromClient.nextLine();
            if(line.equals(""))
                break;
            reqBuilder.append(line);
            reqBuilder.append("\n");
        }
        String req = reqBuilder.toString();

        requestParser = new RequestParser(req);

        requestParser.parseUrl();

        System.out.println(req);

        if(requestParser.getConnectionType().equals("CONNECT"))
            handleConnect();
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
        String lineConnecter = "HTTP/1.0 200 OK\r\n" +
                "\r\n";

        try
        {
            //wysyłam informacje do przeglądarki że mogę rozpocząć tunelowanie tcp
            outToClient.write(lineConnecter);
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
            new TransferBytesThread(serverSocket,clientSocket).start();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

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
