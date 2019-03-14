
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.apache.commons.io.input.ReversedLinesFileReader;

public class Server
implements Runnable {
    private LamportClock localClock;
    private String serverIP;
    private int serverPort;
    private String serverID;
    private ArrayList<String> files;

    public Server(String serverID, String configFile_server, String configFile_file) {
        this.serverID = serverID;
        this.files = new ArrayList();
        this.configServer(configFile_server);
        this.configFiles(configFile_file);
    }

    private void configServer(String file) {
        try {
            BufferedReader r = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split(" ");
                if (!this.serverID.equals(parts[0])) continue;
                this.localClock = new LamportClock(Integer.parseInt(parts[3]));
                this.serverIP = InetAddress.getByName(parts[1]).getHostAddress();
                this.serverPort = Integer.parseInt(parts[2]);
                break;
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void configFiles(String file) {
        try {
            BufferedReader r = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = r.readLine()) != null) {
                this.files.add(line);
                line = String.valueOf(this.serverID) + "/" + line;
                File newFile = new File(line);
                if (newFile.exists()) continue;
                File parentFile = newFile.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                try {
                    newFile.createNewFile();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Cannot creat this file");
                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getID() {
        return this.serverID;
    }

    public int getPort() {
        return this.serverPort;
    }

    public LamportClock getClock() {
        return this.localClock;
    }

    public ArrayList<String> getFiles() {
        return this.files;
    }

    @Override
    public void run() {
        this.run_listenning();
        System.out.println(this.files);
    }

    public void run_listenning() {
        ServerListener listener = new ServerListener(this);
        listener.start();
    }

    private synchronized String read(String file) {
        String lastLine = "";
        file = String.valueOf(this.serverID) + "/" + file;
        File f = new File(file);
        ReversedLinesFileReader reader = null;
        try {
            reader = new ReversedLinesFileReader(f, Charset.defaultCharset());
            lastLine = reader.readLine();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return lastLine;
    }

    private synchronized void write(String file, String command) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(String.valueOf(this.serverID) + "/" + file, true)));
            out.println(command);
            out.close();
        }
        catch (IOException out) {
            // empty catch block
        }
    }

    public synchronized void deliver_message(Message m) {
        this.localClock.message(m.getTs());
        String opt = m.getOpt();
        String r2IP = m.getIP();
        int r2Port = m.getPort();
        if (opt.equals("READ")) {
            String command = this.read(m.getFile());
            Message rm = new Message("SERVER", this.serverID, "FINISHED-READ", this.localClock.get_time(), command, this.serverID, m.getFile(), null, this.serverIP, this.serverPort);
            this.send_message(rm, r2IP, r2Port);
        } else if (opt.equals("WRITE")) {
            this.write(m.getFile(), m.getCom());
            String command = "Writing finished!";
            Message rm = new Message("SERVER", this.serverID, "FINISHED-WRITE", this.localClock.get_time(), command, this.serverID, m.getFile(), null, this.serverIP, this.serverPort);
            this.send_message(rm, r2IP, r2Port);
        } else if (opt.equals("ENQUIRY")) {
            Message rm = new Message("SERVER", this.serverID, "ENQUIRY", this.localClock.get_time(), null, this.serverID, m.getFile(), this.files, this.serverIP, this.serverPort);
            this.send_message(rm, r2IP, r2Port);
            System.out.println("has reply to client for an enquiry");
        } else {
            System.err.println("ERROR: Wrong operation type - " + opt);
        }
    }

    public synchronized void send_message(Message m, String IP, int Port) {
        try {
            Socket sock = new Socket(IP, Port);
            OutputStream out = sock.getOutputStream();
            ObjectOutputStream outStream = new ObjectOutputStream(out);
            outStream.writeObject(m);
            outStream.close();
            out.close();
            sock.close();
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server s = new Server(args[0], args[1], args[2]);
        Thread t = new Thread(s);
        t.start();
    }
}

