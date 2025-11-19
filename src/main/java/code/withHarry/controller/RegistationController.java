

package code.withHarry.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // ⭐ IMPORT
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // ⭐ IMPORT

import code.withHarry.model.User;
import code.withHarry.repogistory.UserRepo;

@Controller 
public class RegistationController {

	@Autowired 
	private UserRepo userRepository;
	@Autowired 
	private PasswordEncoder passwordEncoder;

	@GetMapping("/register")
    public String showRegistrationForm(Model model) {
       
        model.addAttribute("user", new User()); 
        return "registration"; 
    }

	@PostMapping("/register")
	public String registerUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        
       
        if (userRepository.findByEmail(user.getEmail()) != null) {
            redirectAttributes.addFlashAttribute("registrationError", "Email already exists. Please log in or use a different email.");
            return "redirect:/register";
        }
        
	  
	    user.setPassword(passwordEncoder.encode(user.getPassword()));
	    user.setRole("ROLE_USER");
        user.setProvider(null); 
	    userRepository.save(user);


        redirectAttributes.addFlashAttribute("registrationSuccess", "Registration successful! Please log in.");
	    return "redirect:/login"; 
	}
}