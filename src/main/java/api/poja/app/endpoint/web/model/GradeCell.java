package api.poja.app.endpoint.web.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GradeCell {
  private String examenId;
  private Instant examenDate;
  private BigDecimal coefficient;
  private String noteId;
  private BigDecimal valeur;
  private Integer version;
  private List<api.poja.app.model.NoteHistory> history;
}
