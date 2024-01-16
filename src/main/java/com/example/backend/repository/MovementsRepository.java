package com.example.backend.repository;

import com.example.backend.model.Movements;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovementsRepository extends JpaRepository<Movements, Integer> {

}
