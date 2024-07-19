package dnacoders.tree.wrappers.tree;

import datastructures.reference.DNAPointer;
import dnacoders.tree.encodednodestorage.EncodedNodeStorage;
import dnacoders.tree.wrappers.node.DecodedInternalNode;
import dnacoders.tree.wrappers.node.DecodedNode;
import dnacoders.tree.wrappers.node.EncodedNode;
import java.util.function.Function;


public class LNALContainerEncodedTree<K extends Comparable<K>, V> extends AbstractEncodedBPTree<K, V, Long, DNAPointer.ContainerDNAPointer> {
    public LNALContainerEncodedTree(EncodedNodeStorage<Long, DNAPointer.ContainerDNAPointer> encodedNodeStorage, Function<EncodedNode<DNAPointer.ContainerDNAPointer>, DecodedNode<K, DNAPointer.ContainerDNAPointer>> decoder) {
        super(encodedNodeStorage, decoder);
    }

    @Override
    protected Long getAddress(DecodedInternalNode<K, DNAPointer.ContainerDNAPointer> internalNode, K key) {
        return internalNode.findKidSketch(key).id();
    }

    @Override
    protected Long getAddress(DecodedInternalNode<K, DNAPointer.ContainerDNAPointer> internalNode, int index) {
        return internalNode.kidSketchesAt(index).id();
    }

    @Override
    protected Long getAddress(DNAPointer.ContainerDNAPointer sketch) {
        return sketch.id();
    }
}
