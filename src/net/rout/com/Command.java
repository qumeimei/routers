package net.rout.com;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Command implements Runnable{
	
	private final Router router;
	private static BufferedReader inputLine = null;
	private static Map<String, RouterInfo> removedRouter=new HashMap<String, RouterInfo>();
	private static int headsize=50;
	private static int filenamesize=50;
	private static int serialsize=10;
	private static int addresssize=500;
	private static int destisize=30;
	
	public Command(Router router) {
		this.router = router;
	}
	
	public void run() {
		inputLine = new BufferedReader(new InputStreamReader(System.in));
		
		 while (router.stop) {
			 try {
				String read=inputLine.readLine().trim();
				
				if(read.toLowerCase().contains("SHOWRT".toLowerCase())){
					router.printDistanceTable();
				}
				
			
				if(read.toLowerCase().contains("LINKDOWN".toLowerCase())){
					String[] s=read.trim().split("\\s+");
					String changedKey=s[1]+":"+s[2];
					LinkInfo likfo=router.links.get(changedKey);
					synchronized (router.links){
					likfo.cost=Double.MAX_VALUE;
					}
					System.out.println("now the cost is "+router.links.get(changedKey).cost);
					synchronized (router.adjacentRouters){
					removedRouter.put(changedKey,router.adjacentRouters.remove(changedKey));
					}
					synchronized (router.minimumPathTable){
						router.setToinfinity(changedKey);
					}
					
					byte[] byteMap;
					String comandDown="LINKDOWN";
					////
					byteMap = Router.serializedCommand(comandDown,headsize);
					////
					String adr=s[1];
					int port=Integer.parseInt(s[2]);
					DatagramPacket sendPacket = new DatagramPacket(byteMap, byteMap.length, InetAddress.getByName(adr), port);
					router.serverSocket.send(sendPacket);
					
				}
			
				if(read.toLowerCase().contains("LINKUP".toLowerCase())){
					String[] s=read.trim().split("\\s+");
					if(s.length!=3){
						System.out.println("Your input did not match! please input again!!!");
						continue;
					}
					
					String[] s1=s[1].split(":");
					String ip=s1[0];
					int port=Integer.parseInt(s1[1]);
					String changedKey=ip+":"+s1[1];
					double cost=Double.parseDouble(s[2]);
					LinkInfo likfo=router.links.get(changedKey);
					synchronized (router.links){
					likfo.cost=cost;
					}
					System.out.println("now the cost is "+router.links.get(changedKey).cost);
					RouterInfo fo=new RouterInfo();
					synchronized (router.adjacentRouters){
					router.adjacentRouters.put(changedKey, removedRouter.get(changedKey));
					}
					byte[] byteMap;
					///
					String comandUp="LINKUP$"+s[2];
					byteMap = Router.serializedCommand(comandUp,headsize);
					////
					DatagramPacket sendPacket = new DatagramPacket(byteMap, byteMap.length, InetAddress.getByName(ip), port);
					router.serverSocket.send(sendPacket);
				}
				
				if(read.toLowerCase().contains("TRANSFER".toLowerCase())){
					String[] s=read.trim().split("\\s+");
					String destiip=s[1].trim();
					int destiPort=Integer.parseInt(s[2]);
					String destiKey=destiip+":"+s[2].trim();
					String fileName=router.routerInfo.fileChunk;
					int sequenceNumber=router.routerInfo.sequenceNumber;
					if(fileName==null||sequenceNumber==0)
					{
						System.out.println("Please double check your input");
						continue;
					}
					byte[] fileBytes;
					byte[] head;
					byte[] filename;
					byte[] serial;
					byte[] dest;
					byte[] process;
					byte[] finaldata;
					String command="TRANSFER";
					head=Router.serializedCommand(command,headsize);
					String soureceFilePath="fileTransfer/"+fileName;
					System.out.println(soureceFilePath);
					File file = new File(soureceFilePath);
					 if (file.isFile()) {
				            try {
				                DataInputStream diStream = new DataInputStream(new FileInputStream(file));
				                long len = (int) file.length();
				               fileBytes = new byte[(int) len];
				               filename=Router.serializedCommand(fileName, filenamesize);
							    serial=Router.serializedCommand(Integer.toString(sequenceNumber), filenamesize);
							    dest=Router.serializedCommand(destiKey, destisize);
							    process=Router.serializedCommand(router.routerInfo.key, destisize);
							    finaldata=Router.combinSix(head,fileBytes,filename, serial, dest, process);
							    
								PathInfo info=router.getDistanceTable().get(destiKey);
								String gate=info.gatewayRouterKey;
								String [] s2=gate.split(":");
								String ip=s2[0];
								int port=Integer.parseInt(s2[1]);
								
								DatagramPacket sendPacket = new DatagramPacket(finaldata, finaldata.length, InetAddress.getByName(ip), port);
								router.serverSocket.send(sendPacket);
				                
				          
				            } catch (Exception e) {
				                e.printStackTrace();
				               
				            }
				        } else {
				            System.out.println("path specified is not pointing to a file");
				        
				        }

				}
				if(read.toLowerCase().contains("CLOSE".toLowerCase())){
					//System.out.println("input close");
					
					router.stop=false;
					break;
				}
				}

		catch (IOException e) {	
				e.printStackTrace();
			}
	}
		 
		// System.out.println("I am here!");

}

}


	