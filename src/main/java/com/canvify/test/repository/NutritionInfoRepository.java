package com.canvify.test.repository;

import com.canvify.test.entity.NutritionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NutritionInfoRepository extends JpaRepository<NutritionInfo, Long> {
}
