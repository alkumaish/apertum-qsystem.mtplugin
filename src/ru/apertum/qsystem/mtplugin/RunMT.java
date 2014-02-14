/**
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package ru.apertum.qsystem.mtplugin;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import ru.apertum.qsystem.common.CustomerState;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.SoundPlayer;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.cmd.JsonRPC20Error;
import ru.apertum.qsystem.common.cmd.JsonRPC20OK;
import ru.apertum.qsystem.common.cmd.RpcGetInt;
import ru.apertum.qsystem.common.exceptions.ServerException;
import ru.apertum.qsystem.common.model.QCustomer;
import ru.apertum.qsystem.server.MainBoard;
import ru.apertum.qsystem.server.QServer;
import static ru.apertum.qsystem.server.QServer.clearAllQueue;
import ru.apertum.qsystem.server.model.QService;
import ru.apertum.qsystem.server.model.QServiceTree;
import ru.apertum.qsystem.server.model.QUser;
import ru.apertum.qsystem.server.model.QUserList;

/**
 *
 * @author egorov
 */
public class RunMT {

    final static private File propFile = new File("config/mtplugin.properties");
    public static int port = 27007;
    public static boolean passive = false;

    public static void go() throws FileNotFoundException, SocketException, IOException {
        try (FileInputStream fis = new FileInputStream(propFile); Scanner s = new Scanner(fis)) {
            while (s.hasNextLine()) {
                final String line = s.nextLine().trim();
                if (!line.startsWith("#")) {
                    final String[] ss = line.split("=");
                    System.out.println(ss[0] + "=" + ss[1]);
                    if ("port".equalsIgnoreCase(ss[0])) {
                        port = Integer.parseInt(ss[1]);
                    }
                    if ("passive".equalsIgnoreCase(ss[0])) {
                        passive = "1".equals(ss[1]);
                    }
                }
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
        // привинтить сокет на локалхост
        //  запуска захвата портов
        if (passive) {
            new Thread(new SocketRunnable(port)).start();
        } else {
            QLog.l().logger().info("Не дублер главного табло.");
        }

    }

    static class SocketRunnable implements Runnable {

        synchronized static byte[] compress(byte[] data) {
            final byte[] output = new byte[data.length];
            final Deflater compresser = new Deflater();
            compresser.setInput(data);
            compresser.finish();
            final int lenAfterCompress = compresser.deflate(output);
            byte[] output1 = new byte[lenAfterCompress];
            System.arraycopy(output, 0, output1, 0, output1.length);
            return output1;
        }

        synchronized public static byte[] decompress(byte[] zipped) throws DataFormatException, IOException {
            final Inflater decompresser = new Inflater();
            decompresser.setInput(zipped, 0, zipped.length);
            byte[] result = new byte[1024];
            int resultLength = decompresser.inflate(result);
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while (resultLength != 0) {
                outputStream.write(result, 0, resultLength);
                result = new byte[1024];
                resultLength = decompresser.inflate(result);
            }
            decompresser.end();
            return outputStream.toByteArray();
        }

        private final Integer port;

        public SocketRunnable(Integer port) {
            this.port = port;
        }

        @Override
        public void run() {

            final ServerSocket server;
            try {
                QLog.l().logger().info("Дублер главного табло захватывает порт \"" + port + "\".");
                server = new ServerSocket(port);
            } catch (IOException e) {
                throw new ServerException("Ошибка при создании серверного сокета: " + e);
            } catch (Exception e) {
                throw new ServerException("Ошибка сети: " + e);
            }
            QLog.l().logger().debug("Старт цикла приема сообщений.");
            // слушаем порт
            while (!Thread.interrupted()) {
                // из сокета клиента берём поток входящих данных
                final Socket socket;
                try {
                    socket = server.accept();
                } catch (IOException e) {
                    throw new ServerException("Ошибка при получении входного потока: " + e.getStackTrace());
                }

                final InputStream inR;
                try {
                    inR = socket.getInputStream();
                    //inR = new GZIPInputStream(socket.getInputStream());
                } catch (IOException ex) {
                    throw new ServerException("Ошибка сети 1: " + ex);
                }

                final String data;
                try {
                    // подождать пока хоть что-то приползет из сети, но не более 10 сек.
                    int i = 0;
                    while (inR.available() == 0 && i < 100) {
                        Thread.sleep(100);//бля
                        i++;
                    }
                    //final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    StringBuilder sb = new StringBuilder();
                    while (inR.available() != 0) {
                        sb = sb.append(new String(Uses.readInputStream(inR)));
                        //final byte[] bb = Uses.readInputStream(inR);
                        //outputStream.write(bb, 0, bb.length);
                        i = 0;
                        while (inR.available() == 0 && i < 30) {
                            Thread.sleep(50);//бля
                            i++;
                        }
                    }

                    //if (sb.toString().contains("method%22%3A%22ping%22%7D")) { // пришла команда пинг не в зипе.
                    data = URLDecoder.decode(sb.toString(), "utf-8");
                    //} else {// пришeл зипованный файл темповый.
                    //data = URLDecoder.decode(sb1.toString(), "utf-8");
                    //    final byte[] bb = outputStream.toByteArray();
                    //    final String st = new String(decompress(bb), "utf-8");
                    //    System.out.println(st);
                    //    data = URLDecoder.decode(st, "utf-8");
                    //}
                    QLog.l().logger().trace(data);
                } catch (IOException ex) {
                    throw new ServerException("Ошибка при чтении из входного потока: " + ex);
                } catch (InterruptedException ex) {
                    throw new ServerException("Проблема со сном: " + ex);
                }

                final QServer.TempList rpc;
                final Gson gson = new Gson();

                try {
                    rpc = gson.fromJson(data, QServer.TempList.class);
                } catch (JsonSyntaxException ex) {
                    QLog.l().logger().error("Что-то с json.", ex);
                    return;
                }
                try {
                    new Thread(new WorkRunnable(rpc, socket.getOutputStream())).start();
                } catch (IOException ex) {
                    throw new ServerException("Ошибка обработки и отправки сетевого сообщения: " + ex);
                }

            }

        }
    }

    static class WorkRunnable implements Runnable {

        public final QServer.TempList rpc;
        public final OutputStream os;

        public WorkRunnable(QServer.TempList rpc, OutputStream os) {
            this.rpc = rpc;
            this.os = os;
        }

        @Override
        public void run() {
            QLog.l().logger().trace("Выполняем метод: \"" + rpc.method);
            System.out.print("Run method: " + rpc.method + "  parameter: ");

            QServer.clearAllQueue();
            final QServer.TempList recList = rpc;

            try {
                if (recList.backup != null) {
                    for (QCustomer recCustomer : recList.backup) {
                        // в эту очередь он был
                        final QService service = QServiceTree.getInstance().getById(recCustomer.getService().getId());
                        if (service == null) {
                            QLog.l().logger().warn("Попытка добавить клиента \"" + recCustomer.getPrefix() + recCustomer.getNumber() + "\" к услуге \"" + recCustomer.getService().getName() + "\" не успешна. Услуга не обнаружена!");
                            continue;
                        }
                        service.setCountPerDay(recCustomer.getService().getCountPerDay());
                        service.setDay(recCustomer.getService().getDay());
                        // так зовут юзера его обрабатываюшего
                        final QUser user = recCustomer.getUser();
                        // кастомер ща стоит к этой услуге к какой стоит
                        recCustomer.setService(service);
                        // смотрим к чему привязан кастомер. либо в очереди стоит, либо у юзера обрабатыватся
                        if (user == null) {
                            // сохраненный кастомер стоял в очереди и ждал, но его еще никто не звал
                            if (recCustomer.getState() != CustomerState.STATE_FINISH) {
                                QServiceTree.getInstance().getById(recCustomer.getService().getId()).addCustomer(recCustomer);
                                QLog.l().logger().debug("Добавили клиента \"" + recCustomer.getPrefix() + recCustomer.getNumber() + "\" к услуге \"" + recCustomer.getService().getName() + "\"");
                            }
                        } else {
                            // сохраненный кастомер обрабатывался юзером с именем userId
                            if (QUserList.getInstance().getById(user.getId()) == null) {
                                QLog.l().logger().warn("Попытка добавить клиента \"" + recCustomer.getPrefix() + recCustomer.getNumber() + "\" к юзеру \"" + user.getName() + "\" не успешна. Юзер не обнаружен!");
                                continue;
                            }
                            QUserList.getInstance().getById(user.getId()).setCustomer(recCustomer);
                            recCustomer.setUser(QUserList.getInstance().getById(user.getId()));
                            QLog.l().logger().debug("Добавили клиента \"" + recCustomer.getPrefix() + recCustomer.getNumber() + "\" к юзеру \"" + user.getName() + "\"");
                        }
                    }
                }
            } catch (ServerException ex) {
                System.err.println("Востановление состояния сервера после изменения конфигурации. " + ex);
                clearAllQueue();
                QLog.l().logger().error("Востановление состояния сервера после изменения конфигурации. Для выключения сервера используйте команду exit. ", ex);
            }
            MainBoard.getInstance().showBoard();

            final Object ansver;
            switch (rpc.method) {
                case "ping":
                    ansver = new RpcGetInt(1);
                    break;
                case "show":
                    // просигналим звуком
                    SoundPlayer.play("/ru/apertum/qsystem/server/sound/ding.wav");
                    ansver = new JsonRPC20OK();
                    break;
                case "repeat":
                    // просигналим звуком
                    SoundPlayer.play("/ru/apertum/qsystem/server/sound/ding.wav");
                    ansver = new JsonRPC20OK();
                    break;
                case "work":

                    ansver = new JsonRPC20OK();
                    break;
                case "kill":

                    ansver = new JsonRPC20OK();
                    break;
                default:
                    System.out.println("Warning: default nethod");
                    ansver = new JsonRPC20Error(JsonRPC20Error.ErrorRPC.UNKNOWN_ERROR, "Warning: default nethod");
            }

            try (PrintWriter writer = new PrintWriter(os)) {
                try {
                    Gson gson = new Gson();
                    final String message = gson.toJson(ansver);
                    QLog.l().logger().trace("Высылаем результат: \"" + message + "\"");
                    writer.print(URLEncoder.encode(message, "utf-8"));
                } catch (UnsupportedEncodingException ex) {
                    throw new ServerException("Ошибка отправки сетевого сообщения: " + ex);
                }
                QLog.l().logger().trace("Высылали результат.");
                writer.flush();
            }
        }
    }
}
