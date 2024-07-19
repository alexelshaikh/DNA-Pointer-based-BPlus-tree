package dnacoders.tree.encodednodestorage;

import datastructures.reference.DNAPointer;
import dnacoders.tree.wrappers.node.EncodedNode;
import utils.Streamable;

import java.util.Collection;

public interface EncodedNodeStorage<ADDR, S extends DNAPointer> extends Streamable<EncodedNode<S>> {
    EncodedNode<S> getRoot();
    boolean isEmpty();
    boolean isParallel();
    Collection<EncodedNode<S>> collect();
    long size();
    EncodedNode<S> findNode(ADDR addresses);
}
