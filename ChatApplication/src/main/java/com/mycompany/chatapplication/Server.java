/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.chatapplication;

/**
 *
 * @author sahin
 */
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static Map<String, PrintWriter> clientWriters = new HashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Server is running...");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new ClientHandler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class ClientHandler extends Thread {
        private String clientName;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("SUBMITNAME");
                    clientName = in.readLine();
                    if (clientName == null) {
                        return;
                    }
                    synchronized (clientWriters) {
                        if (!clientWriters.containsKey(clientName)) {
                            clientWriters.put(clientName, out);
                            break;
                        }
                    }
                }

                out.println("NAMEACCEPTED " + clientName);
                for (PrintWriter writer : clientWriters.values()) {
                    writer.println("MESSAGE " + clientName + " has joined");
                }

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/quit")) {
                        return;
                    }
                    for (PrintWriter writer : clientWriters.values()) {
                        writer.println("MESSAGE " + clientName + ": " + message);
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (clientName != null) {
                    clientWriters.remove(clientName);
                    for (PrintWriter writer : clientWriters.values()) {
                        writer.println("MESSAGE " + clientName + " has left");
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }
}

