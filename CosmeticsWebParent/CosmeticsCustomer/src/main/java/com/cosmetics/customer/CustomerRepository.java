package com.cosmetics.customer;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.cosmetics.common.entity.AuthenticationType;
import com.cosmetics.common.entity.Customer;

public interface CustomerRepository extends CrudRepository<Customer, Integer> {

    @Query("SELECT c FROM Customer c WHERE c.email = ?1")
    public Customer findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);

    @Query("SELECT c FROM Customer c WHERE c.verificationCode = ?1")
    public Customer findByVerificationCode(String code);

    @Query("UPDATE Customer c SET c.enabled = true, c.verificationCode = null WHERE c.id = ?1")
    @Modifying
    public void enable(Integer id);

    @Query("UPDATE Customer c SET c.authenticationType = ?2 WHERE c.id = ?1")
    @Modifying
    public void updateAuthenticationType(Integer customerId, AuthenticationType type);

    public Customer findByResetPasswordToken(String token);
}
