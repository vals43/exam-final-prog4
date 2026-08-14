package api.poja.app.conf;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class BaseIT extends FacadeIT {

  @Autowired JdbcTemplate jdbcTemplate;

  @BeforeEach
  void cleanDatabase() {
    jdbcTemplate.execute("DELETE FROM note_history");
    jdbcTemplate.execute("DELETE FROM note");
    jdbcTemplate.execute("DELETE FROM affectation");
    jdbcTemplate.execute("DELETE FROM inscription");
    jdbcTemplate.execute("DELETE FROM examen");
    jdbcTemplate.execute("DELETE FROM cours_parcours");
    jdbcTemplate.execute("DELETE FROM cours");
    jdbcTemplate.execute("DELETE FROM parcours");
    jdbcTemplate.execute("DELETE FROM groupe");
    jdbcTemplate.execute("DELETE FROM app_user");
  }
}
