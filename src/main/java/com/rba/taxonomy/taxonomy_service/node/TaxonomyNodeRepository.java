package com.rba.taxonomy.taxonomy_service.node;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxonomyNodeRepository extends JpaRepository<TaxonomyNode, Long> {

	Optional<TaxonomyNode> findFirstByName(String name);

}
