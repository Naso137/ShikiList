package com.naso.restapi.repository;

import com.naso.restapi.model.ActivationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface ActivationCodeRepository  extends JpaRepository<ActivationCode, Long> {
    ActivationCode findByUserId(long userId);
    ActivationCode findByCode(String code);
    List<ActivationCode> findAllByExpiryDateBefore(Date date);
}
