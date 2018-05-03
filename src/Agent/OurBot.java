package Agent;

import adx.structures.MarketSegment;
import adx.structures.SimpleBidEntry;
import adx.variants.twodaysgame.TwoDaysBidBundle;
import adx.variants.twodaysgame.TwoDaysTwoCampaignsAgent;

public class OurBot extends TwoDaysTwoCampaignsAgent {

	
	public OurBot(String name, int port) {
		super(name, port);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		OurBot myAgent = new OurBot("localhost", 4567);
//		myAgent.connect("Agent1");
	}

	@Override
	protected TwoDaysBidBundle getBidBundle(int day) {
		// TODO Auto-generated method stub
		MarketSegment segment = firstCampaign.getMarketSegment();
		return null;
	}

}
