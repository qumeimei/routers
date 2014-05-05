package net.rout.com;

/**
 * Contains the information about how to reach another router
 */
public class PathInfo {

	public String destinationRouterKey;
	public String gatewayRouterKey;
	public double cost;

	public PathInfo() {

	}
	
	//copy class
	public PathInfo(PathInfo info) {
		this.destinationRouterKey = info.destinationRouterKey;
		this.gatewayRouterKey = info.gatewayRouterKey;
		this.cost = info.cost;
	}

	public String toString() {
		return destinationRouterKey + "#" + gatewayRouterKey + "#" + cost;
	}

	/**
	 * Parses a string to create a PathInfo from a serialized source
	 * @param A string that represents a PathInfo in a serialized way (destinationRouterId:gatewayRouterId:cost)
	 * @return the PathInfo object with the data obtained from the string
	 */
	public static PathInfo buildPathInfo(String s) {
		//System.out.println("s is !!!!"+ s);
		String[] strLine = s.split("#");
		PathInfo info = new PathInfo();
		info.destinationRouterKey = strLine[0];
		//System.out.println("destination key is "+strLine[0]);
		info.gatewayRouterKey = strLine[1];
		//System.out.println("gatewayRouter key is "+strLine[1]);
		//System.out.println("double is "+strLine[2]);
		info.cost = Double.parseDouble(strLine[2]);
		return info;
	}
	public boolean equals(Object obj) {
		if (!PathInfo.class.isInstance(obj)) {
			return false;
		}
		PathInfo path = (PathInfo) obj;
		return destinationRouterKey.equals(path.destinationRouterKey)  && gatewayRouterKey.equals(path.gatewayRouterKey) && cost == path.cost;
	}

}
