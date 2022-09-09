package com.arham.number.repository;
import com.arham.number.model.NumberModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NumberRepository extends JpaRepository<NumberModel, Integer> {

}
