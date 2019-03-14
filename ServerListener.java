
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerListener
extends Thread {
    private Server server;

    public ServerListener(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        System.out.println("server: " + this.server.getID() + " starts listenning at port: " + this.server.getPort());
        try {
            try {
                ServerSocket serverSock = new ServerSocket(this.server.getPort());
                try {
                    do {
                        ObjectInputStream inStream;
                        Socket sock;
                        InputStream in;
                        sock = serverSock.accept();
                        in = sock.getInputStream();
                        inStream = new ObjectInputStream(in);
                        try {
                            try {
                                Message m = (Message)inStream.readObject();
                                System.out.println(String.valueOf(this.server.getID()) + ":" + m);
                                this.server.deliver_message(m);
                            }
                            catch (ClassNotFoundException e) {
                                e.printStackTrace();
                                inStream.close();
                                in.close();
                                sock.close();
                                continue;
                            }
                        }
                        catch (Throwable throwable2) {
                            inStream.close();
                            in.close();
                            sock.close();
                            throw throwable2;
                        }
                        inStream.close();
                        in.close();
                        sock.close();
                    } while (true);
                }
                catch (Throwable throwable3) {
                    if (serverSock != null) {
                        serverSock.close();
                    }
                    throw throwable3;
                }
            }
            catch (Throwable throwable4) {
                throw throwable4;
            }
        }
        catch (IOException e) {
            System.err.println(e);
            return;
        }
    }
}

