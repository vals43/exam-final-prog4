package api.poja.app.endpoint.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeacherAffectationView {
  private String coursId;
  private String coursRef;
  private String coursIntitule;
  private String groupeId;
  private String groupeRef;
  private Integer annee;
}
