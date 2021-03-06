package org.trd.app.teknichrono.model.manage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.trd.app.teknichrono.business.view.LapTimeConverter;
import org.trd.app.teknichrono.business.view.LapTimeFiller;
import org.trd.app.teknichrono.business.view.LapTimeFilter;
import org.trd.app.teknichrono.business.view.LapTimeOrder;
import org.trd.app.teknichrono.model.compare.LapTimeStartComparator;
import org.trd.app.teknichrono.model.dto.LapTimeDTO;
import org.trd.app.teknichrono.model.dto.TestLapTimeDTOCreator;
import org.trd.app.teknichrono.model.jpa.LapTime;
import org.trd.app.teknichrono.model.jpa.TestLapTimeCreator;

import java.util.ArrayList;
import java.util.List;

public class TestLapTimeOrder {


  private TestLapTimeDTOCreator dtoCreator;
  private TestLapTimeCreator creator;

  @Before
  public void setUp() throws Exception {
    dtoCreator = new TestLapTimeDTOCreator();
    creator = new TestLapTimeCreator();
  }

  @Test
  public void ordersByStartDate() {
    List<LapTime> laps = creator.createLapsWithIntermediates();
    laps.sort(new LapTimeStartComparator());
    long previousStart = -1;
    boolean lastStartAvailable = false;
    for (LapTime l : laps) {
      if (l.getStartDate() != null) {
        Assert.assertTrue("Last seen date in incorrect order", l.getStartDate().getTime() >= previousStart);
        previousStart = l.getStartDate().getTime();
        lastStartAvailable = true;
      } else {
        Assert.assertFalse("There has been a laptime with time before and " +
            "this one has no time. Laps was no time are considered the oldest.", lastStartAvailable);
      }
    }
  }


  @Test
  public void ordersDtoByStartDateOrEndDate() {
    LapTimeOrder testMe = new LapTimeOrder();
    long time = System.currentTimeMillis();
    List<LapTimeDTO> result = new ArrayList<>();
    dtoCreator.nextPilot();
    result.add(dtoCreator.createDTOLapTime(time, time + 1000, 1));
    result.add(dtoCreator.createDTOLapTime(time, time + 100, 2));
    result.add(dtoCreator.createDTOLapTime(time, time + 10, 3));
    dtoCreator.nextPilot();
    result.add(dtoCreator.createDTOLapTime(time, time + 1, 1));
    dtoCreator.nextPilot();
    result.add(dtoCreator.createDTOLapTime(time, time + 100000, 1));
    dtoCreator.nextPilot();
    result.add(dtoCreator.createDTOLapTime(time, -1, 1));
    dtoCreator.nextPilot();
    result.add(dtoCreator.createDTOLapTime(time + 10, time + 400, 1));
    dtoCreator.nextPilot();
    result.add(dtoCreator.createDTOLapTime(time + 20, time + 410, 1));
    testMe.orderbyLastSeen(result);
    org.junit.Assert.assertEquals(8, result.size());

    org.junit.Assert.assertEquals(6, result.get(0).getId());
    org.junit.Assert.assertEquals(4, result.get(1).getId());
    org.junit.Assert.assertEquals(3, result.get(2).getId());
    org.junit.Assert.assertEquals(2, result.get(3).getId());
    org.junit.Assert.assertEquals(7, result.get(4).getId());
    org.junit.Assert.assertEquals(8, result.get(5).getId());
    org.junit.Assert.assertEquals(1, result.get(6).getId());
    org.junit.Assert.assertEquals(5, result.get(7).getId());
  }

  @Test
  public void ordersByStartDateOrEndDateWithIncompleteLaps() {
    LapTimeOrder testMe = new LapTimeOrder();
    List<LapTimeDTO> lapsWithIntermediates = dtoCreator.createLapsWithIntermediates();
    int nbLapsBefore = lapsWithIntermediates.size();
    testMe.orderbyLastSeen(lapsWithIntermediates);

    org.junit.Assert.assertEquals(nbLapsBefore, lapsWithIntermediates.size());
    checkOrderByDateWithIntermediates(lapsWithIntermediates);
  }


