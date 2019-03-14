
public class LamportClock {
    private int d;
    private int local_time;

    public LamportClock(int d) {
        this.d = d;
        this.local_time = 0;
    }

    public synchronized void event() {
        this.local_time += this.d;
    }

    public synchronized void message(int timeStamp) {
        this.local_time = Math.max(timeStamp, this.local_time);
        this.event();
    }

    public int get_d() {
        return this.d;
    }

    public int get_time() {
        return this.local_time;
    }
}

