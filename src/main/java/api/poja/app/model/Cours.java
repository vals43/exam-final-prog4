package api.poja.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ToString
public class Cours {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
  private String id;

  @Column(nullable = false)
  private String ref;

  @Column(nullable = false)
  private String intitule;

  @Column(nullable = false)
  private Integer credits;

  @Column(nullable = false)
  private Integer semestre;

  @ManyToMany
  @JoinTable(
      name = "cours_parcours",
      joinColumns = @JoinColumn(name = "cours_id"),
      inverseJoinColumns = @JoinColumn(name = "parcours_id"))
  private List<Parcours> parcours;
}
