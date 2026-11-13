package com.rba.taxonomy.taxonomy_service.node;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "taxonomy_node_closure")
public class TaxonomyNodeClosure {

	@EmbeddedId
	private TaxonomyNodeClosureId id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("ancestorId")
	@JoinColumn(name = "ancestor_id", nullable = false)
	private TaxonomyNode ancestor;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("descendantId")
	@JoinColumn(name = "descendant_id", nullable = false)
	private TaxonomyNode descendant;

	private int depth;

	protected TaxonomyNodeClosure() {
	}

	public TaxonomyNodeClosure(TaxonomyNode ancestor, TaxonomyNode descendant, int depth) {
		this.id = new TaxonomyNodeClosureId(ancestor.getId(), descendant.getId());
		this.ancestor = ancestor;
		this.descendant = descendant;
		this.depth = depth;
	}

	public TaxonomyNode getAncestor() {
		return ancestor;
	}

	public TaxonomyNode getDescendant() {
		return descendant;
	}

	public int getDepth() {
		return depth;
	}

}