  private void checkOrderByDateWithIntermediates(List<LapTimeDTO> laps) {
    // All start dates are in order
    long previousLastSeen = -1;
    boolean lastSeenAvailable = false;
    for (LapTimeDTO l : laps) {
      if (l.getLastSeenDate() != null) {
        Assert.assertTrue("Last seen date in incorrect order", l.getLastSeenDate().getTime() >= previousLastSeen);
        previousLastSeen = l.getLastSeenDate().getTime();
        lastSeenAvailable = true;
      } else {
        Assert.assertFalse("There has been a laptime with time before and " +
            "this one has no time. Laps was no time are considered the oldest.", lastSeenAvailable);
      }
    }
  }

  @Test
  public void ordersByDuration() {
    LapTimeOrder testMe = new LapTimeOrder();
    ArrayList<LapTime> searchResults = new ArrayList<LapTime>();
    long time = System.currentTimeMillis();
    LapTime l1 = creator.createLapTimeWithNoIntermediate(1, true, time);
    time = time + 111;

    LapTime l2 = creator.createLapTimeWithNoIntermediate(2, true, time);
    time = time + 162;

    LapTime l3 = creator.createLapTimeWithNoIntermediate(3, true, time);
    time = time + 133;

    LapTime l4 = creator.createLapTimeWithNoIntermediate(4, true, time);
    time = time + 124;

    LapTime l5 = creator.createLapTimeWithNoIntermediate(5, true, time);
    time = time + 155;

    LapTime l6 = creator.createLapTimeWithNoIntermediate(6, true, time);
    // l6 does not end

    searchResults.add(l4);
    searchResults.add(l1);
    searchResults.add(l6);
    searchResults.add(l5);
    searchResults.add(l2);
    searchResults.add(l3);
    List<LapTimeDTO> result = (new LapTimeConverter()).convert(searchResults);
    testMe.orderByDuration(result);

    org.junit.Assert.assertEquals(6, result.size());
    org.junit.Assert.assertEquals(l1.getId(), result.get(0).getId());
    org.junit.Assert.assertEquals(l4.getId(), result.get(1).getId());
    org.junit.Assert.assertEquals(l3.getId(), result.get(2).getId());
    org.junit.Assert.assertEquals(l5.getId(), result.get(3).getId());
    org.junit.Assert.assertEquals(l2.getId(), result.get(4).getId());
    org.junit.Assert.assertEquals(l6.getId(), result.get(5).getId());
  }

  @Test
  public void ordersByNbLapsAndThenLapEndThenLapStart() {
    LapTimeOrder testMe = new LapTimeOrder();
    long startForAll = System.currentTimeMillis();

    // Laps of pilot 1 - has more laps
    long start = startForAll;
    long end = start + 111;
    dtoCreator.nextPilot();
    LapTimeDTO l1 = dtoCreator.createDTOLapTime(start, end, 3);
    start = end;
    end = start + 111;
    LapTimeDTO l2 = dtoCreator.createDTOLapTime(start, end, 3);
    start = end;
    end = start + 111;
    LapTimeDTO l3 = dtoCreator.createDTOLapTime(start, end, 3);

    // Laps of pilot 2 - has first lap end
    start = startForAll;
    end = start + 112;
    dtoCreator.nextPilot();
    LapTimeDTO l4 = dtoCreator.createDTOLapTime(start, end, 2);
    start = end;
    end = start + 112;
    LapTimeDTO l5 = dtoCreator.createDTOLapTime(start, end, 2);

    // Laps of pilot 3 -
    start = startForAll;
    end = start + 113;
    dtoCreator.nextPilot();
    LapTimeDTO l6 = dtoCreator.createDTOLapTime(start, end, 2);
    start = end;
    end = start + 113;
    LapTimeDTO l7 = dtoCreator.createDTOLapTime(start, end, 2);

    // Laps of pilot 4 - has first lap start
    start = startForAll;
    end = start + 114;
    dtoCreator.nextPilot();
    LapTimeDTO l8 = dtoCreator.createDTOLapTime(start, -1, 2);
    start = end;
    end = start + 114;
    LapTimeDTO l9 = dtoCreator.createDTOLapTime(start, -1, 2);

    // Laps of pilot 5 -
    start = startForAll;
    dtoCreator.nextPilot();
    end = start + 115;
    LapTimeDTO l10 = dtoCreator.createDTOLapTime(start, -1, 2);
    start = end;
    end = start + 115;
    LapTimeDTO l11 = dtoCreator.createDTOLapTime(start, -1, 2);

    List<LapTimeDTO> result = new ArrayList<>();

    result.add(l1);
    result.add(l2);
    result.add(l3);
    result.add(l4);
    result.add(l5);
    result.add(l6);
    result.add(l7);
    result.add(l8);
    result.add(l9);
    result.add(l10);
    result.add(l11);

    (new LapTimeFiller()).fillLapsNumber(result);
    (new LapTimeFilter()).keepOnlyLast(result);
    testMe.orderForRace(result);
    org.junit.Assert.assertEquals(5, result.size());
    org.junit.Assert.assertEquals(3, result.get(0).getId());
    org.junit.Assert.assertEquals(5, result.get(1).getId());
    org.junit.Assert.assertEquals(7, result.get(2).getId());
    org.junit.Assert.assertEquals(9, result.get(3).getId());
    org.junit.Assert.assertEquals(11, result.get(4).getId());
  }

