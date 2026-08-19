package api.poja.app.endpoint.web.model;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeacherNoteView {
  private String coursRef;
  private String coursIntitule;
  private Integer semestre;
  private String studentStd;
  private String studentNom;
  private String studentPrenom;
  private Instant examenDate;
  private BigDecimal coefficient;
  private BigDecimal valeur;
  private Integer version;
}
