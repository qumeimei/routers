package net.rout.com;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Command implements Runnable{
	
	private final Router router;
	private static BufferedReader inputLine = null;
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
				else if(read.toLowerCase().contains("CHECKSELF".toLowerCase())){
					System.out.println("You have received "+router.filemap.size()+" files and their name is: ");
					if(router.filename.size()!=0){
						for (String s: router.filename.values()) {
							System.out.println(s);
						}	
					}
					}
	
				else if(read.toLowerCase().contains("LINKDOWN".toLowerCase())){
					String[] s=read.trim().split("\\s+");
					String changedKey=s[1]+":"+s[2];
					LinkInfo likfo=router.links.get(changedKey);
					synchronized (router.links){
					likfo.cost=Double.MAX_VALUE;
					}
					System.out.println("now the cost is set to"+router.links.get(changedKey).cost);
					synchronized (router.adjacentRouters){
						synchronized (router.removedRouter){
					router.removedRouter.put(changedKey,router.adjacentRouters.remove(changedKey));
						}
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
				else if(read.toLowerCase().contains("DELIVERFILE".toLowerCase())){
					String[] s=read.trim().split("\\s+");
					if(s.length!=2){
						System.out.println("Your input did not match! please input again!!!");
						continue;
					}
					String[] s1=s[1].split(":");
					String ip=s1[0];
					int port=Integer.parseInt(s1[1]);
					String destinationKey=ip+":"+s1[1];
					
					byte[] byteMap;
					///
					String comandUp="DELIVERFILE";
					byteMap = Router.serializedCommand(comandUp,headsize);
					////
					DatagramPacket sendPacket = new DatagramPacket(byteMap, byteMap.length, InetAddress.getByName(ip), port);
					router.serverSocket.send(sendPacket);
				}
				
				else if(read.toLowerCase().contains("LINKUP".toLowerCase())){
					String[] s=read.trim().split("\\s+");
					if(s.length!=4){
						System.out.println("Your input did not match! please input again!!!");
						continue;
					}
					
					String ip=s[1];
					int port=Integer.parseInt(s[2]);
					String changedKey=ip+":"+s[2];
					double cost=Double.parseDouble(s[3]);
					LinkInfo likfo=router.links.get(changedKey);
					synchronized (router.links){
					likfo.cost=cost;
					}
					System.out.println("now the cost is set to: "+router.links.get(changedKey).cost);
					synchronized (router.adjacentRouters){
						synchronized (router.removedRouter){
					router.adjacentRouters.put(changedKey, router.removedRouter.get(changedKey));
						}
					}
					byte[] byteMap;
					///
					String comandUp="LINKUP$"+s[2];
					byteMap = Router.serializedCommand(comandUp,headsize);
					////
					DatagramPacket sendPacket = new DatagramPacket(byteMap, byteMap.length, InetAddress.getByName(ip), port);
					router.serverSocket.send(sendPacket);
				}
				else if(read.toLowerCase().contains("TRANSFER".toLowerCase())){
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
					//System.out.println(soureceFilePath);
					File file = new File(soureceFilePath);
					 if (file.isFile()) {
				            try {
				                DataInputStream diStream = new DataInputStream(new FileInputStream(file));
				                long len = (int) file.length();
				                fileBytes = new byte[(int) len];
				                int read1 = 0;
				                int numRead = 0;
				                while (read1 < fileBytes.length && (numRead = diStream.read(fileBytes, read1,
				                        fileBytes.length - read1)) >= 0) {
				                    read1 = read1 + numRead;
				                }
				                String filsize=String.valueOf(fileBytes.length);
				                String command1="TRANSFER#"+filsize;
								head=Router.serializedCommand(command1,headsize);
								
				                filename=Router.serializedCommand(fileName, filenamesize);
							    serial=Router.serializedCommand(Integer.toString(sequenceNumber), serialsize);
							    dest=Router.serializedCommand(destiKey, destisize);
							    process=Router.serializedCommand(router.routerInfo.key, addresssize);
							    /*
							    System.out.println("length of head is"+head.length);
							    System.out.println("length of fileBytes is"+fileBytes.length);
							    System.out.println("length of filename is"+filename.length);
							    System.out.println("length of serial is"+serial.length);
							    System.out.println("length of dest is"+dest.length);
							    System.out.println("length of process is"+process.length);
							    **/
							    finaldata=Router.combinSix(head,fileBytes,filename, serial, dest, process);
							    
								PathInfo info=router.getDistanceTable().get(destiKey);
								String gate=info.gatewayRouterKey;
								String [] s2=gate.split(":");
								String ip=s2[0];
								int port=Integer.parseInt(s2[1]);
								
								DatagramPacket sendPacket = new DatagramPacket(finaldata, finaldata.length, InetAddress.getByName(ip), port);
								System.out.println("file length is "+finaldata.length);
								router.serverSocket.send(sendPacket);
								System.out.println("file sent to "+info.gatewayRouterKey);
								//System.out.println(finaldata.length);
								//System.out.println("head is "+new String(Arrays.copyOfRange(finaldata, 0, 50))+" package sent to"+ip+":"+port);
								String path1=new String(Arrays.copyOfRange(finaldata, finaldata.length-500, finaldata.length));
								String path=path1.substring(0, path1.indexOf('@'));
								//System.out.println("path is "+path);
				            } catch (Exception e) {
				                e.printStackTrace();
				               
				            }
				        } else {
				            System.out.println("path specified is not pointing to a file");
				        
				        }
				}
				else if(read.toLowerCase().contains("CLOSE".toLowerCase())){
					router.stop=false;
					System.out.println("bye~~~~~~~~");
					break;
				}
				}
		catch (IOException e) {	
				e.printStackTrace();
			}
	}
		
}
}


	