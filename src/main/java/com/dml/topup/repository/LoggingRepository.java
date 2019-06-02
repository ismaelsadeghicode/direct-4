package com.dml.topup.repository;

import com.dml.topup.domain.Charge;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for the Charge entity.
 *
 * @author Ismael Sadeghi
 */
public interface LoggingRepository extends CrudRepository<Charge, Long> {

    List<Charge> findAll();

    Charge findByMessageId(String messageid);

    Charge findByResNoAndRequestDateTopup(int resNo, Long requestDateTopup);

}
