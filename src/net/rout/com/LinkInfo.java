package net.rout.com;

public class LinkInfo {
   public String routerkey;
   public double cost;
   
   public LinkInfo(LinkInfo inf){
	   this.routerkey=inf.routerkey;
	   this.cost=inf.cost;
   }

   public LinkInfo() {
	
}
   public LinkInfo(String key,double cost) {
	   routerkey=key;
	   this.cost=cost;
}
}

