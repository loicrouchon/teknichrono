package org.trd.app.teknichrono.rest;

import org.jboss.logging.Logger;
import org.trd.app.teknichrono.model.dto.ChronometerDTO;
import org.trd.app.teknichrono.model.jpa.Chronometer;
import org.trd.app.teknichrono.model.repository.ChronometerRepository;
import org.trd.app.teknichrono.model.repository.PingRepository;
import org.trd.app.teknichrono.util.DurationLogger;
import org.trd.app.teknichrono.util.exception.NotFoundException;

import javax.inject.Inject;
import javax.persistence.OptimisticLockException;
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
import java.util.List;
import java.util.stream.Collectors;

@Path("/chronometers")
public class ChronometerEndpoint {

  private static final Logger LOGGER = Logger.getLogger(ChronometerEndpoint.class);

  private final ChronometerRepository chronometerRepository;

  private final PingRepository pingRepository;

  @Inject
  public ChronometerEndpoint(ChronometerRepository chronometerRepository, PingRepository pingRepository) {
    this.chronometerRepository = chronometerRepository;
    this.pingRepository = pingRepository;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  public Response create(Chronometer entity) {
    try (DurationLogger dl = new DurationLogger(LOGGER, "Create chronometer " + entity.getName())) {
      chronometerRepository.persist(entity);
      return Response
          .created(UriBuilder.fromResource(ChronometerEndpoint.class).path(String.valueOf(entity.id)).build())
          .build();
    }
  }

  @DELETE
  @Path("/{id:[0-9][0-9]*}")
  @Transactional
  public Response deleteById(@PathParam("id") long id) {
    try (DurationLogger perf = DurationLogger.get(LOGGER).start("Delete chronometer id=" + id)) {
      try {
        chronometerRepository.deleteById(id);
      } catch (NotFoundException e) {
        return Response.status(Status.NOT_FOUND).build();
      }
      return Response.noContent().build();
    }

  }

  @GET
  @Path("/{id:[0-9][0-9]*}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response findById(@PathParam("id") long id) {
    try (DurationLogger dl = new DurationLogger(LOGGER, "Find chronometer ID=" + id)) {
      Chronometer entity = chronometerRepository.findById(id);
      if (entity == null) {
        return Response.status(Status.NOT_FOUND).build();
      }
      ChronometerDTO dto = ChronometerDTO.fromChronometer(entity);
      return Response.ok(dto).build();
    }
  }

  @GET
  @Path("/name")
  @Produces(MediaType.APPLICATION_JSON)
  public Response findChronometerByName(@QueryParam("name") String name) {
    try (DurationLogger dl = new DurationLogger(LOGGER, "Find chronometer " + name)) {
      Chronometer entity = chronometerRepository.findByName(name);
      if (entity == null) {
        return Response.status(Status.NOT_FOUND).build();
      }
      ChronometerDTO dto = ChronometerDTO.fromChronometer(entity);
      return Response.ok(dto).build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<ChronometerDTO> listAll(@QueryParam("page") Integer pageIndex, @QueryParam("pageSize") Integer pageSize) {
    try (DurationLogger dl = new DurationLogger(LOGGER, "Get all chronometers")) {
      return chronometerRepository.findAll(pageIndex, pageSize)
          .map(ChronometerDTO::fromChronometer)
          .collect(Collectors.toList());
    }
  }

  @PUT
  @Path("/{id:[0-9][0-9]*}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  public Response update(@PathParam("id") long id, Chronometer dto) {
    try (DurationLogger dl = new DurationLogger(LOGGER, "Update chronometer ID=" + id)) {
      if (dto == null) {
        return Response.status(Status.BAD_REQUEST).build();
      }
      if (id != dto.id) {
        return Response.status(Status.CONFLICT).entity(dto).build();
      }
      if (chronometerRepository.findById(id) == null) {
        return Response.status(Status.NOT_FOUND).build();
      }
      try {
        chronometerRepository.persist(dto);
      } catch (OptimisticLockException e) {
        return Response.status(Response.Status.CONFLICT).entity(e.getEntity()).build();
      }

      return Response.noContent().build();
    }
  }
}
