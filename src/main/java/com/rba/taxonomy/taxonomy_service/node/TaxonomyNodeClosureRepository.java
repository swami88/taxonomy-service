package com.rba.taxonomy.taxonomy_service.node;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaxonomyNodeClosureRepository extends JpaRepository<TaxonomyNodeClosure, TaxonomyNodeClosureId> {

	@Query("""
			select closure from TaxonomyNodeClosure closure
			join fetch closure.ancestor
			where closure.descendant.id = :nodeId
			order by closure.depth desc
			""")
	List<TaxonomyNodeClosure> findAncestorPaths(@Param("nodeId") Long nodeId);

	@Query("""
			select closure from TaxonomyNodeClosure closure
			join fetch closure.descendant
			where closure.ancestor.id = :nodeId
			order by closure.depth asc
			""")
	List<TaxonomyNodeClosure> findDescendantPaths(@Param("nodeId") Long nodeId);

	@Query("""
			select closure.ancestor.id from TaxonomyNodeClosure closure
			where closure.descendant.id = :nodeId and closure.depth = 1
			""")
	Optional<Long> findParentId(@Param("nodeId") Long nodeId);

}
