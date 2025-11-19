package code.withHarry.repogistory;

import org.springframework.data.jpa.repository.JpaRepository;
import code.withHarry.model.User;

public interface UserRepo extends JpaRepository<User, Long> {
  
	User findByEmail(String email);
	User findByEmailAndProvider(String email,String provider);
	
}