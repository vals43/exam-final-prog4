package api.poja.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.Instant;
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
public class NoteHistory {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "note_id", nullable = false)
  private Note note;

  @Column(nullable = false, precision = 5, scale = 2)
  private BigDecimal ancienneValeur;

  @Column(nullable = false, precision = 5, scale = 2)
  private BigDecimal nouvelleValeur;

  @Column(nullable = false)
  private Instant dateModification;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "modifie_par_id", nullable = false)
  private User modifiePar;
}
