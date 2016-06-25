import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class WorkerThread implements Runnable {

	Socket myClientSocket;
	int nodeId;
	String[] hosts;
	int[] ports;

	public WorkerThread(Socket myClientSocket,int nodeId, String[] host, int[] port) {
		this.myClientSocket = myClientSocket;
		this.nodeId = nodeId;
		this.hosts = host;
		this.ports= port;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		synchronized(Variables.locksendrecv){
			BufferedReader in = null;
			String clientCommand="";
			try	{
				in = new BufferedReader(new InputStreamReader(this.myClientSocket.getInputStream()));
				clientCommand = in.readLine();
				String[] val = clientCommand.split("-");

				if(val[0].startsWith("REQ")){
					int node = Integer.parseInt(val[2]);
					synchronized(Variables.editqueue){
						Variables.hash.put(Integer.parseInt(val[1]), node);
						Variables.queue.offer(Integer.parseInt(val[1]));
					}
					System.out.println(Variables.queue.toString() + " " + Variables.hash.toString()+ " at node " + nodeId);
					int maxtime = findMax(Integer.parseInt(val[1]),Variables.timestamp);

					if((maxtime > Variables.timestamp) && (Variables.reply_array[node]==false)){
						synchronized(Variables.editreply){
							Variables.reply_array[node]=true;
						}
					}
					Variables.timestamp=maxtime++;
					in.close();
					myClientSocket.close();

					Socket clientSock = new Socket(hosts[node],ports[node]);
					BufferedWriter brwriter = new BufferedWriter(new OutputStreamWriter(clientSock.getOutputStream()));
					String out_messg = "REP-"+Variables.timestamp+"-"+nodeId;
					System.out.println(out_messg + " to node " + node + " from node " + nodeId);
					brwriter.write(out_messg);
					brwriter.flush();
					brwriter.close();
					clientSock.close();
				}

				if(val[0].startsWith("REP")){
					int maxtime = findMax(Integer.parseInt(val[1]),Variables.timestamp);
					int node = Integer.parseInt(val[2]);
					if((maxtime > Variables.timestamp) && (Variables.reply_array[node]==false)){
						synchronized(Variables.editreply){
							Variables.reply_array[node]=true;
						}
					}
					System.out.println(Variables.queue.toString() +" " + Variables.hash.toString()+ " at node " + nodeId);
					Variables.timestamp=maxtime++;
					in.close();
					myClientSocket.close();
				}

				if(val[0].startsWith("REL")){
					int node = Integer.parseInt(val[2]);
					synchronized(Variables.editqueue){
						Variables.hash.remove(Integer.parseInt(val[3]));
						Variables.queue.remove(Integer.parseInt(val[3]));
					}
					int maxtime = findMax(Integer.parseInt(val[1]),Variables.timestamp);
					Variables.timestamp=maxtime++;
					System.out.println(Variables.queue.toString() +" " + Variables.hash.toString()+ " at node " + nodeId);
					in.close();
					myClientSocket.close();
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	private int findMax(int recvtime, int timestamp) {
		// TODO Auto-generated method stub
		if(recvtime > timestamp){
			return recvtime;
		}
		else{
			return timestamp;
		}
	}

}
