package api.poja.app.repository;

import api.poja.app.model.Role;
import api.poja.app.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
  Optional<User> findByStd(String std);

  Optional<User> findByEmail(String email);

  List<User> findByRole(Role role);
}
