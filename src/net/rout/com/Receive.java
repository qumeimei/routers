package net.rout.com;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.awt.Image;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;

/**
 * Implements the routine of the router that receives messages from the neighbors and update the local information
 */
public class Receive implements Runnable {

	private final Router router;
	//private static Map<String, RouterInfo> removedRouter=new HashMap<String, RouterInfo>();
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
			//System.out.println("head is "+head);
			/*
			if(!head.toLowerCase().contains("linkdown")&&!head.toLowerCase().contains("map")&&!head.toLowerCase().contains("linkup")){
				System.out.println(head);
				System.out.println("here is receive!! head is: "+head);
			}
			**/
			RouterInfo info = router.getAdjacentByIPAndPort(receivedPacket.getAddress(), receivedPacket.getPort());
			
			if(head.toLowerCase().contains("LINKDOWN".toLowerCase())){
				
				//System.out.println("receive "+head+"from "+ info.key);
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
				router.removedRouter.put(info.key, router.adjacentRouters.remove(info.key));
				}
			}
			
			else if(head.toLowerCase().contains("LINKUP".toLowerCase())){
				
				String key=receivedPacket.getAddress().getHostAddress()+":"+String.valueOf(receivedPacket.getPort());
				
				RouterInfo info1 =router.removedRouter.get(key);
				/*
				System.out.println(key);
				System.out.println(removedRouter.containsKey(key));
				System.out.println("receive: "+head +"from "+ info1.key);
				**/
				String[] s=head.split("\\$");
				//System.out.println(s.length);
				double cost=Double.parseDouble(s[1].trim());
				
				LinkInfo likfo=router.links.get(info1.key);
				likfo.cost=cost;
				//System.out.println("now the cost is "+router.links.get(info1.key).cost);
				synchronized (router.adjacentRouters) {
					synchronized (router.removedRouter) {
				router.adjacentRouters.put(info1.key,router.removedRouter.get(key));}}
			}
			
			else if(head.toLowerCase().contains("close".toLowerCase())){
				
				break;
			}
			else if(head.toLowerCase().contains("SENDFILE".toLowerCase())){
				
				//System.out.println("head is "+head);
				String second=head.substring(head.indexOf("#")+1);
				String filnumber=second.substring(0,second.indexOf("#"));
				String filename=second.substring(second.indexOf("#")+1);
				System.out.println("received file size is "+filnumber);
				System.out.println("received file name is "+filename);
			}
			else if(head.toLowerCase().contains("DELIVERFILE".toLowerCase())){
				
				String fileNumber=String.valueOf(router.filename.size());
				String fileNames="";
				for (String b : router.filename.values()) {
					fileNames=fileNames+":"+b+" ";
				}
				
				String path3="SENDFILE#"+fileNumber+"#"+fileNames;
				byte [] pathb=Router.serializedCommand(path3, 500);
				//System.out.println("receivedByte is "+receivedByte.length);
				
				
				//byte [] finaldata=Router.combinTwo(Arrays.copyOfRange(receivedByte, 0, 140+filesize),pathb);
				//System.out.println("length1 is "+Arrays.copyOfRange(receivedByte, 0, 140+filesize).length+"length2 is "+pathb.length);
				DatagramPacket sendPacket;
				try {
					//System.out.println(finaldata.length);
					sendPacket = new DatagramPacket(pathb, pathb.length,receivedPacket.getAddress(), receivedPacket.getPort());
					router.serverSocket.send(sendPacket);
				} catch (IOException e) {
				
					e.printStackTrace();
				}
				
				
			}
			else if(head.toLowerCase().contains("map".toLowerCase())){
			Map<String, PathInfo> receivedMap = Router.deserialize(content);
			if (info == null) {
				continue;
			}
			updatePingTable(info.key);

			synchronized (router.minimumPathTable) {
				router.minimumPathTable.put(info.key, receivedMap);
			}
			boolean changed;
			synchronized (router.minimumPathTable) {
				changed = router.relaxEdges(info.key);
			}
			}
			else if(head.toLowerCase().contains("TRANSFER".toLowerCase())){
				
				int filesize=Integer.parseInt(head.substring(head.indexOf("#")+1,head.length()));
				String filename1=new String(Arrays.copyOfRange(receivedByte, filesize+50, filesize+100));
				filename1=filename1.substring(0, filename1.indexOf("@"));
				//System.out.println("filename is "+filename1);
				
				String serialnumber=new String(Arrays.copyOfRange(receivedByte, filesize+100, filesize+110));
				int SN=Integer.parseInt(serialnumber.substring(0,serialnumber.indexOf('@')));
			
				
				byte[] process=Arrays.copyOfRange(receivedByte, 140+filesize, receivedByte.length);
				String path1=new String(process);
				String path=path1.substring(0, path1.indexOf('@'));
				//System.out.println("path is "+path);
				 
				byte[] desti=Arrays.copyOfRange(receivedByte,filesize+110,filesize+140);
				String dest = new String(desti);
				dest=dest.substring(0,dest.indexOf("@"));
				//System.out.println("destination key is "+dest);
				
				if(dest.trim().equals(router.routerInfo.key))
				{
					 String outputFile = "ReceivedFile/"+filename1;
					 //System.out.println("outputfile : is " + outputFile);
				        File dstFile = new File(outputFile);
				        FileOutputStream fileOutputStream = null;
				        try {
				            fileOutputStream = new FileOutputStream(dstFile);
				            fileOutputStream.write(Arrays.copyOfRange(receivedByte, 50, 50+filesize));
				            fileOutputStream.flush();
				            fileOutputStream.close();
				            
				    		int second, minute, hour;
				    	    GregorianCalendar date = new GregorianCalendar();
				    	    second = date.get(Calendar.SECOND);
				    	    minute = date.get(Calendar.MINUTE);
				    	    hour = date.get(Calendar.HOUR);
				    	    System.out.println("*****************************************");
				    	    System.out.println("********Receive file at hour " +hour+"minute "+minute+"second "+second);
				            System.out.println("Output file : " + outputFile + " is successfully saved ");
				            System.out.println("trunk name is " + filename1+", trunk serial number is" +SN+"trunk size is "+filesize);
				            System.out.println("Path is " + path);
				            System.out.println("*****************************************");
				            router.filemap.put(SN, Arrays.copyOfRange(receivedByte, 50, 50+filesize));
				            router.filename.put(SN, filename1);
				            if(router.filemap.size()==2){
				            	byte[] one=router.filemap.get(1);
				            	byte[] two=router.filemap.get(2);
				            	byte[] result=router.combinTwo(one, two);
				            	String outputFile1 = "ReceivedFile/output";
				            	File dstFile1 = new File(outputFile1);
				            	fileOutputStream = new FileOutputStream(dstFile1);
					            fileOutputStream.write(result);
					            fileOutputStream.flush();
					            fileOutputStream.close();
					            
					            LoadAnshowImageFromFile(outputFile1);
				            } 
				        } catch (FileNotFoundException e) {
				            e.printStackTrace();
				        } catch (IOException e) {
				            e.printStackTrace();
				        }
				}
				else{
				PathInfo info1=router.getDistanceTable().get(dest); 
				String ip=info1.gatewayRouterKey.split(":")[0];
				int port=Integer.parseInt(info1.gatewayRouterKey.split(":")[1]);
				String path3=path+"#"+router.routerInfo.key;
				
				byte [] pathb=Router.serializedCommand(path3, 500);
				//System.out.println("receivedByte is "+receivedByte.length);
				
				
				byte [] finaldata=Router.combinTwo(Arrays.copyOfRange(receivedByte, 0, 140+filesize),pathb);
				//System.out.println("length1 is "+Arrays.copyOfRange(receivedByte, 0, 140+filesize).length+"length2 is "+pathb.length);
				DatagramPacket sendPacket;
				try {
					//System.out.println(finaldata.length);
					sendPacket = new DatagramPacket(finaldata, finaldata.length, InetAddress.getByName(ip), port);
					router.serverSocket.send(sendPacket);
				} catch (IOException e) {
				
					e.printStackTrace();
				}
				

				}
			}
	
		}
	}
	
	private void LoadAnshowImageFromFile(String s){
		
		Image image = null;
        try {
        	image = ImageIO.read(new File(s));
        } catch (IOException e) {
        	e.printStackTrace();
        }
        JFrame frame = new JFrame();
        frame.setSize(300, 300);
        JLabel label = new JLabel(new ImageIcon(image));
        frame.add(label);
        frame.setVisible(true);
	}
	/**
	 * Listen to the port and capture the packets
	 */
	private DatagramPacket receiveData() throws IOException {
		byte[] receiveData = new byte[1024*2*1024];//set the package size to 2M
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		router.serverSocket.receive(receivePacket);
		return receivePacket;
	}

	/**
	 * Updates the timestamp of the last message from a router
	 * @param routerID
	 */
	private void updatePingTable(String routerID) {
		synchronized (router.lastPing) {
			router.lastPing.put(routerID, System.currentTimeMillis());
		}
	}

}

