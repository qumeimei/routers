package net.rout.com;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages the list of routers available on the network
 */
public class RouterTable {

	private  String routerConfigFilePath;
	private Map<String, RouterInfo> routers;
	private Map<String, LinkInfo> links;
	private RouterInfo routerSelf;

	
	public RouterTable(String routerConfigFilePath) {
		this.routerConfigFilePath = routerConfigFilePath;
		this.routers = new HashMap<String, RouterInfo>();
		this.links = new HashMap<String, LinkInfo>();
	}
	


	public void parseConfigFile() throws IOException {
		BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(routerConfigFilePath)));
		String line;
		int n=0;
		while ((line = buf.readLine()) != null) {
			String[] strLine = line.trim().split("\\s+");
			RouterInfo routerinfo = new RouterInfo();
			LinkInfo linkinfo = new LinkInfo();
			double cost=0;
			if(n==0){

				String localIpAddress=InetAddress.getLocalHost().getHostAddress();
				routerinfo.ipAddress=InetAddress.getByName(localIpAddress);
				System.out.println ("********local ip is******* "+localIpAddress);
				routerinfo.port = Integer.parseInt(strLine[0]);
				routerinfo.timeOut=Double.parseDouble(strLine[1]);
				if(strLine.length>2)
				{
					routerinfo.fileChunk=strLine[2];
					routerinfo.sequenceNumber=Integer.parseInt(strLine[3]);
				}
				
				String key=routerinfo.ipAddress.getHostAddress()+":"+Integer.toString(routerinfo.port);
				routerinfo.key=key;
				System.out.println("This router's Key is "+key);
				routerSelf=routerinfo;
			}
			else{
				cost=Double.parseDouble(strLine[1]);
				String[] split = strLine[0].split("\\:");
				String[] split1=split[0].split("\\.");
				byte[] bytedIP = new byte[4];
				for (int i = 0; i < split1.length; i++) {
					bytedIP[i] = (byte) Integer.parseInt(split1[i]);
				}
				routerinfo.ipAddress = InetAddress.getByAddress(bytedIP);
				routerinfo.port=Integer.parseInt(split[1]);
				String key=routerinfo.ipAddress.getHostAddress()+":"+Integer.toString(routerinfo.port);
				routerinfo.key=key;
				routers.put(key, routerinfo);
				linkinfo.routerkey= key;
				linkinfo.cost=cost;
				links.put(key,linkinfo);
			}		
			n++;
		}
		buf.close();
		
	}
	
	public Map<String, RouterInfo> getRouters(){
		return routers;
	}
	public Map<String, LinkInfo> getLinks(){
		return links;
	}
	public RouterInfo getSelfRouter(){
		return routerSelf;
	}


}
