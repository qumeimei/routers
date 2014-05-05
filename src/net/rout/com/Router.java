package net.rout.com;

import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Represents the Router and calls the actions to communicate and receive messages from other Routers
 */
public class Router {

	public static double TIME_OUT;
	public  Map<String, Map<String, PathInfo>> minimumPathTable;
	public  RouterInfo routerInfo;
	public  Map<String, LinkInfo> links;
	public  Map<String, RouterInfo> adjacentRouters;
	public Map<String, Long> lastPing;
	public static final double INFINITY = Double.MAX_VALUE;
	public PrintStream out;
	public DatagramSocket serverSocket;
	public boolean stop;

	public Router(RouterInfo routerInfo, Map<String, RouterInfo> adjacentRouters, Map<String, LinkInfo> links, PrintStream out,boolean stop) {
		
		this.routerInfo = routerInfo;
		this.adjacentRouters = adjacentRouters;
		this.links = links;
		this.minimumPathTable = new HashMap<String, Map<String, PathInfo>>();
		this.lastPing = new HashMap<String, Long>();
		this.out = out;
		this.TIME_OUT=routerInfo.timeOut*1000;
		this.stop=stop;
		/**
		 * initialize itself's distanceVector including reach his neighbors and itself.
		 */
		Map<String, PathInfo> distanceVector = new HashMap<String, PathInfo>();
		//build path to its neighbor
		for (LinkInfo info : links.values()) {
			//copy
			PathInfo path = new PathInfo();
			path.cost = info.cost;
			path.destinationRouterKey = info.routerkey;
			path.gatewayRouterKey = path.destinationRouterKey;
			distanceVector.put(path.destinationRouterKey, path);
		}
		//build path to itself
		PathInfo pathToMyself = new PathInfo();
		pathToMyself.cost = 0;
		pathToMyself.destinationRouterKey = routerInfo.key;
		pathToMyself.gatewayRouterKey = pathToMyself.destinationRouterKey;
		distanceVector.put(pathToMyself.destinationRouterKey, pathToMyself);
		this.minimumPathTable.put(this.routerInfo.key, distanceVector);
		this.printDistanceTable();
	}

	public Map<String, PathInfo> getDistanceTable() {
		return this.minimumPathTable.get(this.routerInfo.key);
	}
	
	public void initSocket() throws SocketException, UnknownHostException {
		serverSocket = new DatagramSocket(routerInfo.port, routerInfo.ipAddress);
		System.out.println("Initiate socket! Router's information: " + routerInfo.ipAddress + ":" + routerInfo.port);
	}

	/**
	 * Based on the ip address and port used, this method can identify what is the ID of the router
	 * @param inetAddr
	 * @param port
	 * @return the complete metadata of the router
	 */
	public RouterInfo getAdjacentByIPAndPort(InetAddress inetAddr, int port) {
		for (RouterInfo info : adjacentRouters.values()) {
			if (info.ipAddress.equals(inetAddr) && info.port == port) {
				return info;
			}
		}
		return null;
	}
	
	public void setToinfinity(String changedkey){
		Map<String, PathInfo> myDistanceTable = getDistanceTable();
		for (PathInfo path : myDistanceTable.values()) {
			if (path.gatewayRouterKey.equals(myDistanceTable)&& path.destinationRouterKey != routerInfo.key) {
				path.cost = Router.INFINITY;
			}
		}
		
	}

	/**
	 * Attributes new weights to the edges
	 * 
	 * @param changedVectorRouterID, which is neighbor
	 * @return if there was any information that wasn't already registered
	 */
	
