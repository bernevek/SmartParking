package com.smartparking.service.impl;

import com.smartparking.entity.Client;
import com.smartparking.entity.Role;
import com.smartparking.model.request.PasswordRequest;
import com.smartparking.model.request.RegistrationRequest;
import com.smartparking.model.request.SocialSignInRequest;
import com.smartparking.repository.ClientRepository;
import com.smartparking.security.exception.*;
import com.smartparking.security.utils.validation.Validator;
import com.smartparking.service.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Qualifier("MyUserDetails")
public class SecurityServiceImpl implements UserDetailsService, SecurityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityServiceImpl.class);

    @Autowired
    private Validator validator;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final Optional<UserDetails> user = Optional.of(clientRepository.findClientByEmail(username));
        final AccountStatusUserDetailsChecker detailsChecker = new AccountStatusUserDetailsChecker();
        user.ifPresent(detailsChecker::check);
        return user.orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    @Override
    public void activateUserByEmail(String email) {
        Client client = clientRepository.findClientByEmail(email);
        client.setActivated(true);
        clientRepository.save(client);
    }

    public void saveClientFromRegistrationRequest(RegistrationRequest registrationRequest) throws EmailValidationEx, NonMatchingPasswordsEx, PasswordValidationEx, FirstnameValidationEx, LastnameValidationEx, DuplicateEmailEx {
        Client client = new Client();
        if ((clientRepository.findClientByEmail(registrationRequest.getEmail()) != null
                && !clientRepository.findClientByEmail(registrationRequest.getEmail()).getActivated())) {
            client = clientRepository.findClientByEmail(registrationRequest.getEmail());
        }
        client.setEmail(validator.validateEmailOnRegistration(registrationRequest.getEmail()));
        client.setPassword(bcryptEncoder.encode(validator.checkPasswords(registrationRequest.getPassword(), registrationRequest.getConfirmPassword())));
        client.setFirstName(validator.validateFirstname(registrationRequest.getFirstname()));
        client.setLastName(validator.validateLastname(registrationRequest.getLastname()));
        client.setRole(Role.DRIVER);
        client.setActivated(false);
        clientRepository.save(client);
    }

    public void updateClientPassword(PasswordRequest passwordRequest) throws NonMatchingPasswordsEx, PasswordValidationEx {
        Client client = clientRepository.findClientByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        client.setPassword(bcryptEncoder.encode(validator.checkPasswords(passwordRequest.getPassword(), passwordRequest.getConfirmPassword())));
        clientRepository.save(client);
    }

    public void updateClientEncodedPassword(String newEncodedPassword, String userEmail) {
        Client client = clientRepository.findClientByEmail(userEmail);
        client.setPassword(newEncodedPassword);
        clientRepository.save(client);
    }

    public void saveClientFromSocialSignInRequest(SocialSignInRequest socialSignInRequest) throws EmailValidationEx, DuplicateEmailEx, FirstnameValidationEx, LastnameValidationEx {
        String[] nameSurname = socialSignInRequest.getName().trim().split("\\s+");
        Client client = new Client();
        client.setEmail(validator.validateEmailOnRegistration(socialSignInRequest.getEmail()));
        client.setPassword(bcryptEncoder.encode("1234567"));
        client.setFirstName(validator.validateFirstname(nameSurname[0]));
        client.setLastName(validator.validateLastname(nameSurname[1]));
        client.setRole(Role.DRIVER);
        client.setActivated(true);
        clientRepository.save(client);
    }
}
