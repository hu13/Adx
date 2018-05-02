package Agent;

import adx.variants.twodaysgame.TwoDaysBidBundle;
import adx.variants.twodaysgame.TwoDaysTwoCampaignsAgent;

public class OurBot extends TwoDaysTwoCampaignsAgent {

	
	public OurBot(String name, int port) {
		super(name, port);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		OurBot myAgent = new OurBot("localhost", 9898);
		myAgent.connect("CrazyNickName");
	}

	@Override
	protected TwoDaysBidBundle getBidBundle(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
