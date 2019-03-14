
import java.util.ArrayList;
import java.util.HashMap;

public class Mutex_imp {
    private Client client;
    private String file;
    private ArrayList<String> pending_reply;
    private boolean inWaiting;
    private HashMap<String, Boolean> seenReply;
    private boolean finished;
    private int haveFinished;
    private Message msg;

    public Mutex_imp(Client client, String file) {
        this.client = client;
        this.inWaiting = false;
        this.file = file;
        this.finished = false;
        this.haveFinished = 0;
        this.seenReply = new HashMap();
    }
    
    public Message getMsg() {
    	return this.msg;
    }
    
    public String getFile() {
        return this.file;
    }

    public boolean getFinished() {
        return this.finished;
    }

    public void setFinished(boolean b) {
        this.finished = b;
    }

    public int getHaveFinished() {
        return this.haveFinished;
    }

    public void setHaveFinished(int n) {
        this.haveFinished = 0;
    }

    public void setUnwaiting() {
        this.inWaiting = false;
    }



    public synchronized boolean receive_request(Message m) {
    	this.seenReply.put(m.getID(), false);
        // when self node is waiting and it not the highest time in all request
    	System.out.println(" [Request-Got]: Get request message from " + m.getID());
    	System.out.println("My inWaiting is: "+Boolean.toString(this.inWaiting));
    	if (this.msg != null) System.out.println("My ts is: "+Integer.toString(this.msg.getTs()));
    	System.out.println("received request ts is: "+Integer.toString(m.getTs()));
        this.client.writeToClientFile(" [Request-Got]: Get request message from " + m.getID());
    	// not in waiting to get into CS
    	if (!this.inWaiting) {
    		return true;
    	}
    	// Waiting to get into CS however the incoming request have a higher timestamp
    	if (this.inWaiting && m.getTs() < this.msg.getTs()) {
    		return true;
    	}
    	// Waiting to get into CS and my message have higher timestamp
    	this.client.getDefer().add(m);
    	this.pending_reply.remove(m.getID());
        return false;
    }
    
    // receive message from other client
    public synchronized void receive_reply(Message m) {
        if (!this.inWaiting) {
            System.err.println("[Reply Error]: Receive reply but not in waiting!");
            this.client.writeToClientFile("[Reply Error]: Receive reply but not in waiting!");
        }else {
            System.out.println(" [Reply-Got]: Get reply from " + m.getID() + " - " + m);
            this.client.writeToClientFile(" [Reply-Got]: Get reply from " + m.getID() + " - " + m);
            this.pending_reply.remove(m.getID());
        }
    }
    
    // receive message from server
    public synchronized void receive_server(Message m) {
        if (m.getOpt().equals("ENQUIRY")) {
            this.client.getFiles(m);
        } else if (m.getOpt().equals("FINISHED-READ")) {
            System.out.println(" [READ-FINISHED-ON-SERVER]: Get informing from Server!");
            this.client.writeToClientFile(" [READ-FINISHED-ON-SERVER]: Get informing from Server!");
            System.out.println(" [READ-RESULT]: " + m.getCom());
            this.client.writeToClientFile(" [READ-RESULT]: " + m.getCom());
            this.finished = true;
        } else if (m.getOpt().equals("FINISHED-WRITE")) {
            ++this.haveFinished;
            System.out.println("  [WRITE-FINISHED-ON-" + m.getTarget() + "]: One server finished writing!");
            this.client.writeToClientFile("  [WRITE-FINISHED-ON-" + m.getTarget() + "]: One server finished writing!");
            if (this.haveFinished == this.client.sizeOfServers()) {
                this.finished = true;
                System.out.println(" [WRITE-FINISHED-ON-ALL-SERVERS]: All servers finished writing!");
                this.client.writeToClientFile(" [WRITE-FINISHED-ON-ALL-SERVERS]: All servers finished writing!");
            }
        } else {
            System.err.println("[ERROR]: SERVER Type with wrong opt!");
            this.client.writeToClientFile("[ERROR]: SERVER Type with wrong opt!");
        }
    }

    public synchronized void send_request(Message m) {
    	// when client is sending request
        if (!this.inWaiting) {
            this.inWaiting = true;
            this.msg = m;
            this.pending_reply = find_pending(this.client.getOtherClients());
            this.client.broadCast(m, this.pending_reply);
        }
    }
    // find other clients that has already receive reply without new request
    public synchronized ArrayList<String> find_pending(ArrayList<String> otherClient){
    	ArrayList<String> stillNeed = new ArrayList<String>();
    	for(String c: otherClient) {
    		if (this.seenReply.containsKey(c) && this.seenReply.get(c)) {
    			stillNeed.add(c);
    		}
    	}
    	return stillNeed;
    }

    public synchronized boolean isAllowedInCS() {
        if (this.inWaiting && this.pending_reply.isEmpty()) {
            return true;
        }
        return false;
    }
}

