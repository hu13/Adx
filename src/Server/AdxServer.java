package Server;

import java.io.IOException;

import adx.exceptions.AdXException;
import adx.util.Logging;
import adx.variants.twodaysgame.TwoDaysTwoCampaignsGameServer;


//Note: The port number is fixed to 9898
public class AdxServer {
	  public static void main(String[] args) throws AdXException {
		    try {
		      // Try to initialize the server.
		    	TwoDaysTwoCampaignsGameServer Server = new TwoDaysTwoCampaignsGameServer(0000);
		      Server.main(null);
		    } catch (IOException e) {
		      e.printStackTrace();
		      System.exit(-1);
		    }
		  }
}
