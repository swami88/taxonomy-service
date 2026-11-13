package com.rba.taxonomy.taxonomy_service.node;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SampleTaxonomyInitializerIntegrationTests {

	@Autowired
	private TaxonomyNodeRepository nodeRepository;

	@Autowired
	private TaxonomyNodeService nodeService;

	@Test
	void persistsAndRetrievesTheSampleHierarchy() {
		TaxonomyNode life = nodeRepository.findFirstByName("Life").orElseThrow();

		assertThat(nodeService.findDescendants(life.getId()))
				.extracting(TaxonomyPathNodeResponse::name)
				.containsExactlyInAnyOrder("Animals", "Plants", "Fungi", "Mammals", "Birds");
	}

}
