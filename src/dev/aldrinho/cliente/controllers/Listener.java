package dev.aldrinho.cliente.controllers;


import dev.aldrinho.enums.Estados;
import dev.aldrinho.enums.TipoMensaje;
import dev.aldrinho.models.Mensaje;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

import static dev.aldrinho.enums.TipoMensaje.CONNECTED;


public class Listener implements Runnable {

    private static final String HASCONNECTED = "has connected";

    private static String picture;
    private Socket socket;
    public String hostname;
    public int port;
    public static String username;
    public ChatController controller;
    private static ObjectOutputStream oos;
    private InputStream is;
    private ObjectInputStream input;
    private OutputStream outputStream;
    Logger logger = LoggerFactory.getLogger(Listener.class);

    public Listener(String hostname, int port, String username, String picture, ChatController controller) {
        this.hostname = hostname;
        this.port = port;
        Listener.username = username;
        Listener.picture = picture;
        this.controller = controller;
    }

    public void run() {
        try {
            socket = new Socket(hostname, port);
            LoginController.getInstance().showScene();
            outputStream = socket.getOutputStream();
            oos = new ObjectOutputStream(outputStream);
            is = socket.getInputStream();
            input = new ObjectInputStream(is);
        } catch (IOException e) {
            LoginController.getInstance().showErrorDialog("No se ha podido conectar al server");
            logger.error("No se ha podido conectar");
        }
        logger.info("Coneccion establecida " + socket.getInetAddress() + ":" + socket.getPort());

        try {
            connect();
            logger.info("Sockets in and out ready!");
            while (socket.isConnected()) {
                Mensaje message = (Mensaje) input.readObject();
                if (message != null) {
                    logger.debug("Mensaje recibido:" + message.getMensaje() + " TipoMensaje:" + message.getTipoMensaje() + "Username:" + message.getUsername());
                    switch (message.getTipoMensaje()) {
                        case USER:
                            controller.addToChat(message);
                            break;
                        case VOICE:
                            controller.addToChat(message);
                            break;
                        case NOTIFICATION:
                            controller.newUserNotification(message);
                            break;
                        case SERVER:
                            controller.addAsServer(message);
                            break;
                        case CONNECTED:
                            controller.setUserList(message);
                            break;
                        case DISCONNECTED:
                            controller.setUserList(message);
                            break;
                        case STATUS:
                            controller.setUserList(message);
                            break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            controller.logoutScene();
        }
    }


    public static void send(String msg) throws IOException {
        Mensaje createMessage = new Mensaje();
        createMessage.setUsername(username);
        createMessage.setTipoMensaje(TipoMensaje.USER);
        createMessage.setEstado(Estados.AUSENTE);
        createMessage.setMensaje(msg);
        createMessage.setLogo(picture);
        oos.writeObject(createMessage);
        oos.flush();
    }

    public static void sendVoiceMessage(byte[] audio) throws IOException {
        Mensaje createMessage = new Mensaje();
        createMessage.setUsername(username);
        createMessage.setTipoMensaje(TipoMensaje.VOICE);
        createMessage.setEstado(Estados.AUSENTE);
        createMessage.setVoiceMsg(audio);
        createMessage.setLogo(picture);
        oos.writeObject(createMessage);
        oos.flush();
    }


    public static void sendStatusUpdate(Estados status) throws IOException {
        Mensaje createMessage = new Mensaje();
        createMessage.setUsername(username);
        createMessage.setTipoMensaje(TipoMensaje.STATUS);
        createMessage.setEstado(status);
        createMessage.setLogo(picture);
        oos.writeObject(createMessage);
        oos.flush();
    }


    public static void connect() throws IOException {
        Mensaje createMessage = new Mensaje();
        createMessage.setUsername(username);
        createMessage.setTipoMensaje(CONNECTED);
        createMessage.setMensaje(HASCONNECTED);
        createMessage.setLogo(picture);
        oos.writeObject(createMessage);
    }

}
