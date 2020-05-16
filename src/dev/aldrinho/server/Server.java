package dev.aldrinho.server;

import dev.aldrinho.enums.Estados;
import dev.aldrinho.enums.TipoMensaje;
import dev.aldrinho.exceptions.DuplicateUsernameException;
import dev.aldrinho.models.Mensaje;
import dev.aldrinho.models.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Server {

    private static final int PORT = 9001; //Puerto del Servidor
    private static final HashMap<String, Usuario> usernames = new HashMap<>();
    private static HashSet<ObjectOutputStream> writers = new HashSet<>();
    private static List<Usuario> usuarios = new ArrayList<>();
    private static Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws Exception {
        logger.info("El Server esta iniciando.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            listener.close();
        }
    }

    //Clase privada
    private static class Handler extends Thread {
        private String username;
        private Socket socket;
        private Logger logger = LoggerFactory.getLogger(Handler.class);
        private Usuario usuario;

        Handler(Socket socket) throws IOException {
            this.socket = socket;
        }

        public void run() {
            logger.info("Esperando conexion de usuario...");
            try (
                    InputStream is = socket.getInputStream();
                    ObjectInputStream input = new ObjectInputStream(is);
                    OutputStream os = socket.getOutputStream();
                    ObjectOutputStream output = new ObjectOutputStream(os)
            ) {
                Mensaje username = (Mensaje) input.readObject();
                checkDuplicateUsername(username);
                writers.add(output);
                sendNotification(username);
                addToList();
                while (socket.isConnected()) {
                    Mensaje mensaje = (Mensaje) input.readObject();
                    if (mensaje != null) {
                        logger.info(mensaje.getUsername() + " tiene " + usernames.size());
                        switch (mensaje.getTipoMensaje()) {
                            case USER:
                                write(mensaje);
                                break;
                            case VOICE:
                                write(mensaje);
                                break;
                            case CONNECTED:
                                addToList();
                                break;
                            case STATUS:
                                changeStatus(mensaje);
                                break;
                        }
                    }
                }
            } catch (IOException | DuplicateUsernameException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                closeConnections();
            }
        }

        private Mensaje changeStatus(Mensaje inputmsg) throws IOException {
            logger.debug(inputmsg.getUsername() + " ha cambiado su estado a  " + inputmsg.getEstado());
            Mensaje msg = new Mensaje();
            msg.setUsername(usuario.getUsuario());
            msg.setTipoMensaje(TipoMensaje.STATUS);
            msg.setMensaje("");
            Usuario userObj = usernames.get(username);
            userObj.setEstado(inputmsg.getEstado());
            write(msg);
            return msg;
        }

        private synchronized void checkDuplicateUsername(Mensaje mensaje) throws DuplicateUsernameException {
            logger.info(mensaje.getUsername() + " esta intentando conectarse...");
            if (!usernames.containsKey(mensaje.getUsername())) {
                this.username = mensaje.getUsername();
                usuario = new Usuario();
                usuario.setUsuario(mensaje.getUsername());
                usuario.setEstado(Estados.ONLINE);
                usuario.setLogo(mensaje.getLogo());
                usuarios.add(usuario);
                usernames.put(username, usuario);
                logger.info(username + " ha sido agregado!");
            } else {
                logger.error(mensaje.getUsername() + " ya esta conectado");
                throw new DuplicateUsernameException(mensaje.getUsername() + " ya esta conectado");
            }
        }

        private Mensaje sendNotification(Mensaje firstMessage) throws IOException {
            Mensaje msg = new Mensaje();
            msg.setMensaje("Te has unido al chat!");
            msg.setTipoMensaje(TipoMensaje.NOTIFICATION);
            msg.setUsername(firstMessage.getUsername());
            msg.setLogo(firstMessage.getLogo());
            write(msg);
            return msg;
        }


        private Mensaje removeFromList() {
            logger.debug("removeFromList() method Enter");
            Mensaje msg = new Mensaje();
            msg.setMensaje("Te has salido del chat!");
            msg.setTipoMensaje(TipoMensaje.DISCONNECTED);
            msg.setMensaje("SERVER");
            msg.setListUsuarios(usernames);
            write(msg);
            logger.debug("removeFromList() method Exit");
            return msg;
        }


        private Mensaje addToList() throws IOException {
            Mensaje msg = new Mensaje();
            msg.setMensaje("Bienvenido, Te has unido al server! Disfruta del chat!");
            msg.setTipoMensaje(TipoMensaje.CONNECTED);
            msg.setUsername("SERVER");
            write(msg);
            return msg;
        }


        private void write(Mensaje msg) {
            if (writers.size() > 0) {
                for (ObjectOutputStream writer : writers) {
                    msg.setListUsuarios(usernames);
                    msg.setUsuarios(usuarios);
                    msg.setCount(usernames.size());
                    logger.info(writer.toString() + " " + msg.getUsername() + " " + msg.getListUsuarios().toString());
                    try {
                        writer.writeObject(msg);
                        writer.reset();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        //closeConnections();
                    }
                }
            }
        }

        /*
         * Cerrando conexion del usuario
         */
        private synchronized void closeConnections() {
            logger.debug("closeConnections() method Enter");
            logger.info("HashMap usernames:" + usernames.size() + " writers:" + writers.size() + " usuarios size:" + usuarios.size());
            if (username != null) {
                usernames.remove(username);
                logger.info("El Usuario: " + username + " ha sido eliminado!");
            }
            if (usuario != null) {
                usuarios.remove(usuario);
                logger.info("User object: " + usuario + " ha sido elimiado!");
            }
            try {
                removeFromList();
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.info("HashMap usernames:" + usernames.size() + " writers:" + writers.size() + " usuariosList size:" + usuarios.size());
            logger.debug("closeConnections() method Exit");
        }
    }
}
