package net.rout.com;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class test {

	public static void main(String[] args) throws UnknownHostException {
		
		int len=50;
		String white="LINKUP$5";
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
		System.out.println(result.length);
		System.out.println(string);
		System.out.println(string.indexOf("@"));
		String head=string.substring(0, string.indexOf("@"));
		System.out.println("transfer".toLowerCase());

	}

}
