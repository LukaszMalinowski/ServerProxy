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
        String pathName = "params.txt";
        if(args.length != 0)
            pathName = args[0];

        ParamsFile params = new ParamsFile(new File(pathName));
        int port = params.getProxyPort();
        String cachePath = params.getCachePath();
        List<String> words = new ArrayList<>(Arrays.asList(params.getWords()));
        List<File> cachedFiles = getCachedFiles(cachePath);

        System.out.println("Starting proxy on port " + port);
        ServerSocket serverSocket = new ServerSocket(port);

        //Czekamy na polaczenie z przegladarki i po polaczeniu tworzymy nowy watek z podanymi parametrami
        while (true)
        {
            new ProxyThread(serverSocket.accept(), words, cachePath, cachedFiles);
        }
    }

    private static List<File> getCachedFiles(String cachePath)
    {
        File cacheDir = new File(cachePath);
        if(cacheDir.listFiles() == null)
            return new ArrayList<>();
        else
            return Arrays.asList(cacheDir.listFiles());
    }
}
