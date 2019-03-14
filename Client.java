
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

public class Client
implements Runnable {
    private LamportClock localClock;
    private String clientID;
    private String clientIP;
    private int clientPort;
    private HashMap<String, String> servers;
    private HashMap<String, String> clients;
    private Queue<Message> defered_reply;
    private ArrayList<String> serverIDs;
    private ArrayList<String> clientIDs;
    private ArrayList<String> files;
    private int finish_CS = 0;
    private ArrayList<String> pending_servers;
    private HashMap<String, Mutex_imp> mutexs;

    public Client(String clientID, String configFile_server, String configFile_client) {
        this.clientID = clientID;
        this.servers = new HashMap();
        this.clients = new HashMap();
        this.serverIDs = new ArrayList();
        this.clientIDs = new ArrayList();
        this.defered_reply = new LinkedList<Message>();
        this.configServer(configFile_server);
        this.configClient(configFile_client);
        this.pending_servers = new ArrayList<String>(this.serverIDs);
    }
    
    public Queue<Message> getDefer(){
    	return this.defered_reply;
    }
    
    public String getIP() {
        return this.clientIP;
    }

    public int getPort() {
        return this.clientPort;
    }

    public String getID() {
        return this.clientID;
    }

    public LamportClock getClock() {
        return this.localClock;
    }

    public ArrayList<String> getOtherClients() {
        return this.clientIDs;
    }
    // Configuration all the servers in the system
    private void configServer(String file) {
        try {
            BufferedReader r = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split(" ");
                this.addServer(parts[0], InetAddress.getByName(parts[1]).getHostAddress(), parts[2]);
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Configuration all the Client in the system
    private void configClient(String file) {
        try {
            BufferedReader r = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split(" ");
                if (this.clientID.equals(parts[0])) {
                    this.localClock = new LamportClock(Integer.parseInt(parts[3]));                    
                    this.clientIP = InetAddress.getByName(parts[1]).getHostAddress();
                    this.clientPort = Integer.parseInt(parts[2]);
                    this.createFile();
                    continue;
                }
                this.addClient(parts[0], InetAddress.getByName(parts[1]).getHostAddress(), parts[2]);
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Configuration all the Mutex lock in the system
    public void configMutex() {
        this.mutexs = new HashMap();
        for (String file : this.files) {
            Mutex_imp mutex = new Mutex_imp(this, file);
            this.mutexs.put(file, mutex);
        }
    }

    private void addServer(String serverID, String ip, String port) {
        this.servers.put(serverID, String.valueOf(ip) + ":" + port);
        this.serverIDs.add(serverID);
    }

    private void addClient(String clientID, String ip, String port) {
        this.clients.put(clientID, String.valueOf(ip) + ":" + port);
        this.clientIDs.add(clientID);
    }

    public int sizeOfServers() {
        return this.serverIDs.size();
    }

    public int sizeOfClients() {
        return this.clientIDs.size();
    }
    // create client log file
    public void createFile() {
        File ClientFile = new File(String.valueOf(this.clientID) + ".txt");
        if (!ClientFile.exists()) {
            try {
                ClientFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
                System.err.println("[ERROR]: Cannot create ClientFile!");
            }
        }
    }
    // write log function
    public void writeToClientFile(String record) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(String.valueOf(this.clientID) + ".txt", true)));
            out.println(record);
            out.close();
        }
        catch (IOException out) {
            // empty catch block
        	out.printStackTrace();
            System.err.println("[ERROR]: Cannot write Client File!");
        }
    }

    @Override
    public void run() {
    	// system configure
        System.out.println("client-" + this.clientID + " is started!");
        this.writeToClientFile("client-" + this.clientID + " is started!");
        this.run_listenning();
        this.initial_connection();
        while (!this.pending_servers.isEmpty()) {
            System.out.println("Still need replies of filenames from " + this.pending_servers);
        }
        System.out.println("finished initial connection!");
        this.writeToClientFile("finished initial connection!");
        this.configMutex();
        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("  There are " + this.sizeOfServers() + " server processes!");
        this.writeToClientFile("  There are " + this.sizeOfServers() + " server processes!");
        System.out.println("  " + this.serverIDs);
        this.writeToClientFile("  " + this.serverIDs);
        System.out.println("  There are " + this.sizeOfClients() + " client processes!");
        this.writeToClientFile("  There are " + this.sizeOfClients() + " client processes!");
        System.out.println("  " + this.clientIDs);
        this.writeToClientFile("  " + this.clientIDs);
        System.out.println("Type 's' if preparing finished.");
        // wait for start
        Scanner keyboard = new Scanner(System.in);
        while (!(keyboard.next()).equals("s")) {
            System.out.println("Not match, please type in again.");
        }
        System.out.println("[START]: Client start generating operations! (total-50)");
        this.writeToClientFile("[START]: Client start generating operations! (total-50)");
        // start CS for 50 times
        while (this.finish_CS < 50) {
            System.out.println("[" + this.finish_CS + "] " + this.clientID + " Finished Critical Section is " + this.finish_CS);
            this.writeToClientFile("[" + this.finish_CS + "] " + this.clientID + " Finished Critical Section is " + this.finish_CS);
            String select_file = this.generate_opt();
            Mutex_imp mutex = this.mutexs.get(select_file);
            // keep running while for entering the CS
            while (!mutex.isAllowedInCS()) {
            }
            this.execute_CS(select_file);
            System.out.println(" [Waiting]: Message sended to Servers and waiting for replys:");
            this.writeToClientFile(" [Waiting]: Message sended to Servers and waiting for replys:");
            // keep check when CS finished
            while (!mutex.getFinished()) {
                System.out.print("-");
            }
            System.out.println(" [Finished-Replies-Servers]: Get all needed Replies from Servers!");
            this.writeToClientFile(" [Finished-Replies-Servers]: Get all needed Replies from Servers!");

            mutex.setFinished(false);
            this.send_defer();
            mutex.setUnwaiting();
            
            
            this.finish_CS++;
        }
        System.out.println("[Over]: All Operations Finished!");
        this.writeToClientFile("[Over]: All Operations Finished!");
    }

    private void initial_connection() {
        for (String serverID : this.serverIDs) {
            Message m = new Message("SERVER", this.clientID, "ENQUIRY", this.getClock().get_time(), null, serverID, null, null, this.clientIP, this.clientPort);
            String addr = this.servers.get(serverID);
            this.send_message(addr, m);
        }
    }

    private void run_listenning() {
        ClientListener listener = new ClientListener(this);
        listener.start();
    }

    private synchronized void execute_CS(String file) {
        Mutex_imp mutex = this.mutexs.get(file);
        Message m = mutex.getMsg();
        if (m.getOpt().equals("READ")) {
            System.out.println("=== execute critical section with READ! ===");
            this.writeToClientFile("=== execute critical section with READ! ===");
            String addr = this.servers.get(m.getTarget());
            this.send_message(addr, m);
        } else if (m.getOpt().equals("WRITE")) {
            System.out.println("=== execute critical section with WRITE! ===");
            this.writeToClientFile("=== execute critical section with WRITE! ===");
            mutex.setHaveFinished(0);
            for (String addr : this.servers.values()) {
                this.send_message(addr, m);
            }
        } else {
            System.err.println("[ERROR]: Worng type of request! " + m);
            this.writeToClientFile("[ERROR]: Worng type of request! " + m);
        }
    }
    // randomly generate operation on random server
    private String generate_opt() {
        Message m;
        Random rand = new Random();
        this.localClock.event();
        int delayTime = rand.nextInt(1000) + 200;
        try {
            Thread.sleep(delayTime);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean select_Read = rand.nextBoolean();
        String select_server = this.serverIDs.get(rand.nextInt(this.serverIDs.size()));
        String select_file = this.files.get(rand.nextInt(this.files.size()));
        int ts = this.localClock.get_time();
        if (select_Read) {
            m = new Message("REQUEST", this.clientID, "READ", ts, null, select_server, select_file, null, this.clientIP, this.clientPort);
        } else {
            String command = "<" + this.clientID + ", " + ts + ">";
            m = new Message("REQUEST", this.clientID, "WRITE", ts, command, null, select_file, null, this.clientIP, this.clientPort);
        }
        Mutex_imp mutex = this.mutexs.get(select_file);
        mutex.send_request(m);
        return select_file;
    }
    // Broadcast request
    public synchronized void broadCast(Message m, ArrayList<String> cids) {
        System.out.println(" [broadCast]: " + m);
        this.writeToClientFile(" [broadCast]: " + m);
        this.localClock.event();
        for (String cID : cids) {
            //if (cID.equals(this.clientID)) continue;
            String target = this.clients.get(cID);
            System.out.println(" target address is " + target);
            this.writeToClientFile(" target address is " + target);
            this.send_message(target, m);
        }
        
    }

    public synchronized void send_message(String targetAddr, Message m) {
        System.out.println(" [Send] Sends message to " + targetAddr + " with Message - " + m);
        this.writeToClientFile(" [Send] Sends message to " + targetAddr + " with Message - " + m);
        String[] addr = targetAddr.split(":");
        try {
            Socket sock = new Socket(addr[0], Integer.parseInt(addr[1]));
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
    
    // sending all the defered message after CS
    public synchronized void send_defer() {
    	while (!this.defered_reply.isEmpty()) {
    		Message m = this.defered_reply.poll();
    		//System.out.println("=>=> check msgs from send_defer: " + m.toString());
    		
    		String file = m.getFile();
    		String target = this.clients.get(m.getID());
            Message reply = new Message("REPLY", this.clientID, null, this.localClock.get_time(), null, null, file, null, this.clientIP, this.clientPort);
            System.out.println(" [REPLY]: has replied to " + m.getIP() + ":" + m.getPort());
            this.writeToClientFile(" [REPLY]: has replied to " + m.getIP() + ":" + m.getPort());
            this.send_message(target, reply);
    		
    		//this.deliver_message(m);
    	}
    }
    
    
    public synchronized void deliver_message(Message m) {
    	//System.out.println("=>=> check msgs from deliver_message: " + m.toString());
        this.localClock.message(m.getTs());
        if ("ENQUIRY".equals(m.getOpt())) {
            this.getFiles(m);
        } else {
            String file = m.getFile();
            Mutex_imp mutex = this.mutexs.get(file);
            if (m.getType().equals("REQUEST")) {
                boolean readyreply = mutex.receive_request(m);
                if(readyreply) {
                	String target = this.clients.get(m.getID());
                    Message reply = new Message("REPLY", this.clientID, null, this.localClock.get_time(), null, null, file, null, this.clientIP, this.clientPort);
                    System.out.println(" [REPLY]: has replied to " + m.getIP() + ":" + m.getPort());
                    this.writeToClientFile(" [REPLY]: has replied to " + m.getIP() + ":" + m.getPort());
                    this.send_message(target, reply);
                }
                
            } else if (m.getType().equals("REPLY")) {
                mutex.receive_reply(m);
            }else if (m.getType().equals("SERVER")) {
                mutex.receive_server(m);
            } else {
                System.err.println("[ERROR]: Wrong type of message" + m);
                this.writeToClientFile("[ERROR]: Wrong type of message" + m);
            }
        }
    }

    public synchronized void getFiles(Message m) {
        this.pending_servers.remove(m.getID());
        System.out.println("[Filenames-Get]: Removed " + m.getID() + " from pending_servers");
        this.writeToClientFile("[Filenames-Get]: Removed " + m.getID() + " from pending_servers");
        if (this.files == null) {
            this.files = m.getFileList();
        } else if (!this.files.equals(m.getFileList())) {
            System.err.println("[ERROR]: Files are not same on servers!");
            this.writeToClientFile("[ERROR]: Files are not same on servers!");
        }
    }

    public static void main(String[] args) {
        Client c = new Client(args[0], args[1], args[2]);
        Thread t = new Thread(c);
        t.start();
    }
}

