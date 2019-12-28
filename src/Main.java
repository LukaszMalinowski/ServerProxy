import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        ParamsFile params = new ParamsFile(new File("params.txt"));
        int port = params.getProxyPort();
        ArrayList<String> words = new ArrayList<>(Arrays.asList(params.getWords()));
        String cachePath = params.getCachePath();

        System.out.println("Starting proxy on port " + port);
        ServerSocket serverSocket = new ServerSocket(port);

        while (true)
        {
            new ProxyThread(serverSocket.accept(), words, cachePath);
        }
    }
}
