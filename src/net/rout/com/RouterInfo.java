package net.rout.com;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class RouterInfo {

	public int port;
	public InetAddress ipAddress;
	public String key;
	public String fileChunk;
	public int sequenceNumber;
	public double timeOut;
	public Set<LinkInfo> links;
	
	public RouterInfo (){}
	public RouterInfo (RouterInfo info){
		this.key=info.key;
		this.port=info.port;
		this.ipAddress=info.ipAddress;
		this.fileChunk=info.fileChunk;
		this.sequenceNumber=info.sequenceNumber;
		this.timeOut=info.timeOut;
		links=new HashSet<LinkInfo>();
		for (LinkInfo e : info.links) {
			LinkInfo b=new LinkInfo(e);
			links.add(b);
		}
		
	}
}
