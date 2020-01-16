import java.io.*;
import java.net.Socket;
import java.util.List;

public class HttpHandler
{
    private Socket clientSocket;
    private Socket serverSocket;

    private BufferedReader serverReader;
    private BufferedWriter clientWriter;

    private File file;
    private List<File> cachedFiles;

    String headers;
    String response;

    public HttpHandler(Socket serverSocket, Socket clientSocket, File file, List<File> cachedFiles)
    {
        this.clientSocket = clientSocket;
        this.serverSocket = serverSocket;

        try
        {
            this.serverReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            this.clientWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }

        this.file = file;
        this.cachedFiles = cachedFiles;

        //TODO wczytaj wszystko do jednego stringa

        initHeaders();
        initResponse();
        send();

//        sendData();
//        cachePage();
    }

    private void cachePage()
    {
        try
        {
            if (file.createNewFile())
            {
                System.out.println("writing response to " + file + ":\n" + response);
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(response);
                fileWriter.close();
                cachedFiles.add(file);
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    private void send()
    {
        try
        {
            System.out.println("Sending headers:\n" + headers);
            clientWriter.write(headers);
            clientWriter.write("\r\n");
            clientWriter.flush();
            System.out.println("Sending response:\n" + response);
            clientWriter.write(response);
            clientWriter.write("\n\r");
            clientWriter.flush();
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
//
//    @Override
//    public void run()
//    {
//        try
//        {
//            serverReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
//            clientWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//
//        try
//        {
//            clientWriter.write(headers);
//            clientWriter.write("\r\n");
//            clientWriter.flush();
//            clientWriter.write(response);
//            clientWriter.write("\r\n\r\n");
//            clientWriter.flush();
//        }
//        catch (IOException ex)
//        {
//            System.err.println("Nie udalo sie wyslac");
//        }
//    }

    public String getHeaders()
    {
        return headers;
    }

    public String getResponse()
    {
        return response;
    }

    public void sendData()
    {
        //TODO sproboj odczytac dane
        try
        {
            byte[] buffer = new byte[4096];
            int read;
            do {
                read = serverSocket.getInputStream().read(buffer);
                if (read > 0)
                {
                    clientSocket.getOutputStream().write(buffer, 0, read);

                    clientSocket.getOutputStream().flush();
                }
            } while (read >= 0);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    private void initHeaders()
    {
        //TODO przeczytaj tutaj readlinem
        StringBuilder headersBulider = new StringBuilder();
        try
        {
            String line;
            while (!(line = serverReader.readLine()).equals(""))
            {
                headersBulider.append(line);
                headersBulider.append("\n");
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        headers = headersBulider.toString();
    }

    private void initResponse()
    {
        //TODO tutaj zostaw
        int character;
        StringBuilder responseBuilder = new StringBuilder();
        try
        {
            while ((character = serverReader.read()) > 0)
            {
                responseBuilder.append((char) character);
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        response = responseBuilder.toString();
    }
}
