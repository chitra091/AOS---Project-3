import java.util.HashMap;
import java.util.PriorityQueue;


public class Variables {
	public static int timestamp=0;
	public static boolean all_reply = false, cscheck=false;
	public static boolean[] reply_array;
	public static PriorityQueue<Integer> queue = new PriorityQueue<Integer>();
	public static HashMap<Integer, Integer> hash = new HashMap<Integer, Integer>();
	public static Object lock = new Object();
	public static Object locksendrecv = new Object();
	public static Object editreply = new Object();
	public static Object editqueue = new Object();
}
