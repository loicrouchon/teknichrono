package org.trd.app.teknichrono.model.dto;

import lombok.Data;
import org.jboss.logging.Logger;
import org.trd.app.teknichrono.model.jpa.LapTime;
import org.trd.app.teknichrono.model.jpa.Ping;

import javax.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class LapTimeDTO {

  private Logger logger = Logger.getLogger(LapTimeDTO.class);

  private long id;
  private int version;
  private NestedPilotDTO pilot;
  private NestedSessionDTO session;
  private Instant startDate;
  // Either the last intermediate ping or the first ping of the next lap
  private Instant endDate;
  private Instant lastSeenDate;
  private Duration duration;
  private Duration gapWithPrevious;
  private Duration gapWithBest;
  private List<SectorDTO> sectors = new ArrayList<>();
  private long lapIndex;
  private long lapNumber;

  public static LapTimeDTO fromLapTime(LapTime lapTime) {
    return DtoMapper.INSTANCE.asLapTimeDTO(lapTime);
  }

  public LapTime fromDTO(LapTime entity, EntityManager em) {
    if (entity == null) {
      entity = new LapTime();
    }
    entity.setVersion(this.version);
    if (this.pilot != null) {
      entity.setPilot(this.pilot.fromDTO(entity.getPilot(), em));
    }
    if (this.session != null) {
      entity.setSession(this.session.fromDTO(entity.getSession(), em));
    }
    if (!this.sectors.isEmpty()) {
      logger.error("Sorry I cannot rebuild a LapTime from a LapTimeDTO. Leaving list empty.");
    }
    entity = em.merge(entity);
    return entity;
  }

  public void setStartDate(Instant startDate) {
    this.startDate = startDate;
    if (endDate != null) {
      duration = Duration.between(startDate, endDate);
    }
  }

  public void setStartDate(Ping start) {
    setStartDate(start.getInstant());
  }

  public void setEndDate(Instant endDate) {
    this.endDate = endDate;
    if (startDate != null) {
      duration = Duration.between(startDate, endDate);
    }
  }

  public void setEndDate(Ping end) {
    setEndDate(end.getInstant());
  }

  /**
   * If we are on a loop and we identified the lap after this one. Then its
   * start is the end of this one.
   *
   * @param endDate
   */
  public void addLastSector(Instant endDate) {
    // If we have a proper sector, we use it
    if (sectors.size() > 0) {
      SectorDTO previousLast = this.sectors.get(sectors.size() - 1);
      Instant previousLastStart = previousLast.getStart();
      Instant previousLastEnd = previousLastStart.plus(previousLast.getDuration());
      long previousLastChronoId = previousLast.getToChronoId();
      this.sectors.add(new SectorDTO(previousLastEnd, previousLastChronoId, Duration.between(previousLastEnd, endDate)));
    }
    setEndDate(endDate);
  }
}