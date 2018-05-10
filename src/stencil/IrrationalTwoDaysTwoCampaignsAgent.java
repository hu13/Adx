package stencil;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import adx.exceptions.AdXException;
import adx.structures.MarketSegment;
import adx.structures.SimpleBidEntry;
import adx.variants.twodaysgame.TwoDaysBidBundle;
import adx.variants.twodaysgame.TwoDaysTwoCampaignsAgent;

import java.util.HashSet;
import java.util.Set;

public class IrrationalTwoDaysTwoCampaignsAgent extends TwoDaysTwoCampaignsAgent {
  public IrrationalTwoDaysTwoCampaignsAgent(String host, int port) {
    super(host, port);
  }

  protected TwoDaysBidBundle getBidBundle(int day) {
    try {
      double irrationalBid = 0.9D;
      Set<SimpleBidEntry> simpleBidEntries = new HashSet();
      simpleBidEntries.add(new SimpleBidEntry(MarketSegment.FEMALE_OLD_HIGH_INCOME, irrationalBid, 1.7976931348623157E308D));
      simpleBidEntries.add(new SimpleBidEntry(MarketSegment.FEMALE_OLD_LOW_INCOME, irrationalBid, 1.7976931348623157E308D));
      simpleBidEntries.add(new SimpleBidEntry(MarketSegment.FEMALE_YOUNG_HIGH_INCOME, irrationalBid, 1.7976931348623157E308D));
      simpleBidEntries.add(new SimpleBidEntry(MarketSegment.FEMALE_YOUNG_LOW_INCOME, irrationalBid, 1.7976931348623157E308D));
      simpleBidEntries.add(new SimpleBidEntry(MarketSegment.MALE_OLD_HIGH_INCOME, irrationalBid, 1.7976931348623157E308D));
      simpleBidEntries.add(new SimpleBidEntry(MarketSegment.MALE_OLD_LOW_INCOME, irrationalBid, 1.7976931348623157E308D));
      simpleBidEntries.add(new SimpleBidEntry(MarketSegment.MALE_YOUNG_HIGH_INCOME, irrationalBid, 1.7976931348623157E308D));
      simpleBidEntries.add(new SimpleBidEntry(MarketSegment.MALE_YOUNG_LOW_INCOME, irrationalBid, 1.7976931348623157E308D));
      int campaignId;
      if (day == 1) {
        campaignId = this.firstCampaign.getId();
      } else {
        campaignId = this.secondCampaign.getId();
      }

      return new TwoDaysBidBundle(day, campaignId, 1.7976931348623157E308D, simpleBidEntries);
    } catch (AdXException var6) {
      var6.printStackTrace();
      return null;
    }
  }

  public static void main(String[] args) {
    IrrationalTwoDaysTwoCampaignsAgent agent = new IrrationalTwoDaysTwoCampaignsAgent("localhost", 9898);
    agent.connect("Agent10");
    while (true) {;}
  }
}

