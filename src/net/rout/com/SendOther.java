package net.rout.com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


/**
 * Implements the client side of the router, i.e., sending messages to his neighbors 
 */
public class SendOther implements Runnable {

	private final Router router;


	public SendOther(Router router) {
		this.router = router;
	}
	
	public void run() {
		while (router.stop) {
			//System.out.println("Sendother's stop:"+router.stop);

			try {
				this.checkNeighborsTimeout();
				this.sendDistanceVectorToNeighbors();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//System.out.println("I am here in sendother!");
		byte[] byteMap=Router.serializedCommand("close",50);
		String adr=router.routerInfo.ipAddress.getHostAddress();
		DatagramPacket sendPacket;
		try {
			sendPacket = new DatagramPacket(byteMap, byteMap.length, InetAddress.getByName(adr), router.routerInfo.port);
			router.serverSocket.send(sendPacket);
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Sends the table of distance to the neighbors
	 * @throws IOException
	 */
	private void sendDistanceVectorToNeighbors() throws IOException {

		byte[] byteMap;
		Map<String, PathInfo> orimap;
		Map<String, PathInfo> changedmap=new HashMap<String, PathInfo>();
		synchronized (router.adjacentRouters) {
		for (RouterInfo routerInfo : router.adjacentRouters.values()) {
			if(!routerInfo.equals(null)){
			synchronized (router.minimumPathTable) {
				orimap=router.getDistanceTable();
			
			for (Entry<String, PathInfo> a: orimap.entrySet()) {
				
				PathInfo path=new PathInfo(a.getValue());
				//apply Poison reverse
				if((path.gatewayRouterKey)!=null&&null!=routerInfo.key){
					if(routerInfo.key.equals(path.gatewayRouterKey)&& !(path.destinationRouterKey.equalsIgnoreCase(routerInfo.key))){
						path.cost=Double.MAX_VALUE;
				}
				}
			
				changedmap.put(a.getKey(), path);	
			}
			byte [] head=Router.serializedCommand("Map",50);
			byteMap = Router.serialize(changedmap);
			byte [] data = Arrays.copyOf(head, head.length + byteMap.length);
			System.arraycopy(byteMap, 0, data, head.length, byteMap.length);
			String adr=routerInfo.ipAddress.getHostAddress();
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getByName(adr), routerInfo.port);
			router.serverSocket.send(sendPacket);
			}
		
		}
		}
		}
	}
	/**
	 * Checks if a neighbor has got in touch before the timeout, if not, mark as unavailable
	 * 
	 * @throws IOException
	 */
	
	private void checkNeighborsTimeout() throws IOException {

		long currentTime = System.currentTimeMillis();

		Set<Entry<String, Long>> set;
		synchronized (router.lastPing) {
			set = new HashSet<Entry<String, Long>>(router.lastPing.entrySet());
		}

		boolean changed = false;
		synchronized (router.minimumPathTable) {
			Map<String, PathInfo> myDistanceVector = router.getDistanceTable();
			//check which neighbor is not touched for long time and set to infinity
			for (Entry<String, Long> e : set) {
				
				//if it get lost for long time
				if (e.getValue() > 0 && currentTime - e.getValue() > 3 * Router.TIME_OUT) {
					String id = e.getKey();
					if (myDistanceVector.get(id).cost != Router.INFINITY) {
						myDistanceVector.get(id).cost = Router.INFINITY;
						e.setValue(0L);

						for (PathInfo info : myDistanceVector.values()) {
							if (info.gatewayRouterKey == id) {
								info.gatewayRouterKey = "-1";
								info.cost = Router.INFINITY;
							}
						}

						for(PathInfo path : router.minimumPathTable.get(id).values()) {
							path.cost = Router.INFINITY;
							path.gatewayRouterKey = "-1";
						}

						for (String entry : router.minimumPathTable.keySet()) {
							router.relaxEdges(entry);
						}

						router.out.println("[" + router.routerInfo.key + "], his neighbor  [" + id
								+ "] is time out "+"TIMEOUT= "+"3*"+Router.TIME_OUT/1000+"seconds");

						changed = true;
					}
				}
			}
		}
/*
		if (changed) {
			router.printDistanceTable();
		}
		**/
	}
	
	
}
