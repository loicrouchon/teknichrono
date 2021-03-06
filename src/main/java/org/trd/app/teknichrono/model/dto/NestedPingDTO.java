package org.trd.app.teknichrono.model.dto;

import org.jboss.logging.Logger;
import org.trd.app.teknichrono.model.jpa.Ping;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.sql.Timestamp;

@XmlRootElement
public class NestedPingDTO implements Serializable{

  private Logger logger = Logger.getLogger(LapTimeDTO.class);

  private int id;
  private int version;
  private NestedBeaconDTO beacon;
  private Timestamp dateTime;
  private int power;


  public NestedPingDTO() {
  }

  public NestedPingDTO(final Ping entity) {
    if (entity != null) {
      this.id = entity.getId();
      this.version = entity.getVersion();
      if (entity.getBeacon() != null) {
        this.beacon = new NestedBeaconDTO(entity.getBeacon());
      }
      this.dateTime = entity.getDateTime();
      this.power = entity.getPower();
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[id=" + getId());
    sb.append(",beacon=" + beacon.getNumber());
    sb.append(",dateTime=" + dateTime);
    sb.append(",power=" + power + "]");
    return sb.toString();
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public NestedBeaconDTO getBeacon() {
    return beacon;
  }

  public void setBeacon(NestedBeaconDTO beacon) {
    this.beacon = beacon;
  }

  public Timestamp getDateTime() {
    return dateTime;
  }

  public void setDateTime(Timestamp dateTime) {
    this.dateTime = dateTime;
  }

  public int getPower() {
    return power;
  }

  public void setPower(int power) {
    this.power = power;
  }

}