package org.trd.app.teknichrono.model.dto;

import javax.persistence.EntityManager;
import javax.xml.bind.annotation.XmlRootElement;

import org.trd.app.teknichrono.model.jpa.Chronometer;

@XmlRootElement
public class NestedChronometerDTO {

  private int id;
  private int version;

  private String name;

  public NestedChronometerDTO() {
  }

  public NestedChronometerDTO(final Chronometer entity) {
    if (entity != null) {
      this.id = entity.getId();
      this.version = entity.getVersion();
      this.name = entity.getName();
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[id=" + getId());
    sb.append(",name=" + name + "]");
    return sb.toString();
  }

  //
  public Chronometer fromDTO(Chronometer entity, EntityManager em) {
    if (entity == null) {
      entity = new Chronometer();
    }
    entity.setVersion(this.version);
    entity.setName(this.name);
    entity = em.merge(entity);
    return entity;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
