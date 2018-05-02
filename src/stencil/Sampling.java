package stencil;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import adx.exceptions.AdXException;
import adx.structures.Campaign;
import adx.structures.MarketSegment;
import adx.structures.Query;
import adx.util.Pair;
import adx.util.Parameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public class Sampling {
  public static final Map<MarketSegment, Integer> segmentsToSample;
  public static ArrayList<Entry<MarketSegment, Integer>> cumulativeMarketSegments;
  public static Integer totalProportion = 0;
  private static final double[] campaignReachFactor = new double[]{0.2D, 0.4D, 0.6D};
  private static int campaignId = 0;
  public static final Random random = new Random();

  static {
    HashSet<MarketSegment> setOfSegments = new HashSet();
    setOfSegments.add(MarketSegment.MALE_YOUNG_LOW_INCOME);
    setOfSegments.add(MarketSegment.MALE_YOUNG_HIGH_INCOME);
    setOfSegments.add(MarketSegment.MALE_OLD_LOW_INCOME);
    setOfSegments.add(MarketSegment.MALE_OLD_HIGH_INCOME);
    setOfSegments.add(MarketSegment.FEMALE_YOUNG_LOW_INCOME);
    setOfSegments.add(MarketSegment.FEMALE_YOUNG_HIGH_INCOME);
    setOfSegments.add(MarketSegment.FEMALE_OLD_LOW_INCOME);
    setOfSegments.add(MarketSegment.FEMALE_OLD_HIGH_INCOME);
    HashMap<MarketSegment, Integer> initSegmentsToSample = new HashMap();
    Iterator var3 = setOfSegments.iterator();

    MarketSegment m;
    while(var3.hasNext()) {
      m = (MarketSegment)var3.next();
      initSegmentsToSample.put(m, (Integer)MarketSegment.proportionsMap.get(m));
    }

    segmentsToSample = Collections.unmodifiableMap(initSegmentsToSample);
    cumulativeMarketSegments = new ArrayList();
    var3 = segmentsToSample.keySet().iterator();

    while(var3.hasNext()) {
      m = (MarketSegment)var3.next();
      totalProportion = totalProportion + (Integer)segmentsToSample.get(m);
      cumulativeMarketSegments.add(new SimpleEntry(m, totalProportion));
    }

  }

  public Sampling() {
  }

  public static final HashMap<Query, Integer> sampleAndBucketPopulation(int n) throws AdXException {
    HashMap<Query, Integer> population = new HashMap();
    Iterator var3 = segmentsToSample.keySet().iterator();

    while(var3.hasNext()) {
      MarketSegment m = (MarketSegment)var3.next();
      population.put(new Query(m), 0);
    }

    for(int i = 0; i < n; ++i) {
      int r = random.nextInt(totalProportion) + 1;
      Iterator var5 = cumulativeMarketSegments.iterator();

      while(var5.hasNext()) {
        Entry<MarketSegment, Integer> x = (Entry)var5.next();
        if (r <= (Integer)x.getValue()) {
          Query query = new Query((MarketSegment)x.getKey());
          population.put(query, (Integer)population.get(query) + 1);
          break;
        }
      }
    }

    return population;
  }

  public static final List<Query> samplePopulation(int n) throws AdXException {
    List<Query> samplePopulation = new ArrayList();

    for(int i = 0; i < n; ++i) {
      int r = random.nextInt(totalProportion) + 1;
      Iterator var5 = cumulativeMarketSegments.iterator();

      while(var5.hasNext()) {
        Entry<MarketSegment, Integer> x = (Entry)var5.next();
        if (r <= (Integer)x.getValue()) {
          samplePopulation.add(new Query((MarketSegment)x.getKey()));
          break;
        }
      }
    }

    return samplePopulation;
  }

  public static Campaign sampleInitialCampaign() throws AdXException {
    Campaign initialCampaign = sampleCampaign(0);
    initialCampaign.setBudget((double)initialCampaign.getReach());
    return initialCampaign;
  }

  public static Campaign sampleCampaign(int day) throws AdXException {
    return sampleCampaignOpportunityMessage(day);
  }

  public static List<Campaign> sampleCampaingList(int day, int n) throws AdXException {
    ArrayList<Campaign> campaignsList = new ArrayList();

    for(int i = 0; i < n; ++i) {
      campaignsList.add(sampleCampaign(day));
    }

    return campaignsList;
  }

  public static Campaign sampleCampaignOpportunityMessage(int day) throws AdXException {
    Pair<MarketSegment, Integer> randomMarketSegmentPair = sampleMarketSegment();
    MarketSegment randomMarketSegment = (MarketSegment)randomMarketSegmentPair.getElement1();
    int sizeOfRandomSegment = (Integer)randomMarketSegmentPair.getElement2();
    double randomReachFactor = campaignReachFactor[random.nextInt(campaignReachFactor.length)];
    int randomDuration = (Integer)Parameters.CAMPAIGN_DURATIONS.get(random.nextInt(Parameters.CAMPAIGN_DURATIONS.size()));
    int reach = (int)Math.floor(randomReachFactor * (double)sizeOfRandomSegment * (double)randomDuration);
    return new Campaign(getUniqueCampaignId(), day + 1, day + randomDuration, randomMarketSegment, reach);
  }

  public static Pair<MarketSegment, Integer> sampleMarketSegment() {
    Entry<MarketSegment, Integer> entry = (Entry)MarketSegment.proportionsList.get(random.nextInt(MarketSegment.proportionsList.size()));
    return new Pair((MarketSegment)entry.getKey(), (Integer)entry.getValue());
  }

  public static int getUniqueCampaignId() {
    return ++campaignId;
  }

  public static void resetUniqueCampaignId() {
    campaignId = 0;
  }
}

