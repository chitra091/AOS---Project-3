import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;


public class Mutex implements Runnable{
	int nodeId;
	String[] hosts;
	int[] ports;
	
	
	public Mutex(int nodeId, String[] hosts, int[] ports) {
		this.nodeId = nodeId;
		this.hosts = hosts;
		this.ports = ports;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String ip = args[0];
		String[] tokens=null;
		String[] hostName = null;
		int[] portNo = null;
		List<String> lines = new ArrayList<String>();
		try	{
			File file = new File("config.txt");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line=null;
			while ((line = bufferedReader.readLine()) != null) {
				if(line.startsWith("#")||line.length()==0) {
					continue;
				}
				else {
					line = line.trim();
					lines.add(line);
				}
			}
			fileReader.close();
			String str = lines.get(0);
			String[] first_line = str.split("\\s+");
			int no_of_nodes = Integer.parseInt(first_line[0]);
			int no_of_messages = Integer.parseInt(first_line[1]);
			portNo = new int[no_of_nodes];
			hostName = new String[no_of_nodes];
			Variables.reply_array = new boolean[no_of_nodes];
			Variables.reply_array[Integer.parseInt(ip)]=true;
			for(int i=1;i<=no_of_nodes;i++) {
				tokens = lines.get(i).split("\\s+");
				portNo[i-1]=Integer.parseInt(tokens[2]);
				hostName[i-1]=tokens[1];
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		Mutex mutex = new Mutex(Integer.parseInt(ip), hostName, portNo);
		Thread mutexthread = new Thread(mutex);
		mutexthread.start();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LamportProtocol lp = new LamportProtocol(Integer.parseInt(ip), hostName, portNo);
		Thread lpthread = new Thread(lp);
		lpthread.start();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			ServerSocket serverSocket = new ServerSocket(ports[nodeId]);
			while(true)	{
				try	{
					Socket sock = serverSocket.accept();
					Thread serviceThread = new Thread(new WorkerThread(sock,nodeId,hosts,ports));
					serviceThread.start();
				}
				catch(Exception ex)	{
					ex.printStackTrace();
				}
			}


		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
