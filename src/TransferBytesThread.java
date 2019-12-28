import java.io.IOException;
import java.net.Socket;

public class TransferBytesThread extends Thread
{
    private Socket serverSocket;
    private Socket clientSocket;

    public TransferBytesThread(Socket serverSocket, Socket clientSocket)
    {
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run()
    {
        //TODO sproboj zrobic polaczenie uzywajac bufferedWritera i bufferedReadeara
        Thread sendData = new Thread(() ->
        {
            try {
                // Read byte by byte from client and send directly to server
                byte[] buffer = new byte[4096];
                int read;
                do {
                    read = clientSocket.getInputStream().read(buffer);
                    if (read > 0) {
                        serverSocket.getOutputStream().write(buffer, 0, read);
                        if (clientSocket.getInputStream().available() < 1) {
                            serverSocket.getOutputStream().flush();
                        }
                    }
                } while (read >= 0);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
        sendData.start();

        try
        {
            byte[] buffer = new byte[4096];
            int read;
            do {
                read = serverSocket.getInputStream().read(buffer);
                if (read > 0)
                {
                    clientSocket.getOutputStream().write(buffer, 0, read);
                    if (serverSocket.getInputStream().available() < 1)
                    {
                        clientSocket.getOutputStream().flush();
                    }
                }
            } while (read >= 0);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}
