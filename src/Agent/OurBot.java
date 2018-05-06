package Agent;


import java.util.HashSet;
import java.util.Set;

import adx.structures.Campaign;
import adx.structures.MarketSegment;
import adx.structures.SimpleBidEntry;
import adx.variants.twodaysgame.TwoDaysBidBundle;
import adx.variants.twodaysgame.TwoDaysTwoCampaignsAgent;

public class OurBot extends TwoDaysTwoCampaignsAgent {

	private static Campaign _firstCampaign;
	private static Campaign _secondCampaign;
	
	public OurBot(String name, int port) {
		super(name, port);
	}
	
	
	private static void initBot(OurBot bot) {
		_firstCampaign = bot.firstCampaign;
		_secondCampaign = bot.secondCampaign;
	}
	
	public void getFirstMarketSegment() {
		MarketSegment firstMarketSeg = _firstCampaign.getMarketSegment();
		double firstBudget = _firstCampaign.getBudget();
		int firstReach = _firstCampaign.getReach();
		
//		_firstCampaign.
		System.out.printf("Seg=%s Bud=%f Reach=%d", firstMarketSeg, firstBudget, firstReach);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		OurBot myAgent = new OurBot("localhost", 9898);
		initBot(myAgent);
		myAgent.connect("Agent1");
	}

	@Override
	/*
	 * int day: 1 for the first day, sthelse for the second day
	 * (non-Javadoc)
	 * @see adx.variants.twodaysgame.TwoDaysTwoCampaignsAgent#getBidBundle(int)
	 */
	protected TwoDaysBidBundle getBidBundle(int day) {
		// TODO Auto-generated method stub
		int campaignId;
	    if (day == 1) {
	        campaignId = _firstCampaign.getId();
	    } else {
	        campaignId = _secondCampaign.getId();
	    }
	    
	    Set<SimpleBidEntry> simpleBidEntries = new HashSet<>();
	    
		TwoDaysBidBundle bundle = new TwoDaysBidBundle();

		return null;
	}

}
