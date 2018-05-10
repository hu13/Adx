package stencil;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import adx.exceptions.AdXException;
import adx.structures.Campaign;
import adx.structures.SimpleBidEntry;
import adx.util.Logging;
import adx.variants.twodaysgame.TwoDaysBidBundle;
import adx.variants.twodaysgame.TwoDaysTwoCampaignsAgent;

import java.util.HashSet;
import java.util.Set;

public class SimpleTwoDaysTwoCampaignsAgent extends TwoDaysTwoCampaignsAgent {
  public SimpleTwoDaysTwoCampaignsAgent(String host, int port) {
    super(host, port);
  }

  protected TwoDaysBidBundle getBidBundle(int day) {
    try {
      Campaign c = null;
      if (day == 1) {
        Logging.log("[-] Bid for first campaign which is: " + this.firstCampaign);
        c = this.firstCampaign;
      } else {
        if (day != 2) {
          throw new AdXException("[x] Bidding for invalid day " + day + ", bids in this game are only for day 1 or 2.");
        }

        Logging.log("[-] Bid for second campaign which is: " + this.secondCampaign);
        c = this.secondCampaign;
      }

      Set<SimpleBidEntry> bidEntries = new HashSet();
      bidEntries.add(new SimpleBidEntry(c.getMarketSegment(), c.getBudget() / (double)c.getReach(), c.getBudget()));
      Logging.log("[-] bidEntries = " + bidEntries);
      return new TwoDaysBidBundle(day, c.getId(), 40.256D, bidEntries);
    } catch (AdXException var4) {
      Logging.log("[x] Something went wrong getting the bid bundle: " + var4.getMessage());
      return null;
    }
  }

  public static void main(String[] args) {
    SimpleTwoDaysTwoCampaignsAgent agent = new SimpleTwoDaysTwoCampaignsAgent("localhost", 9898);
    agent.connect("Agent11");
    while (true) {;}
  }
}
