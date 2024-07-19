package dnacoders.tree.encodednodestorage;

import core.BaseSequence;
import datastructures.container.DNAContainer;
import datastructures.reference.DNAPointer;
import dnacoders.tree.coders.BPTreeContainerCoder;
import dnacoders.tree.wrappers.node.EncodedNode;
import utils.AddressedDNA;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

public class EncodedNodeContainerStorage implements EncodedNodeStorage<Long, DNAPointer.ContainerDNAPointer> {

    private final DNAContainer container;
    private final long rootId;
    private final Collection<Long> nodeIds;

    public EncodedNodeContainerStorage(DNAContainer container, long rootId, Collection<Long> nodeIds) {
        this.container = container;
        this.rootId = rootId;
        this.nodeIds = nodeIds;
    }

    @Override
    public EncodedNode<DNAPointer.ContainerDNAPointer> getRoot() {
        var oligos = container.getOligos(rootId);
        return new EncodedNode<>(
                nodeIds.size() == 1L,
                Arrays.stream(oligos).map(AddressedDNA::payload).toArray(BaseSequence[]::new),
                new DNAPointer.ContainerDNAPointer(rootId, container),
                Arrays.stream(oligos).map(AddressedDNA::join).toArray(BaseSequence[]::new)
        );
    }

    @Override
    public boolean isParallel() {
        return true;
    }

    @Override
    public Collection<EncodedNode<DNAPointer.ContainerDNAPointer>> collect() {
        return stream().toList();
    }

    @Override
    public long size() {
        return nodeIds.size();
    }

    @Override
    public EncodedNode<DNAPointer.ContainerDNAPointer> findNode(Long id) {
        var oligos = container.getOligos(id);
        if (oligos == null){
            System.out.println("id not found: " + id);
            return null;
        }

        BaseSequence seq = container.assembleFromOligos(oligos);
        return new EncodedNode<>(
                seq.get(0) == BPTreeContainerCoder.LEAF_MARKER_BASE,
                Arrays.stream(oligos).map(AddressedDNA::payload).toArray(BaseSequence[]::new),
                new DNAPointer.ContainerDNAPointer(id, container),
                Arrays.stream(oligos).map(AddressedDNA::join).toArray(BaseSequence[]::new)
        );
    }

    @Override
    public boolean isEmpty() {
        return nodeIds.isEmpty();
    }

    @Override
    public Iterator<EncodedNode<DNAPointer.ContainerDNAPointer>> iterator() {
        return stream().iterator();
    }

    @Override
    public Stream<EncodedNode<DNAPointer.ContainerDNAPointer>> stream() {
        return nodeIds.stream().map(this::findNode);
    }
}
