package Agent;


import java.util.HashSet;
import java.util.Set;

import adx.exceptions.AdXException;
import adx.structures.Campaign;
import adx.structures.MarketSegment;
import adx.structures.SimpleBidEntry;
import adx.variants.twodaysgame.TwoDaysBidBundle;
import adx.variants.twodaysgame.TwoDaysTwoCampaignsAgent;
import adx.util.Logging;

public class OurBot extends TwoDaysTwoCampaignsAgent {

	private static Campaign _firstCampaign;
	private static Campaign _secondCampaign;
	private String NAME = "[OurBot:]";

	public OurBot(String name, int port) {
		super(name, port);
	}

	public static void main(String[] args) {
		new OurBot("localhost", 9898).connect("Agent18");
		while (true) {}
	}
	
	private void log(String msg) {
		Logging.log(NAME + " " + msg);
	}
		
	private double randomRange(double min, double max) {
		return Math.random()*(max-min) + min;
	}
	
	private double shadeBid(double budget, int reach) {
		double shade = 0.916656D;
		double bid = shade;
		double ratio = budget / (double) reach;
		if (ratio > 1.0D) {
			bid = shade*ratio;
		}
		return bid;
	}
		
	private void simpleBidStrat(int day, Campaign campaign, Set<SimpleBidEntry> simpleBidEntries) throws AdXException {
		log("---- day " + day + " ----");
		
		boolean shade = true;
		double maxBudget = campaign.getBudget();
		if (day == 1) {
			// don't shade:
			shade = false;
			// over budget for high quality score
			maxBudget *= randomRange(1.0,1.01); 
		}
		log("[Max Budget: " + maxBudget + "]");
		MarketSegment m = campaign.getMarketSegment();
		double rand = randomRange(0.90D, 0.95D);
		if (MarketSegmentUtils.getNumAttributes(m) == 3) {
			log("[Assign 3 attributes seg: " + m + "]");
			// assign a 3 attr campaign -> bid everything on this segment
			double bid = campaign.getBudget()/ (double) campaign.getReach();
			if (shade) {
				bid = shadeBid(campaign.getBudget(), campaign.getReach());
			}
			
			log("Budget/Reach Ratio: " + campaign.getBudget()/(double) campaign.getReach() + " Bid: " + bid + " Random: " + rand);
			simpleBidEntries.add(new SimpleBidEntry(m, bid, maxBudget));
		} else {
			// assign a 2 attr campaign
			log("[Assign 2 attributes seg: " + m + "]");
			Set<MarketSegment> segments = MarketSegmentUtils.getSmallestSegments(m);
			// choose how much to bid on sub segments:
			
			int total = MarketSegmentUtils.getProportion(m);
			int reach = campaign.getReach();
			for (MarketSegment sm: segments) {
				int subPortion = MarketSegmentUtils.getProportion(sm);
				log("Bidding on ms: " + sm + " with subportion: " + subPortion);
				double proportion = (double) subPortion/total;
				
				double b = maxBudget * proportion;
				double r = Math.ceil(reach * proportion);
				double bid = b / r;
				if (shade) {
					bid = shadeBid(b, (int) r);
				}

				log("bid: " + bid + " proportion: " + proportion + " budget: " + b + " reach: " + r + " random: " + rand);
				simpleBidEntries.add(new SimpleBidEntry(sm, bid, b));
			}
			
		}
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
		Set<SimpleBidEntry> simpleBidEntries = new HashSet<>();
		_firstCampaign = this.firstCampaign;
		_secondCampaign = this.secondCampaign;
		try {
			if (day == 1) {
				simpleBidStrat(day, _firstCampaign, simpleBidEntries);
				campaignId = _firstCampaign.getId();

			} else {
				simpleBidStrat(day, _secondCampaign, simpleBidEntries);
				campaignId = _secondCampaign.getId();
			}

			return new TwoDaysBidBundle(day, campaignId, 1.7976931348623157E308D, simpleBidEntries);

		} catch (AdXException err) {
			err.printStackTrace();
			return null;
		}
		
	}
}