  @Test
  public void ordersRaceLapsByNbLapsAndThenLapEndThenLapStart() {
    LapTimeOrder testMe = new LapTimeOrder();
    List<LapTimeDTO> raceLapsWithIntermediates = dtoCreator.createRaceLapsWithIntermediates();
    testMe.orderForRace(raceLapsWithIntermediates);
    checkRaceLapsOrder(raceLapsWithIntermediates);
  }

  public void checkRaceLapsOrder(List<LapTimeDTO> raceLapsWithIntermediates) {
    int lastLapIndex = Integer.MAX_VALUE;
    for (LapTimeDTO l : raceLapsWithIntermediates) {
      Assert.assertTrue("Lap " + l + " has a lap index inferior to previous lap : " + lastLapIndex, l.getLapIndex() <= lastLapIndex);
      lastLapIndex = l.getLapIndex();
    }
  }

  @Test
  public void ordersByDurationOrStartDate() {
    List<LapTimeDTO> laps = dtoCreator.createLaps();
    checkOrderByDurationOrStartDate(laps);
  }

  public void checkOrderByDurationOrStartDate(List<LapTimeDTO> laps) {
    LapTimeOrder testMe = new LapTimeOrder();
    testMe.orderByDuration(laps);
    long previousDuration = -1;
    long previousStart = -1;
    for (LapTimeDTO l : laps) {

      if (l.getDuration() > 0) {
        Assert.assertTrue(l.getDuration() >= previousDuration);
        previousDuration = l.getDuration();
      } else {
        // None order by duration anymore
        previousDuration = Long.MAX_VALUE;
        if (l.getStartDate() != null) {
          // Is it the first that did not end
          if (previousStart >= 0) {
            Assert.assertTrue(l.getStartDate().getTime() >= previousStart);
          }
          previousStart = l.getStartDate().getTime();
        } else {
          // None order by start date anymore
          previousStart = Long.MAX_VALUE;
        }
      }
    }
  }

  @Test
  public void ordersLapsWithIntermediatesByDurationOrStartDate() {
    List<LapTimeDTO> laps = dtoCreator.createLapsWithIntermediates();
    checkOrderByDurationOrStartDate(laps);
  }

  @Test
  public void fillGapsInfoWhenOrderedByDuration() {
    LapTimeOrder order = new LapTimeOrder();
    List<LapTimeDTO> laps = dtoCreator.createLaps();
    order.orderByDuration(laps);
    long previousGap = -1;

    for (LapTimeDTO l : laps) {
      if (previousGap == -1) {
        Assert.assertEquals(0, l.getGapWithBest());
      }
      if (l.getDuration() > 0) {
        Assert.assertTrue(l.getGapWithBest() >= previousGap);
      } else {
        // No more gaps
        previousGap = Long.MAX_VALUE;
      }
      previousGap = l.getGapWithBest();
    }
  }
}