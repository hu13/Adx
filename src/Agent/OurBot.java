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

public class OurBot extends TwoDaysTwoCampaignsAgent implements Runnable {

	private static Campaign _firstCampaign;
	private static Campaign _secondCampaign;
	private Thread _ourBotThread;
	private String NAME = "[OurBot:]";

	public OurBot(String name, int port) {
		super(name, port);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
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
		double bid = 0.9D;
		double ratio = budget / (double) reach;
		if (ratio > 1.0D) {
			bid = 0.9D*ratio;
		}
		return bid;
	}
	
	private double normalizeBiasedProportion(double a, double b) {
		return Math.min(a, b) / (a+b);
	}
	
	private void simpleBidStrat1(int day, Campaign campaign, Set<SimpleBidEntry> simpleBidEntries) throws AdXException {
		log("---- day " + day + " ----");
		
		boolean shade = true;
		if (day == 1) {
			// don't shade:
			shade = false;
		}
		
		MarketSegment m1 = campaign.getMarketSegment();
		int reach = campaign.getReach();
		double budget = campaign.getBudget();
		double rand = randomRange(0.90D, 0.95D);
		if (MarketSegmentUtils.getNumAttributes(m1) == 3) {
			log("[Assign 3 attributes seg: " + m1 + "]");
			// assign a 3 attr campaign -> bid everything on this segment
			double bid = budget / (double) reach;
			if (shade) {
				bid = shadeBid(budget, reach);
			}
			
			log("Budget/Reach Ratio: " + budget/(double) reach + " Bid: " + bid + " Random: " + rand);
			simpleBidEntries.add(new SimpleBidEntry(m1, bid, 0.95D*campaign.getBudget()));
		} else {
			// assign a 2 attr campaign
			log("[Assign 2 attributes seg: " + m1 + "]");
			Set<MarketSegment> segments = MarketSegmentUtils.getSmallestSegments(m1);

			// total users per day
			int total = 10000;
			
			// choose how much to bid on sub segments:
			double[] p = new double[2]; // proportion of users in subsegment
			int i = 0;
			for (MarketSegment sm: segments) {
				p[i] = (double) MarketSegmentUtils.getProportion(sm)/total;
				log("Bidding on ms: " + sm + " with subportion: " + p[i++]);
			}
			for (MarketSegment sm: segments) {
				double fraction = normalizeBiasedProportion(p[0],p[1]);
				if (Double.compare((double) MarketSegmentUtils.getProportion(sm)/total, Math.max(p[0], p[1]))==0) {
					fraction = 1D - fraction;
				}
				double b =  budget * fraction;
				double r = Math.ceil(reach * fraction);
				double bid = b / r;
				if (shade) {
					bid = shadeBid(b, (int) r);
				}
				log("bid: " + bid + " proportion: " + fraction + " budget: " + b + " reach: " + r + " random: " + rand);
				simpleBidEntries.add(new SimpleBidEntry(sm, bid, 0.95D*b));
			}
		}
	}
	
	private void simpleBidStrat(int day, Campaign campaign, Set<SimpleBidEntry> simpleBidEntries) throws AdXException {
		log("---- day " + day + " ----");
		
		boolean shade = true;
		if (day == 1) {
			// don't shade:
			shade = false;
		}
		
		MarketSegment m1 = campaign.getMarketSegment();
		double rand = randomRange(0.90D, 0.95D);
		if (MarketSegmentUtils.getNumAttributes(m1) == 3) {
			log("[Assign 3 attributes seg: " + m1 + "]");
			// assign a 3 attr campaign -> bid everything on this segment
			double bid = campaign.getBudget()/ (double) campaign.getReach();
			if (shade) {
				bid = shadeBid(campaign.getBudget(), campaign.getReach());
			}
			
			log("Budget/Reach Ratio: " + campaign.getBudget()/(double) campaign.getReach() + " Bid: " + bid + " Random: " + rand);
			simpleBidEntries.add(new SimpleBidEntry(m1, bid, 0.95D*campaign.getBudget()));
		} else {
			// assign a 2 attr campaign
			log("[Assign 2 attributes seg: " + m1 + "]");
			Set<MarketSegment> segments = MarketSegmentUtils.getSmallestSegments(m1);
			// choose how much to bid on sub segments:
			
			int total = MarketSegmentUtils.getProportion(m1);
			int reach = campaign.getReach();
			for (MarketSegment sm: segments) {
				int subPortion = MarketSegmentUtils.getProportion(sm);
				log("Bidding on ms: " + sm + " with subportion: " + subPortion);
				double proportion = (double) subPortion/total;
				
				double b = campaign.getBudget() * proportion;
				double r = Math.ceil(reach * proportion);
				double bid = b / r;
				if (shade) {
					bid = shadeBid(b, (int) r);
				}
				log("bid: " + bid + " proportion: " + proportion + " budget: " + b + " reach: " + r + " random: " + rand);
				simpleBidEntries.add(new SimpleBidEntry(sm, bid, 0.95D*b));
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
				simpleBidStrat1(day, _firstCampaign, simpleBidEntries);
				campaignId = _firstCampaign.getId();

			} else {
				simpleBidStrat1(day, _secondCampaign, simpleBidEntries);
				campaignId = _secondCampaign.getId();
			}

			return new TwoDaysBidBundle(day, campaignId, 1.7976931348623157E308D, simpleBidEntries);

		} catch (AdXException err) {
			err.printStackTrace();
			return null;
		}
		
	}


	public void start() {
		// TODO Auto-generated method stub
//		OurBot bot = new OurBot("localhost", 9898);
		if (_ourBotThread == null) {
			_ourBotThread = new Thread(this, "OurBotThread");
			_ourBotThread.start();
		}
	}
	
	@Override
	public void run() {
		this.connect("Agent18");
		while (true) {}
	}

}
