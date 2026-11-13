package com.rba.taxonomy.taxonomy_service.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SampleTaxonomyInitializer implements ApplicationRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleTaxonomyInitializer.class);

	private final TaxonomyNodeRepository nodeRepository;
	private final TaxonomyNodeService nodeService;

	public SampleTaxonomyInitializer(TaxonomyNodeRepository nodeRepository, TaxonomyNodeService nodeService) {
		this.nodeRepository = nodeRepository;
		this.nodeService = nodeService;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (nodeRepository.count() != 0) {
			return;
		}

		TaxonomyNodeResponse life = nodeService.create(new CreateTaxonomyNodeRequest("Life", null));
		TaxonomyNodeResponse animals = nodeService.create(new CreateTaxonomyNodeRequest("Animals", life.id()));
		nodeService.create(new CreateTaxonomyNodeRequest("Plants", life.id()));
		nodeService.create(new CreateTaxonomyNodeRequest("Fungi", life.id()));
		nodeService.create(new CreateTaxonomyNodeRequest("Mammals", animals.id()));
		nodeService.create(new CreateTaxonomyNodeRequest("Birds", animals.id()));

		LOGGER.info("Seeded sample taxonomy '{}': {}", life.name(),
				nodeService.findDescendants(life.id()).stream().map(TaxonomyPathNodeResponse::name).toList());
	}

}
