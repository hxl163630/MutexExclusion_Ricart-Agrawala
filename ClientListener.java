
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientListener
extends Thread {
    private Client client;

    public ClientListener(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        System.out.println("client: " + this.client.getID() + " starts listenning at port: " + this.client.getPort());
        try {
            try {
                ServerSocket serverSock = new ServerSocket(this.client.getPort());
                try {
                    do {
                        ObjectInputStream inStream;
                        Socket sock;
                        InputStream in;
                        sock = serverSock.accept();
                        System.out.println(" [Listener]: get a socket from other process");
                        in = sock.getInputStream();
                        inStream = new ObjectInputStream(in);
                        try {
                            try {
                                Message m = (Message)inStream.readObject();
                                System.out.println(String.valueOf(this.client.getID()) + ":" + m);
                                //System.out.println("=>=> check msgs from clientlistener: " + m.toString());
                                this.client.deliver_message(m);
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

