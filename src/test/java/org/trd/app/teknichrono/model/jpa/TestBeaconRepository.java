package org.trd.app.teknichrono.model.jpa;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trd.app.teknichrono.model.dto.BeaconDTO;
import org.trd.app.teknichrono.model.jpa.Beacon;
import org.trd.app.teknichrono.model.jpa.BeaconRepository;
import org.trd.app.teknichrono.model.jpa.Pilot;
import org.trd.app.teknichrono.model.jpa.PilotRepository;
import org.trd.app.teknichrono.model.jpa.Ping;
import org.trd.app.teknichrono.model.jpa.PingRepository;
import org.trd.app.teknichrono.util.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestBeaconRepository {

  private long id = 1L;

  @Mock
  private PilotRepository pilotRepository;

  @Mock
  private PingRepository pingRepository;

  @Spy
  @InjectMocks
  private BeaconRepository beaconRepository;

  public Beacon newBeacon(long number, long pilotId) {
    Beacon beacon = new Beacon();
    beacon.id = id++;
    beacon.setNumber(number);
    if (pilotId >= 0) {
      Pilot p = new Pilot();
      p.id = pilotId;
      beacon.setPilot(p);
      p.setFirstName("First");
      p.setLastName("Last");
    }
    for (int i = 0; i < 10; i++) {
      Ping p = new Ping();
      p.id = id++;
      p.setBeacon(beacon);
      beacon.getPings().add(p);
    }
    return beacon;
  }

  @Test
  public void createsBeaconWithCompletePilotIfIdProvided() throws NotFoundException {
    Beacon entity = newBeacon(999, 9);
    Pilot pilot = entity.getPilot();

    when(pilotRepository.findById(9L)).thenReturn(pilot);
    doNothing().when(beaconRepository).persist(entity);

    beaconRepository.create(entity);

    verify(pilotRepository).findById(9L);
    verify(beaconRepository).persist(entity);
  }

  @Test
  public void deleteByIdRemovesBeaconFromPilot() throws NotFoundException {
    Beacon entity = newBeacon(999, 9);
    Pilot pilot = entity.getPilot();

    doReturn(entity).when(beaconRepository).findById(anyLong());
    doNothing().when(beaconRepository).delete(entity);

    beaconRepository.deleteById(entity.id);

    verify(beaconRepository).delete(entity);
    verify(pilotRepository).persist(pilot);
    assertThat(pilot.getCurrentBeacon()).isNull();
  }

  @Test
  public void deleteByIdRemovesBeaconFromPings() throws NotFoundException {
    Beacon entity = newBeacon(999, 9);
    List<Ping> pings = entity.getPings();

    doReturn(entity).when(beaconRepository).findById(anyLong());
    doNothing().when(beaconRepository).delete(entity);

    beaconRepository.deleteById(entity.id);

    verify(beaconRepository).delete(entity);

    for (Ping ping : pings) {
      verify(pingRepository).persist(ping);
      assertThat(ping.getBeacon()).isNull();
    }
  }

  @Test
  public void deleteByIdReturnsErrorIfBeaconDoesNotExist() {
    doReturn(null).when(beaconRepository).findById(42L);
    assertThrows(NotFoundException.class, () -> beaconRepository.deleteById(42L));
    verify(beaconRepository, never()).delete(any());
  }


  @Test
  public void listAll() {
    List<Beacon> entities = new ArrayList<>();
    Beacon entity1 = newBeacon(999, 9);
    Beacon entity2 = newBeacon(-1, 10);
    Beacon entity3 = newBeacon(46, -1);
    entities.add(entity1);
    entities.add(entity2);
    entities.add(entity3);

    PanacheQuery<Beacon> query = mock(PanacheQuery.class);
    doReturn(query).when(beaconRepository).findAll();
    when(query.page(any())).thenReturn(query);
    when(query.stream()).thenReturn(entities.stream());

    List<BeaconDTO> beacons = beaconRepository.findAll(null, null);
    assertThat(beacons).isNotNull();
    assertThat(beacons).hasSize(3);

    assertThat(beacons.stream().filter(b -> (b.getNumber() == 999 && b.getPilot() != null && b.getPilot().getId() == 9)).count()).isEqualTo(1);
    assertThat(beacons.stream().filter(b -> (b.getPilot() != null && b.getPilot().getId() == 10)).count()).isEqualTo(1);
    assertThat(beacons.stream().filter(b -> (b.getNumber() == 46 && b.getPilot() == null)).count()).isEqualTo(1);

  }

  @Test
  public void listAllCanUseWindows() {
    List<Beacon> entities = new ArrayList<>();
    Beacon entity1 = newBeacon(999, 9);
    entities.add(entity1);

    PanacheQuery<Beacon> query = mock(PanacheQuery.class);
    doReturn(query).when(beaconRepository).findAll();
    when(query.page(any())).thenReturn(query);
    when(query.stream()).thenReturn(entities.stream());

    List<BeaconDTO> beacons = beaconRepository.findAll(1, 1);
    assertThat(beacons).isNotNull();
    assertThat(beacons).hasSize(1);
  }
}