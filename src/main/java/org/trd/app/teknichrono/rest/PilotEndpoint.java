package org.trd.app.teknichrono.rest;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.trd.app.teknichrono.model.jpa.Beacon;
import org.trd.app.teknichrono.model.jpa.Category;
import org.trd.app.teknichrono.model.jpa.Pilot;
import org.trd.app.teknichrono.model.jpa.Session;
import org.trd.app.teknichrono.model.dto.PilotDTO;

/**
 * 
 */
@Path("/pilots")
public class PilotEndpoint {

  EntityManager em;

  @Inject
  public PilotEndpoint(EntityManager em) {
    this.em = em;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  public Response create(Pilot entity) {
    if (entity.getCurrentBeacon() != null && entity.getCurrentBeacon().id > 0) {
      Beacon beacon = em.find(Beacon.class, entity.getCurrentBeacon().id);
      entity.setCurrentBeacon(beacon);
    }
    em.persist(entity);
    return Response.created(UriBuilder.fromResource(PilotEndpoint.class).path(String.valueOf(entity.id)).build())
        .build();
  }

  @DELETE
  @Path("/{id:[0-9][0-9]*}")
  @Transactional
  public Response deleteById(@PathParam("id") long id) {
    Pilot entity = em.find(Pilot.class, id);
    if (entity == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    for (Session s : entity.getSessions()) {
      s.getPilots().remove(entity);
    }
    em.remove(entity);
    return Response.noContent().build();
  }

  @GET
  @Path("/{id:[0-9][0-9]*}")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public Response findById(@PathParam("id") long id) {
    TypedQuery<Pilot> findByIdQuery = em.createQuery(
        "SELECT DISTINCT p FROM Pilot p LEFT JOIN FETCH p.currentBeacon WHERE p.id = :entityId ORDER BY p.id",
        Pilot.class);
    findByIdQuery.setParameter("entityId", id);
    Pilot entity;
    try {
      entity = findByIdQuery.getSingleResult();
    } catch (NoResultException nre) {
      entity = null;
    }
    if (entity == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    PilotDTO dto = new PilotDTO(entity);
    return Response.ok(dto).build();
  }

  @GET
  @Path("/name")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public Response findByName(@QueryParam("firstname") String firstname, @QueryParam("lastname") String lastname) {
    TypedQuery<Pilot> findByIdQuery = em
        .createQuery("SELECT DISTINCT p FROM Pilot p LEFT JOIN FETCH p.currentBeacon WHERE"
            + " p.firstName = :firstname AND p.lastName = :lastname ORDER BY p.id", Pilot.class);
    findByIdQuery.setParameter("firstname", firstname);
    findByIdQuery.setParameter("lastname", lastname);
    Pilot entity;
    try {
      entity = findByIdQuery.getSingleResult();
    } catch (NoResultException nre) {
      entity = null;
    }
    if (entity == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    PilotDTO dto = new PilotDTO(entity);
    return Response.ok(dto).build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public List<PilotDTO> listAll(@QueryParam("start") Integer startPosition, @QueryParam("max") Integer maxResult) {
    TypedQuery<Pilot> findAllQuery = em
        .createQuery("SELECT DISTINCT p FROM Pilot p LEFT JOIN FETCH p.currentBeacon ORDER BY p.id", Pilot.class);
    if (startPosition != null) {
      findAllQuery.setFirstResult(startPosition);
    }
    if (maxResult != null) {
      findAllQuery.setMaxResults(maxResult);
    }
    final List<Pilot> results = findAllQuery.getResultList();
    final List<PilotDTO> converted = PilotDTO.convert(results);
    return converted;
  }

  @POST
  @Path("{pilotId:[0-9][0-9]*}/setBeacon")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public Response associateBeacon(@PathParam("pilotId") long pilotId, @QueryParam("beaconId") long beaconId) {
    Pilot pilot = em.find(Pilot.class, pilotId);
    if (pilot == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    Beacon beacon = em.find(Beacon.class, beaconId);
    if (beacon == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    pilot.setCurrentBeacon(beacon);
    em.persist(pilot);
    em.persist(beacon);
    PilotDTO dto = new PilotDTO(pilot);
    return Response.ok(dto).build();
  }

  @PUT
  @Path("/{id:[0-9][0-9]*}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  public Response update(@PathParam("id") long id, Pilot entity) {
    if (entity == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    if (id != entity.id) {
      return Response.status(Status.CONFLICT).entity(entity).build();
    }
    Pilot pilot = em.find(Pilot.class, id);
    if (pilot == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    // Update of category
    if (entity.getCategory() != null && entity.getCategory().id > 0) {
      Category category = em.find(Category.class, entity.getCategory().id);
      pilot.setCategory(category);
    }
    // Update of beacon
    if (entity.getCurrentBeacon() != null && entity.getCurrentBeacon().id > 0) {
      Beacon beacon = em.find(Beacon.class, entity.getCurrentBeacon().id);
      pilot.setCurrentBeacon(beacon);
    }
    pilot.setFirstName(entity.getFirstName());
    pilot.setLastName(entity.getLastName());
    try {
      em.persist(pilot);
    } catch (OptimisticLockException e) {
      return Response.status(Response.Status.CONFLICT).entity(e.getEntity()).build();
    }

    return Response.noContent().build();
  }
}