	public boolean relaxEdges(String changedVectorRouterKey) {

		Map<String, PathInfo> myDistanceTable = getDistanceTable();
		Map<String, PathInfo> beforeList = new HashMap<String, PathInfo>();
		for (Entry<String, PathInfo> entry : myDistanceTable.entrySet()) {
			beforeList.put(entry.getKey(), new PathInfo(entry.getValue()));
		}
		
		//****set the path of gatewayRouter( changed neighbor) to infinity
		for (PathInfo path : myDistanceTable.values()) {
			if (path.gatewayRouterKey.equals(changedVectorRouterKey)&& path.destinationRouterKey != routerInfo.key) {
				path.cost = Router.INFINITY;
			}
		}

		Map<String, PathInfo> receivedMap = minimumPathTable.get(changedVectorRouterKey);
		
		
		//set the path to changed neighbors the new weight
		PathInfo pathFromMeToDistanceVectorOwner = myDistanceTable.get(changedVectorRouterKey);
		if (lastPing.containsKey(changedVectorRouterKey) && lastPing.get(changedVectorRouterKey) > 0 && links.containsKey(changedVectorRouterKey)
				&& pathFromMeToDistanceVectorOwner.cost > links.get(changedVectorRouterKey).cost) {
			pathFromMeToDistanceVectorOwner.cost = links.get(changedVectorRouterKey).cost;
			pathFromMeToDistanceVectorOwner.gatewayRouterKey = changedVectorRouterKey;
		}
		
		//add path other than neighbors
		for (PathInfo receivedPath : receivedMap.values()) {
			//if router other than neighbors not exists, created new;
			if (!myDistanceTable.containsKey(receivedPath.destinationRouterKey)) {
				PathInfo newInfo = new PathInfo();
				newInfo.destinationRouterKey = receivedPath.destinationRouterKey;
				newInfo.gatewayRouterKey = changedVectorRouterKey;
				newInfo.cost = receivedPath.cost + pathFromMeToDistanceVectorOwner.cost;//through neighbor
				myDistanceTable.put(newInfo.destinationRouterKey, newInfo);

			//else add compare with older one and add the smaller ones.
			} else {
				PathInfo pathFromMeToDestination = myDistanceTable.get(receivedPath.destinationRouterKey);
				if (pathFromMeToDestination.cost > receivedPath.cost + pathFromMeToDistanceVectorOwner.cost) {
					pathFromMeToDestination.cost = receivedPath.cost + pathFromMeToDistanceVectorOwner.cost;
					pathFromMeToDestination.gatewayRouterKey = changedVectorRouterKey;

				}
			}
		}
		
		Set<PathInfo> currentSet = new HashSet<PathInfo>(myDistanceTable.values());
		Set<PathInfo> previousSet = new HashSet<PathInfo>(beforeList.values());
		return !currentSet.containsAll(previousSet) || !previousSet.containsAll(currentSet);
	}

	/**
	 * Outputs the table of distances
	 */
	public void printDistanceTable() {
		int second, minute, hour;
	    GregorianCalendar date = new GregorianCalendar();
	    second = date.get(Calendar.SECOND);
	    minute = date.get(Calendar.MINUTE);
	    hour = date.get(Calendar.HOUR);
	    
	    out.println("<"+hour+" : "+minute+" : "+second+"> Distance vector list is: ");
		for (PathInfo info : minimumPathTable.get(routerInfo.key).values()) {
			out.println("Destination = " +info.destinationRouterKey+", Cost = "+info.cost+ ", Link = (" +info.gatewayRouterKey+")");
		}
	}

	public static byte[] serializedCommand(String s,int size){
		
		int len=size;
		String white=s;
		byte[] c;
		c=white.getBytes();
		byte[] b=new byte[len-c.length];
		if(c.length<len){
			
			for (int i = 0; i < len-c.length; i++) {
				b[i]=(byte)'@';
			}
		}
		 byte[] result = Arrays.copyOf(c, c.length + b.length);
		  System.arraycopy(b, 0, result, c.length, b.length);
		  String string = new String(result);
		//System.out.println("serialized command's length is "+result.length);
		//System.out.println("serialized command is"+string);
		return string.getBytes();
	}
	
	public static byte[] combinSix(byte[] a,byte[] b,byte[] c,byte[] d,byte[] e,byte[] f){
		
		List<byte[]> places = Arrays.asList(a, b,c,d,e,f);
		byte[] result= new byte[a.length+b.length+c.length+d.length+e.length+f.length];
		for (int i = 0; i < places.size()-1; i++) {
			if(i==0){
				result = Arrays.copyOf(a, a.length + b.length);
				System.arraycopy(b, 0, result, a.length, b.length);
			}
			
			else{
			int resultsize=result.length;
			result = Arrays.copyOf(result, result.length + places.get(i+1).length);
			System.arraycopy(places.get(i+1), 0, result, resultsize, places.get(i+1).length);
			}
			
		}
		  return result;
	}
	
	public static byte[] combinTwo(byte[] a,byte[] b){
		byte[] result = Arrays.copyOf(a, a.length + b.length);
		  System.arraycopy(b, 0, result, a.length, b.length);
		  return result;
		
	}
	/**
	 * Serializes the table of distances to send through the network
	 * 
	 * @param map
	 * @return the serialized data
	 */
	public static byte[] serialize(Map<String, PathInfo> map) {
		StringBuilder bld = new StringBuilder();
		for (PathInfo entry : map.values()) {
			bld.append(entry.toString() + "\n");
		}
		String result = bld.toString();
		return result.substring(0, result.length() - 1).getBytes();
	}

	/**
	 * Deserialize the data and mounts a table of distances
	 * @param array
	 * @return the table of distances
	 */
	public static Map<String, PathInfo> deserialize(byte[] array) {
		String string = new String(array);
		Map<String, PathInfo> map = new HashMap<String, PathInfo>();
		for (String s : string.split("\n")) {
			PathInfo info = PathInfo.buildPathInfo(s);
			map.put(info.destinationRouterKey, info);
		}
		return map;
	}

}

