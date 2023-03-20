package FileServer;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

public class Client {

    private static boolean isRunning = true;
    public static Scanner scan = new Scanner(System.in);
    public static void main(String[] args) throws IOException, SocketException {
        String address = "127.0.0.1";
        int port = 34522;
        Socket socket = new Socket(address,port);
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        String req = "";
        String response = "";
        byte[] data;
        String filename = "";

        while (isRunning) {
            System.out.println("Enter action (1 - get a file, 2 - save a file, 3 - delete a file, exit - stop the process): ");
            String action = scan.nextLine();
            switch (action) {
                case "1":
                    req = getRequest();
                    output.writeUTF(req);
                    System.out.println("The request was sent.");
                    response = input.readUTF();
                    if (processResponse(response, action)) {
                        int length = input.readInt();
                        data = new byte[length];
                        input.readFully(data, 0, data.length);
                        filename = "D:\\JavaFiles\\data\\" + scan.nextLine();
                        try (FileOutputStream fos = new FileOutputStream(filename)) {
                            fos.write(data);
                            System.out.println(filename + "****");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.print("File saved on the hard drive!");
                    }
                    break;

                case "2":
                    System.out.print("Enter filename: ");
                    String name = scan.nextLine();
                    Path path = Paths.get("D:\\JavaFiles\\data\\" + name);
                    data = Files.readAllBytes(path);
                    System.out.print("Enter name of the file to be saved on server: ");
                    String outName = scan.nextLine();
                    output.writeUTF("PUT" + ";" + outName);
                    output.writeInt(data.length); // write length of the message
                    output.write(data);
                    response = input.readUTF();
                    processResponse(response, action);
                    String pathUpdate = path.getParent().toString();
                    path = Path.of(pathUpdate);
                    break;

                case "3":
                    req = deleteRequest();
                    output.writeUTF(req);
                    System.out.println("The request was sent.");
                    response = input.readUTF();
                    processResponse(response, action);
                    break;
                case "exit":
                    output.writeUTF("exit");
                    output.flush();
                    output.close();
                    input.close();
                    socket.close();
                    System.exit(0);
                }
            }
        }





    public static String getRequest() {
        String name = "";
        String mode = "";
        System.out.println("Do you want to get the file by/id (1 - name, 2 - id): ");
        String choice = scan.nextLine();
        switch (choice) {
            case "1":
                System.out.print("Enter filename: ");
                name = scan.nextLine();
                mode = "n";
                break;
            case "2":
                System.out.print("Enter id: ");
                name = scan.nextLine();
                mode = "i";
                break;
        }
        return "GET" + ";" + mode + ";" + name;
    }

    public static String deleteRequest() {
        String name = "";
        String mode = "";
        System.out.println("Do you want to delete the file by name/id (1 - name, 2 - id): ");
        String choice = scan.nextLine();
        switch (choice) {
            case "1":
                System.out.print("Enter filename: ");
                name = scan.nextLine();
                mode = "n";
                break;
            case "2":
                System.out.print("Enter id: ");
                name = scan.nextLine();
                mode = "i";
                break;
        }
        return "DELETE" + ";" + mode + ";" + name;
    }


    public static Boolean processResponse(String response, String operation) {
        switch (operation) {
            case "2":
                if (response.startsWith("200")) {
                    System.out.println("Response says that file is saved! ID = " + response.substring(4));
                    return true;
                }
                else {
                    System.out.println("The response says that creating the file was forbidden!");
                }
                break;
            case "1":
                if (response.equals("200"))  {
                    System.out.print("The file was downloaded! Specify a name for it: ");
                    return true;
                }
                else {
                    System.out.println("The response says that this file is not found!");
                }
                break;
            case "3":
                if (response.equals("200")) {
                    System.out.println("The response says that the file was successfully deleted!");
                    return true;
                }
                else {
                    System.out.println("The response says that this file is not found!");
                }
                break;
        }
        return false;
    }

}
