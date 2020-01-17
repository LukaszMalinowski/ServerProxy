import java.io.*;
import java.net.Socket;

public class TCPTunnel extends Thread
{
    private Socket serverSocket;
    private Socket clientSocket;

    public TCPTunnel(Socket serverSocket, Socket clientSocket)
    {
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run()
    {
        //Osobny watek do asynchronicznego wysylania danych do serwera docelowego
        Thread sendData = new Thread(() ->
        {
            try
            {
                InputStream clientReader = clientSocket.getInputStream();
                OutputStream serverWriter = serverSocket.getOutputStream();
                int read;
                while ((read = clientReader.read()) >= 0)
                {
                    serverWriter.write(read);
                    serverWriter.flush();
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        });
        sendData.start();

        //Odbieranie danych i wysylanie ich do przegladarki
        try
        {
            InputStream serverReader = serverSocket.getInputStream();
            OutputStream clientWriter = clientSocket.getOutputStream();
            int read;
            while ((read = serverReader.read()) >= 0)
            {
                clientWriter.write(read);
                clientWriter.flush();
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}
