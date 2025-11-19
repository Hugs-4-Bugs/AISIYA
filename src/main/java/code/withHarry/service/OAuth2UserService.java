package code.withHarry.service;

import code.withHarry.model.User;
import code.withHarry.repogistory.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepo userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        
        OAuth2User oAuth2User = super.loadUser(userRequest);

     
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        if (name == null) { 
            name = oAuth2User.getAttribute("login"); 
        }

     
        User user = userRepository.findByEmail(email);

        if (user == null) {

            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setProvider(provider);
            user.setRole("ROLE_USER");
   
            userRepository.save(user);
        } else if (user.getProvider() == null) {
     
            user.setProvider(provider);
            userRepository.save(user);
        }

        return oAuth2User;
    }
}