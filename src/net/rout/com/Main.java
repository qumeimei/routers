package net.rout.com;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Initializes the environment and execute the router algorithm
 */
public class Main {
	public static final String CONFIG_DIR="configFile\\";
	public static final String FILE_TRAN_DIR="fileTransfer\\";
	public static void main(String[] args) throws IOException, InterruptedException {

		if (args.length < 1) {
			System.err.println("Please input configure file!");
			System.exit(1);
		}
		
		String config=args[0];
		String filepath=CONFIG_DIR+config;
		//table store router information
		RouterTable routerTable = new RouterTable(filepath);
		routerTable.parseConfigFile();

		RouterInfo currentRouterInfo = routerTable.getSelfRouter();
		Map<String, LinkInfo> links=routerTable.getLinks();
		Map<String, RouterInfo> routers=routerTable.getRouters();
		if (currentRouterInfo == null) {
			System.err.println("No configuration was found for theis router!");
			System.exit(3);
		}
		Router router = new Router(currentRouterInfo, routers, links, System.out,true);
		router.initSocket();
		
		new Thread(new Receive(router)).start();
		new Thread(new SendOther(router)).start();
		new Thread(new Command(router)).start();
	}
}
