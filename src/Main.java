import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        ParamsFile params = new ParamsFile(new File("params.txt"));
        int port = params.getProxyPort();
        List<String> words = new ArrayList<>(Arrays.asList(params.getWords()));
        String cachePath = params.getCachePath();
        List<File> cachedFiles = getCachedFiles(cachePath);

        System.out.println("Starting proxy on port " + port);
        ServerSocket serverSocket = new ServerSocket(port);

        while (true)
        {
            new ProxyThread(serverSocket.accept(), words, cachePath, cachedFiles);
        }
    }

    private static List<File> getCachedFiles(String cachePath)
    {
        File cacheDir = new File(cachePath);
        return Arrays.asList(cacheDir.listFiles());
    }
}
