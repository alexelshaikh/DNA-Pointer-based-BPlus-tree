package dnacoders.tree.coders;

import datastructures.reference.DNAPointer;
import datastructures.searchtrees.BPlusTree;
import dnacoders.tree.wrappers.node.DecodedNode;
import dnacoders.tree.wrappers.node.EncodedNode;
import dnacoders.tree.wrappers.tree.EncodedBPTree;
import utils.AsymmetricCoder;

import java.util.stream.Stream;

public interface BPTreeAsymmetricCoder<K extends Comparable<K>, V, S extends DNAPointer, ADDR> extends AsymmetricCoder<BPlusTree<K, V>, Stream<DecodedNode<K, S>>, EncodedBPTree<K, V, ADDR, S>> {

    boolean isParallel();
    DecodedNode<K, S> decodeNode(EncodedNode<S> encodedNode);
}
