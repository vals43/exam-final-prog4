package api.poja.app.endpoint.web.model;

import api.poja.app.model.User;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GradeRow {
  private User student;
  private String inscriptionId;
  private List<GradeCell> cells;
}
