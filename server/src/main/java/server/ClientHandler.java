package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class ClientHandler {
    private static final Logger logger2 = Logger.getLogger(ClientHandler.class.getName());
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server server;

    private String nick;
    private String login;

    public ClientHandler(Socket socket, Server server) {
        Handler conHandler2 = new ConsoleHandler();
        conHandler2.setLevel(Level.ALL);
        logger2.addHandler(conHandler2);
        Date dateNow = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss a zzz");
        conHandler2.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return  formatForDateNow.format(dateNow)+": "+ record.getMessage() + "\n";
            }
        });
        try {
            this.socket = socket;
            logger2.log(Level.SEVERE, "RemoteSocketAddress:  " + socket.getRemoteSocketAddress());
            this.server = server;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            server.getClientsExecutorService().execute(() -> {
                try {
                    socket.setSoTimeout(120000);

                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/reg ")) {
                            logger2.log(Level.SEVERE, "сообщение с просьбой регистрации прошло");
                            String[] token = str.split(" ");
                            boolean b = server
                                    .getAuthService()
                                    .registration(token[1], token[2], token[3]);
                            if (b) {
                                sendMsg("Регистрация прошла успешно");
                            } else {
                                sendMsg("Логин или ник уже занят");
                            }
                        }


                        if (str.equals("/end")) {
                            RuntimeException e = new RuntimeException("Клиент отключился крестиком");
                            logger2.log(Level.SEVERE, e.getMessage(),e);
                                                    }
                        if (str.startsWith("/auth ")) {
                            String[] token = str.split(" ");
                            String newNick = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);

                            login = token[1];

                            if (newNick != null) {
                                if (!server.isLoginAuthorized(login)) {
                                    sendMsg("/authok " + newNick);
                                    nick = newNick;
                                    server.subscribe(this);
                                    logger2.log(Level.SEVERE, "Клиент " + nick + " прошел аутентификацию");
                                    socket.setSoTimeout(0);
                                    break;
                                } else {
                                    sendMsg("С этим логином уже авторизовались");
                                }
                            } else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }
                    }


                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                out.writeUTF("/end");
                                break;
                            }

                            if (str.startsWith("/w ")) {
                                String[] token = str.split(" ", 3);
                                if (token.length == 3) {
                                    server.privateMsg(this, token[1], token[2]);
                                }
                            }

                            if (str.startsWith("/chnick ")) {
                                String[] token = str.split(" ", 2);
                                if (token[1].contains(" ")) {
                                    sendMsg("Ник не может содержать пробелов");
                                    continue;
                                }
                                if (server.getAuthService().changeNick(this.nick, token[1])) {
                                    sendMsg("/yournickis " + token[1]);
                                    sendMsg("Ваш ник изменен на " + token[1]);
                                    this.nick = token[1];
                                    server.broadcastClientList();
                                } else {
                                    sendMsg("Не удалось изменить ник. Ник " + token[1] + " уже существует");
                                }
                            }

                        } else {
                            server.broadcastMsg(nick, str);
                        }
                    }
                } catch (SocketTimeoutException e) {
                    logger2.log(Level.SEVERE, "Клиент отключился по таймауту");
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    logger2.log(Level.SEVERE, "Клиент отключился");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }

    public String getLogin() {
        return login;
    }
}
