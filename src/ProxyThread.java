import java.net.Socket;

public class ProxyThread extends Thread
{
    Socket clientSocket;
    String[] words;
    String cachePath;

    ProxyThread(Socket clientSocket, String[] words, String cachePath)
    {
        this.clientSocket = clientSocket;
        this.words = words;
        this.cachePath = cachePath;
    }
}
