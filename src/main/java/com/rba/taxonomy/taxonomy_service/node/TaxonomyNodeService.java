package com.rba.taxonomy.taxonomy_service.node;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaxonomyNodeService {

	private final TaxonomyNodeRepository nodeRepository;
	private final TaxonomyNodeClosureRepository closureRepository;

	public TaxonomyNodeService(TaxonomyNodeRepository nodeRepository, TaxonomyNodeClosureRepository closureRepository) {
		this.nodeRepository = nodeRepository;
		this.closureRepository = closureRepository;
	}

	@Transactional
	public TaxonomyNodeResponse create(CreateTaxonomyNodeRequest request) {
		TaxonomyNode parent = request.parentId() == null ? null : findNode(request.parentId());
		TaxonomyNode node = nodeRepository.saveAndFlush(new TaxonomyNode(request.name().trim()));

		closureRepository.save(new TaxonomyNodeClosure(node, node, 0));
		if (parent != null) {
			List<TaxonomyNodeClosure> parentPaths = closureRepository.findAncestorPaths(parent.getId());
			List<TaxonomyNodeClosure> newPaths = parentPaths.stream()
					.map(path -> new TaxonomyNodeClosure(path.getAncestor(), node, path.getDepth() + 1))
					.toList();
			closureRepository.saveAll(newPaths);
		}

		return toResponse(node, parent == null ? null : parent.getId());
	}

	@Transactional(readOnly = true)
	public TaxonomyNodeResponse findById(Long id) {
		TaxonomyNode node = findNode(id);
		return toResponse(node, closureRepository.findParentId(id).orElse(null));
	}

	@Transactional(readOnly = true)
	public List<TaxonomyPathNodeResponse> findAncestors(Long id) {
		findNode(id);
		return closureRepository.findAncestorPaths(id).stream()
				.filter(path -> path.getDepth() > 0)
				.map(path -> new TaxonomyPathNodeResponse(path.getAncestor().getId(), path.getAncestor().getName(), path.getDepth()))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<TaxonomyPathNodeResponse> findDescendants(Long id) {
		findNode(id);
		return closureRepository.findDescendantPaths(id).stream()
				.filter(path -> path.getDepth() > 0)
				.map(path -> new TaxonomyPathNodeResponse(path.getDescendant().getId(), path.getDescendant().getName(), path.getDepth()))
				.toList();
	}

	@Transactional
	public TaxonomyNodeResponse rename(Long id, RenameTaxonomyNodeRequest request) {
		TaxonomyNode node = findNode(id);
		node.rename(request.name().trim());
		return toResponse(node, closureRepository.findParentId(id).orElse(null));
	}

	private TaxonomyNode findNode(Long id) {
		return nodeRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Taxonomy node %d was not found".formatted(id)));
	}

	private TaxonomyNodeResponse toResponse(TaxonomyNode node, Long parentId) {
		return new TaxonomyNodeResponse(node.getId(), node.getName(), parentId);
	}

}
