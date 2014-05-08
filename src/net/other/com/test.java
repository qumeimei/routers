package net.other.com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
 
public class test {
 
	public static void main(String[] args) throws IOException {
		InetAddress localhost = InetAddress.getLocalHost();
		System.out.println("getLocalHost:" + localhost);
		System.out.println(NetworkInterface.getNetworkInterfaces());
	}
}