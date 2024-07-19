import core.BaseSequence;
import datastructures.KVEntry;
import datastructures.container.DNAContainer;
import datastructures.reference.DNAPointer;
import datastructures.searchtrees.BPlusTree;
import dnacoders.tree.coders.BPTreeContainerCoder;
import dnacoders.tree.coders.BPTreeNativeCoder;
import dnacoders.tree.sketchers.AbstractHashSketcher;
import dnacoders.tree.sketchers.IDNASketcher;
import dnacoders.tree.wrappers.node.EncodedNode;
import dnacoders.tree.wrappers.tree.LNALContainerEncodedTree;
import dnacoders.tree.wrappers.tree.LNALNativeEncodedTree;
import utils.Coder;
import utils.DNAPacker;
import utils.lsh.minhash.MinHashLSH;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class DPBAndContainerTreesTest {

    static int addrSize = 80;
    static int payloadSize = 150 - addrSize;
    static Coder<List<Integer>, BaseSequence> integerCoder = new Coder<>() {
        @Override
        public BaseSequence encode(List<Integer> sortedList) {

            BaseSequence seq = new BaseSequence();
            int id0 = sortedList.getFirst();
            int id;
            int size = sortedList.size();
            DNAPacker.packUnsigned(seq, id0);
            for (int i = 1; i < size; i++) {
                id = sortedList.get(i);
                DNAPacker.packUnsigned(seq, id - id0);
                id0 = id;
            }

            return seq;
        }

        @Override
        public List<Integer> decode(BaseSequence seq) {
            DNAPacker.LengthBase lb = DNAPacker.LengthBase.parsePrefix(seq);
            List<Integer> keys = new ArrayList<>();
            int delta = lb.unpackSingle(seq, false).intValue();
            keys.add(delta);
            seq = seq.window(lb.totalSize());

            while (seq.length() > 0) {
                lb = DNAPacker.LengthBase.parsePrefix(seq);
                delta += lb.unpackSingle(seq, false).intValue();
                keys.add(delta);
                seq = seq.window(lb.totalSize());
            }

            return keys;
        }
    };

    static IDNASketcher<DNAPointer.NativeDNAPointer> sketcher = AbstractHashSketcher.builder().setFlavor(AbstractHashSketcher.Builder.Flavor.F2).setAddressSize(addrSize).build();

    static BPTreeNativeCoder<Integer, Integer, DNAPointer.NativeDNAPointer> bpbCoder = new BPTreeNativeCoder.Builder<Integer, Integer, DNAPointer.NativeDNAPointer>()
            .setPayloadSize(payloadSize)
            .setToleranceFunctionLeaves(__ -> 0)
            .setToleranceFunctionInternalNodes(__ -> 0)
            .setLsh(MinHashLSH.newSeqLSHTraditional(6, 5))
            .setKeyCoder(integerCoder)
            .setValueCoder(integerCoder)
            //.setSeedTrialsAndTargetScore(3, 0.5f)
            //.setSeedTrialsWithMaxScore(2)
            .setParallel(true)
            .setSketcher(sketcher)
            .build();

    static DNAContainer dnaContainer = DNAContainer.builder()
            .setPayloadSize(payloadSize)
            .setOligoLSH(MinHashLSH.newSeqLSHTraditional(6, 5))
            .setParallel(true)
            .build();
    static BPTreeContainerCoder<Integer, Integer> containerCoder = new BPTreeContainerCoder<>(
            dnaContainer,
            integerCoder,
            integerCoder
    );

    public static void main(String[] args) {
        int b = 2;
        int c = 2;
        BPlusTree<Integer, Integer> btree = BPlusTree.bulkLoad(IntStream.range(0, 10).mapToObj(i -> new KVEntry<>(i, i)), b, c);
        LNALNativeEncodedTree<Integer, Integer, DNAPointer.NativeDNAPointer> bpbTree = bpbCoder.encode(btree);

        int x = 1;
        int y = 5;
        System.out.println("Number of oligos in the BPB B+-tree: " + bpbTree.getEncodedNodeStorage().collect().stream().mapToInt(EncodedNode::oligosCount).sum());
        System.out.println("Executing the range query [" + x + ", " + y + "] on the BPB B+-tree:       " + bpbTree.search(x, y).toList());
        System.out.println();



        LNALContainerEncodedTree<Integer, Integer> containerTree = containerCoder.encode(btree);
        System.out.println("Number of oligos in the Container B+-tree: " + containerTree.getEncodedNodeStorage().collect().stream().mapToInt(EncodedNode::oligosCount).sum());
        System.out.println("Executing the range query [" + x + ", " + y + "] on the Container B+-tree: " + containerTree.search(x, y).toList());
    }
}
