package FileServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Server {

    static String path = "D:\\JavaFiles\\data\\";
    private static boolean isRunning = true;
    private static final int PORT = 34522;
    public static HashMap<String, String> filenames = new HashMap<String, String>();



    public static void main(String[] args) throws IOException {

        createDirectory(path);

        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server started");

                try (
                        Socket socket = server.accept();
                        DataInputStream input = new DataInputStream(socket.getInputStream());
                        DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                ) {
                    while (isRunning){
                        String response = "";
                        String id = "";
                    String message = input.readUTF();
                    if (message.equals("exit")) {
                        input.close();
                        output.close();
                        socket.close();
                        isRunning = false;
                    } else{
                        switch ((message.split(";")[0])){

                            case "PUT":
                                System.out.println(message);
                                if (message.split(";").length > 1) {
                                    path = path + message.split(";")[1];
                                }
                                File f = new File(path);
                                if(f.exists() && !f.isDirectory()) {
                                    response = "403";
                                } else {
                                    int length = input.readInt();
                                    byte[] data = new byte[length];
                                    input.readFully(data, 0, data.length);
                                    System.out.println(path);
                                    String hash = generateId(path);
                                    filenames.put(hash, path);
                                    try (FileOutputStream fos = new FileOutputStream(path)) {
                                        fos.write(data);
                                        System.out.println(path + "****");
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    id = hash;
                                    response = "200" + " " + id;
                                }
                                output.writeUTF(response);
                                path = path.replace(message.split(";")[1],"");
                                break;


                            case "GET":
                                if (message.split(";")[1].equals("n")) {
                                    path = path + message.split(";")[2];
                                    f = new File(path);
                                    if (f.exists() && !f.isDirectory()) {
                                        response = "200";
                                        Path path2 = Paths.get(path);
                                        byte[] data = new byte[0];
                                        if (Files.exists(path2) && !Files.isDirectory(path2)) {
                                            try {
                                                data = Files.readAllBytes(path2);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        output.writeUTF(response);
                                        output.writeInt(data.length);
                                        output.write(data);
                                    } else {
                                        response = "403";
                                        output.writeUTF(response);
                                    }
                                } else {
                                    id = message.split(";")[2];
                                    if (filenames.containsKey(id)) path = filenames.get(id);
                                    f = new File(path);
                                    if (f.exists() && !f.isDirectory()) {
                                        response = "200";
                                        Path path2 = Paths.get(path);
                                        byte[] data = new byte[0];
                                        if (Files.exists(path2) && !Files.isDirectory(path2)) {
                                            try {
                                                data = Files.readAllBytes(path2);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        output.writeUTF(response);
                                        output.writeInt(data.length);
                                        output.write(data);
                                    } else {
                                        response = "403";
                                        output.writeUTF(response);
                                    }
                                }
                                path = path.replace(message.split(";")[2],"");
                                break;


                            case "DELETE":
                                if (message.split(";")[1].equals("n")){
                                    path = path + message.split(";")[2];
                                } else {
                                    id = message.split(";")[2];
                                    if (filenames.containsKey(id)) path = filenames.get(id);
                                }
                                f = new File(path);
                                if (f.exists() && !f.isDirectory()) {
                                    response = "200";
                                    delete(path);
                                    filenames.remove(path);
                                } else {
                                    response = "403";
                                }
                                output.writeUTF(response);
                                path = path.replace(message.split(";")[2],"");
                                break;
                        }
                        output.flush();
                    }



                }
            }
        }

    }
    public static void createDirectory(String path) {
        File fileDirectory = new File(path);
        if (!fileDirectory.exists()) {
            fileDirectory.mkdirs();
        }
    }

    public static String generateId(String filename) {
        return String.valueOf(Math.abs(filename.hashCode()));
    }

    public static void delete(String filepath) {
        try {
            File file = new File(filepath);
            file.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}





