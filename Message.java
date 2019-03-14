
import java.io.Serializable;
import java.util.ArrayList;

// message class for transfer messages between client and server
public class Message implements Comparable<Message>, Serializable {
    private String type;
    private String ID;
    private String opt;
    private int timeStamp;
    private String command;
    private String serverID;
    private String file;
    private ArrayList<String> files;
    private String IP;
    private int port;

    public Message(String type, String ID, String opt, int timeStamp, String command, String serverID, String file, ArrayList<String> files, String IP, int port) {
        this.type = type;
        this.ID = ID;
        this.opt = opt;
        this.timeStamp = timeStamp;
        this.command = command;
        this.serverID = serverID;
        this.file = file;
        this.files = files;
        this.IP = IP;
        this.port = port;
    }

    @Override
    public int compareTo(Message m) {
        if (this.timeStamp > m.timeStamp) {
            return 1;
        }
        if (this.timeStamp < m.timeStamp) {
            return -1;
        }
        return 0;
    }

    public String getType() {
        return this.type;
    }

    public String getID() {
        return this.ID;
    }

    public String getOpt() {
        return this.opt;
    }

    public int getTs() {
        return this.timeStamp;
    }

    public String getCom() {
        return this.command;
    }

    public String getTarget() {
        return this.serverID;
    }

    public String getFile() {
        return this.file;
    }

    public ArrayList<String> getFileList() {
        return this.files;
    }

    public String getIP() {
        return this.IP;
    }

    public int getPort() {
        return this.port;
    }

    public String toString() {
        return "Message from " + this.ID + " " + this.type + " " + this.opt + " timeStamp:" + this.timeStamp + " " + this.command + " " + this.serverID + " " + this.file + " " + this.files + " " + this.IP + ":" + this.port;
    }
}

