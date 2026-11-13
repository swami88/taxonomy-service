package com.rba.taxonomy.taxonomy_service.node;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaxonomyNodeServiceTests {

	@Mock
	private TaxonomyNodeRepository nodeRepository;

	@Mock
	private TaxonomyNodeClosureRepository closureRepository;

	@InjectMocks
	private TaxonomyNodeService nodeService;

	@Captor
	private ArgumentCaptor<TaxonomyNode> nodeCaptor;

	@Captor
	private ArgumentCaptor<TaxonomyNodeClosure> closureCaptor;

	@Captor
	private ArgumentCaptor<List<TaxonomyNodeClosure>> closuresCaptor;

	@Test
	void createsRootWithSelfReferentialClosureRow() {
		when(nodeRepository.saveAndFlush(any(TaxonomyNode.class))).thenAnswer(invocation -> {
			TaxonomyNode node = invocation.getArgument(0);
			setId(node, 10L);
			return node;
		});

		TaxonomyNodeResponse response = nodeService.create(new CreateTaxonomyNodeRequest("  Animals  ", null));

		assertThat(response).isEqualTo(new TaxonomyNodeResponse(10L, "Animals", null));
		verify(nodeRepository).saveAndFlush(nodeCaptor.capture());
		assertThat(nodeCaptor.getValue().getName()).isEqualTo("Animals");
		verify(closureRepository).save(closureCaptor.capture());
		assertThat(closureCaptor.getValue().getAncestor()).isSameAs(nodeCaptor.getValue());
		assertThat(closureCaptor.getValue().getDescendant()).isSameAs(nodeCaptor.getValue());
		assertThat(closureCaptor.getValue().getDepth()).isZero();
		verify(closureRepository, never()).findAncestorPaths(any());
		verify(closureRepository, never()).saveAll(any());
	}

	@Test
	void createsChildWithClosureRowsForEveryParentAncestor() {
		TaxonomyNode root = nodeWithId(1L, "Animals");
		TaxonomyNode parent = nodeWithId(2L, "Mammals");
		TaxonomyNode child = nodeWithId(3L, "Primates");
		when(nodeRepository.findById(2L)).thenReturn(Optional.of(parent));
		when(nodeRepository.saveAndFlush(any(TaxonomyNode.class))).thenReturn(child);
		when(closureRepository.findAncestorPaths(2L)).thenReturn(List.of(
				new TaxonomyNodeClosure(root, parent, 1),
				new TaxonomyNodeClosure(parent, parent, 0)));

		TaxonomyNodeResponse response = nodeService.create(new CreateTaxonomyNodeRequest("Primates", 2L));

		assertThat(response).isEqualTo(new TaxonomyNodeResponse(3L, "Primates", 2L));
		verify(closureRepository).save(closureCaptor.capture());
		assertThat(closureCaptor.getValue().getAncestor()).isSameAs(child);
		assertThat(closureCaptor.getValue().getDescendant()).isSameAs(child);
		assertThat(closureCaptor.getValue().getDepth()).isZero();
		verify(closureRepository).saveAll(closuresCaptor.capture());
		assertThat(closuresCaptor.getValue())
				.extracting(TaxonomyNodeClosure::getAncestor, TaxonomyNodeClosure::getDescendant,
						TaxonomyNodeClosure::getDepth)
				.containsExactly(
						org.assertj.core.groups.Tuple.tuple(root, child, 2),
						org.assertj.core.groups.Tuple.tuple(parent, child, 1));
	}

	@Test
	void findsNodeWithItsDirectParent() {
		TaxonomyNode node = nodeWithId(3L, "Primates");
		when(nodeRepository.findById(3L)).thenReturn(Optional.of(node));
		when(closureRepository.findParentId(3L)).thenReturn(Optional.of(2L));

		assertThat(nodeService.findById(3L)).isEqualTo(new TaxonomyNodeResponse(3L, "Primates", 2L));
	}

	@Test
	void findsAncestorsWithoutTheNodeSelfRow() {
		TaxonomyNode root = nodeWithId(1L, "Animals");
		TaxonomyNode parent = nodeWithId(2L, "Mammals");
		TaxonomyNode node = nodeWithId(3L, "Primates");
		when(nodeRepository.findById(3L)).thenReturn(Optional.of(node));
		when(closureRepository.findAncestorPaths(3L)).thenReturn(List.of(
				new TaxonomyNodeClosure(root, node, 2),
				new TaxonomyNodeClosure(parent, node, 1),
				new TaxonomyNodeClosure(node, node, 0)));

		assertThat(nodeService.findAncestors(3L)).containsExactly(
				new TaxonomyPathNodeResponse(1L, "Animals", 2),
				new TaxonomyPathNodeResponse(2L, "Mammals", 1));
	}

	@Test
	void findsDescendantsWithoutTheNodeSelfRow() {
		TaxonomyNode root = nodeWithId(1L, "Animals");
		TaxonomyNode child = nodeWithId(2L, "Mammals");
		TaxonomyNode grandchild = nodeWithId(3L, "Primates");
		when(nodeRepository.findById(1L)).thenReturn(Optional.of(root));
		when(closureRepository.findDescendantPaths(1L)).thenReturn(List.of(
				new TaxonomyNodeClosure(root, root, 0),
				new TaxonomyNodeClosure(root, child, 1),
				new TaxonomyNodeClosure(root, grandchild, 2)));

		assertThat(nodeService.findDescendants(1L)).containsExactly(
				new TaxonomyPathNodeResponse(2L, "Mammals", 1),
				new TaxonomyPathNodeResponse(3L, "Primates", 2));
	}

	@Test
	void renamesNodeAndRetainsItsParent() {
		TaxonomyNode node = nodeWithId(3L, "Primates");
		when(nodeRepository.findById(3L)).thenReturn(Optional.of(node));
		when(closureRepository.findParentId(3L)).thenReturn(Optional.of(2L));

		assertThat(nodeService.rename(3L, new RenameTaxonomyNodeRequest("  Apes  ")))
				.isEqualTo(new TaxonomyNodeResponse(3L, "Apes", 2L));
		assertThat(node.getName()).isEqualTo("Apes");
	}

	@Test
	void rejectsRequestsForMissingNodes() {
		when(nodeRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> nodeService.findDescendants(99L))
				.isInstanceOf(NoSuchElementException.class)
				.hasMessage("Taxonomy node 99 was not found");
		verify(closureRepository, never()).findDescendantPaths(eq(99L));
	}

	private static TaxonomyNode nodeWithId(long id, String name) {
		TaxonomyNode node = new TaxonomyNode(name);
		setId(node, id);
		return node;
	}

	private static void setId(TaxonomyNode node, long id) {
		try {
			Field field = TaxonomyNode.class.getDeclaredField("id");
			field.setAccessible(true);
			field.set(node, id);
		}
		catch (ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

}
