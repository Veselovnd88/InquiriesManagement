package ru.veselov.CompanyAuthorizationServer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.veselov.CompanyAuthorizationServer.entity.UserEntity;
import ru.veselov.CompanyAuthorizationServer.repository.UserRepository;
import ru.veselov.CompanyAuthorizationServer.security.UserDetail;

import java.util.Optional;
@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    /*Класс в котором передается Credentials, этот объект проходит аутентификацию*/
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> optionalUser = userRepository.findByUsername(username);
        if(optionalUser.isEmpty()){
            throw new UsernameNotFoundException("No such user in Database");
        }
        UserEntity userDetail = optionalUser.get();
        return new UserDetail(userDetail.getUsername(), userDetail.getPassword(), userDetail.getRole());
    }
}
