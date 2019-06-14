package org.trd.app.teknichrono.model.jpa;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.trd.app.teknichrono.util.exception.NotFoundException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

@Dependent
public class EventRepository extends PanacheRepositoryWrapper<Event> {

  @ApplicationScoped
  public static class Panache implements PanacheRepository<Event> {
  }

  private final Panache panacheRepository;

  private final SessionRepository.Panache sessionRepository;

  @Inject
  public EventRepository(Panache panacheRepository, SessionRepository.Panache sessionRepository) {
    super(panacheRepository);
    this.panacheRepository = panacheRepository;
    this.sessionRepository = sessionRepository;
  }

  public Event findByName(String name) {
    return panacheRepository.find("name", name).firstResult();
  }


  public void deleteById(long id) throws NotFoundException {
    Event entity = panacheRepository.findById(id);
    if (entity == null) {
      throw new NotFoundException();
    }

    List<Session> sessions = entity.getSessions();
    if (sessions != null) {
      for (Session session : sessions) {
        session.setEvent(null);
        sessionRepository.persist(session);
      }
    }
    panacheRepository.delete(entity);
  }
}
