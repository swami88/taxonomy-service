package com.rba.taxonomy.taxonomy_service.node;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/nodes")
public class TaxonomyNodeController {

	private final TaxonomyNodeService nodeService;

	public TaxonomyNodeController(TaxonomyNodeService nodeService) {
		this.nodeService = nodeService;
	}

	@PostMapping
	public ResponseEntity<TaxonomyNodeResponse> create(@Valid @RequestBody CreateTaxonomyNodeRequest request) {
		TaxonomyNodeResponse response = nodeService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping("/{id}")
	public TaxonomyNodeResponse findById(@PathVariable Long id) {
		return nodeService.findById(id);
	}

	@GetMapping("/{id}/ancestors")
	public List<TaxonomyPathNodeResponse> findAncestors(@PathVariable Long id) {
		return nodeService.findAncestors(id);
	}

	@GetMapping("/{id}/descendants")
	public List<TaxonomyPathNodeResponse> findDescendants(@PathVariable Long id) {
		return nodeService.findDescendants(id);
	}

	@PatchMapping("/{id}")
	public TaxonomyNodeResponse rename(@PathVariable Long id, @Valid @RequestBody RenameTaxonomyNodeRequest request) {
		return nodeService.rename(id, request);
	}

}
