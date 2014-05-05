package net.rout.com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements the routine of the router that receives messages from the neighbors and update the local information
 * 
 */
public class Receive implements Runnable {

	private final Router router;
	private static Map<String, RouterInfo> removedRouter=new HashMap<String, RouterInfo>();
	public Receive(Router router) {
		this.router = router;
	}

	public void run() {	
		//keeping receiving data
		while (router.stop) {
			//System.out.println("Collect's stop:"+router.stop);
			DatagramPacket receivedPacket;
			try {
				receivedPacket = receiveData();
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			
			byte[] receivedByte=receivedPacket.getData();
			byte[] title= Arrays.copyOfRange(receivedByte, 0, 50);
			byte[] content= Arrays.copyOfRange(receivedByte, 50, receivedByte.length);
			String string = new String(title);
			String head=string.substring(0, string.indexOf("@"));
			
			RouterInfo info = router.getAdjacentByIPAndPort(receivedPacket.getAddress(), receivedPacket.getPort());
			
			if(head.toLowerCase().contains("LINKDOWN".toLowerCase())){
				
				System.out.println("receive "+head+"from "+ info.key);
				LinkInfo likfo=router.links.get(info.key);
				synchronized (router.links){
				likfo.cost=Double.MAX_VALUE;
				}
				//System.out.println("receive "+string +"from "+ info.key);
				System.out.println("now the cost is "+router.links.get(info.key).cost);
				synchronized (router.minimumPathTable){
					router.setToinfinity(info.key);
				}
				synchronized (router.adjacentRouters){
				removedRouter.put(info.key, router.adjacentRouters.remove(info.key));
				}
			}
			
			else if(head.toLowerCase().contains("LINKUP".toLowerCase())){
				
				String key=receivedPacket.getAddress().getHostAddress()+":"+String.valueOf(receivedPacket.getPort());
				
				RouterInfo info1 =removedRouter.get(key);
				//
				System.out.println(key);
				System.out.println(removedRouter.containsKey(key));
				System.out.println("receive: "+head +"from "+ info1.key);
				///
				String[] s=head.split("\\$");
				//System.out.println(s.length);
				double cost=Double.parseDouble(s[1].trim());
				
				LinkInfo likfo=router.links.get(info1.key);
				likfo.cost=cost;
				//System.out.println("now the cost is "+router.links.get(info1.key).cost);
				router.adjacentRouters.put(info1.key,removedRouter.get(info1.key));
			}
			
			else if(head.toLowerCase().contains("close".toLowerCase())){
				break;
			}
			
			else if(head.toLowerCase().contains("map".toLowerCase())){
			Map<String, PathInfo> receivedMap = Router.deserialize(content);

			
			if (info == null) {
				continue;
			}
			updatePingTable(info.key);
			
			//System.out.println("receive pakage from "+info.key);

			synchronized (router.minimumPathTable) {
				router.minimumPathTable.put(info.key, receivedMap);
			}
			boolean changed;
			synchronized (router.minimumPathTable) {
				changed = router.relaxEdges(info.key);
			}
			}
			
			else if(head.toLowerCase().contains("transfer".toLowerCase())){
				
				System.out.println("Receive file!!!");
				
			}
	
		}
	}
	/**
	 * Listen to the port and capture the packets
	 * 
	 * @return A network packet
	 * @throws IOException
	 */
	private DatagramPacket receiveData() throws IOException {
		byte[] receiveData = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		router.serverSocket.receive(receivePacket);
		return receivePacket;
	}

	/**
	 * Updates the timestamp of the last message from a router
	 * 
	 * @param routerID
	 */
	private void updatePingTable(String routerID) {
		synchronized (router.lastPing) {
			router.lastPing.put(routerID, System.currentTimeMillis());
		}
	}

}

