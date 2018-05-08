package Agent;

import adx.exceptions.AdXException;
import adx.structures.MarketSegment;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class MarketSegmentUtils {

  private static final Map<MarketSegment, Set<MarketSegment>> segs =
          new HashMap<>();
  private static final Map<MarketSegment, Integer> numAttributes = new HashMap<>();

  private static final int NUM_SUBSET_ONE = 9;
  private static final int NUM_SUBSET_TWO = 3;
  private static final int NUM_SUBSET_THREE = 1;
  

  /*
   * return the proportional of people in marketseg m
   */
  static Integer getProportion(MarketSegment m) {
	  return MarketSegment.proportionsMap.get(m);
  }
  
  private MarketSegmentUtils() throws IllegalAccessError {
    throw new IllegalAccessError();
  }

  static int getNumAttributes(MarketSegment segment) {
    return numAttributes.get(segment);
  }

  static Set<MarketSegment> getSmallestSegments(MarketSegment segment) {
    ImmutableSet.Builder<MarketSegment> set = ImmutableSet.builder();

    for (MarketSegment m : segs.get(segment)) {
      if (getNumAttributes(m) != 3) {
        continue;
      }

      set.add(m);
    }

    return set.build();
  }

  static {
    try {
      for (MarketSegment m : MarketSegment.values()) {
        assert (! segs.containsKey(m));

        ImmutableSet.Builder<MarketSegment> set = ImmutableSet.builder();

        for (MarketSegment o : MarketSegment.values()) {
          if (MarketSegment.marketSegmentSubset(m, o)) {
            set.add(o);
          }
        }

        segs.put(m, set.build());
        switch (segs.get(m).size()) {
          case NUM_SUBSET_ONE:
            numAttributes.put(m, 1);
            break;

          case NUM_SUBSET_TWO:
            numAttributes.put(m, 2);
            break;

          case NUM_SUBSET_THREE:
            numAttributes.put(m, 3);
            break;

            default:
              throw new IllegalStateException();
        }
      }
    } catch (AdXException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) {
    for (MarketSegment m : MarketSegment.values()) {
      System.out.println(getSmallestSegments(m));
    }
  }
}
