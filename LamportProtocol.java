import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;


public class LamportProtocol implements Runnable {
	static int nodeId;
	static String[] hosts;
	static int[] ports;

	public LamportProtocol(int node, String[] host, int[] port) {
		// TODO Auto-generated constructor stub
		nodeId = node;
		hosts = host;
		ports = port;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		csEnter();		
	}

	public static void csEnter(){
		sendRequest();
		checkCS();
		csLeave();
	}

	public static void csLeave(){
		synchronized(Variables.lock){
			int temp = Variables.queue.peek();
			System.out.println(nodeId + " OUT OF CRITICAL SECTION!!!!");
			synchronized(Variables.editqueue){
				Variables.hash.remove(Variables.queue.peek());
				Variables.queue.poll();
			}
			System.out.println(Variables.queue.toString() +" " + Variables.hash.toString()+ " at node " + nodeId);
			sendRelease(temp);
		}
	}

	public static void sendRequest(){
		synchronized(Variables.locksendrecv){
			Variables.timestamp++; 
			synchronized(Variables.editqueue){
				Variables.hash.put(Variables.timestamp, nodeId);
				Variables.queue.offer(Variables.timestamp);
			}
			for(int i=0; i < hosts.length; i++){
				if(i!=nodeId){
					Socket clientSock;
					try {
						clientSock = new Socket(hosts[i],ports[i]);
						BufferedWriter brwriter = new BufferedWriter(new OutputStreamWriter(clientSock.getOutputStream()));
						String out_messg = "REQ-" + Variables.timestamp + "-" + nodeId;
						System.out.println(out_messg + " to node " + i + " from node " + nodeId);
						brwriter.write(out_messg);
						brwriter.flush();
						brwriter.close();
						clientSock.close();	
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
	}

	public static void sendRelease(int temp){
		synchronized(Variables.locksendrecv){
			Variables.timestamp++;
			for(int i=0; i < hosts.length; i++){
				if(i!=nodeId){
					Socket clientSock;
					try {
						clientSock = new Socket(hosts[i],ports[i]);
						BufferedWriter brwriter = new BufferedWriter(new OutputStreamWriter(clientSock.getOutputStream()));
						String out_messg = "REL-" + Variables.timestamp + "-" + nodeId + "-" + temp;
						System.out.println(out_messg + " to node " + i + " from node " + nodeId);
						brwriter.write(out_messg);
						brwriter.flush();
						brwriter.close();
						clientSock.close();	
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			Variables.all_reply=false;
		}
	}

	public static void checkCS(){
		synchronized (Variables.lock) {
			boolean temp = false;
			System.out.println("request cs " + nodeId);
			//loop till the node satisfies L1 and L2
			while(true){
				synchronized(Variables.editreply){
					for(int i=0; i<Variables.reply_array.length; i++){
						if(Variables.reply_array[i]==false){
							temp=false;
							break;
						}
						else{
							temp=true;
						}
					}
				}
				Variables.all_reply = temp;
				synchronized(Variables.editqueue){
					if((Variables.all_reply==true) && (nodeId==Variables.hash.get(Variables.queue.peek()))){
						break;
					}
				}
			}
			System.out.println(nodeId + " IN CRITICAL SECTION!!!!!");
		}

	}

}
