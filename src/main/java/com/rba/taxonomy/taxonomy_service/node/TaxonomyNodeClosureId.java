package com.rba.taxonomy.taxonomy_service.node;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class TaxonomyNodeClosureId implements Serializable {

	@Column(name = "ancestor_id")
	private Long ancestorId;

	@Column(name = "descendant_id")
	private Long descendantId;

	protected TaxonomyNodeClosureId() {
	}

	public TaxonomyNodeClosureId(Long ancestorId, Long descendantId) {
		this.ancestorId = ancestorId;
		this.descendantId = descendantId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof TaxonomyNodeClosureId that)) {
			return false;
		}
		return Objects.equals(ancestorId, that.ancestorId) && Objects.equals(descendantId, that.descendantId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ancestorId, descendantId);
	}

}
