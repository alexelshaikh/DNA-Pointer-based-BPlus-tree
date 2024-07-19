package dnacoders.tree.sketchers;

import core.BaseSequence;
import datastructures.reference.DNAPointer;
import utils.AsymmetricCoder;
import utils.DNAPacker;

import java.util.Arrays;
import java.util.stream.IntStream;

public class DNAHashSketchCoder implements AsymmetricCoder<DNAPointer.NativeDNAPointer, IDNASketcher.DecodedSketch<DNAPointer.NativeDNAPointer>, BaseSequence> {
    private final IDNASketcher<DNAPointer.NativeDNAPointer> sketcher;
    private final boolean parallel;

    public DNAHashSketchCoder(IDNASketcher<DNAPointer.NativeDNAPointer> sketcher, boolean parallel) {
        this.sketcher = sketcher;
        this.parallel = parallel;
    }

    public boolean isParallel() {
        return parallel;
    }

    public DNAHashSketchCoder(IDNASketcher<DNAPointer.NativeDNAPointer> sketcher) {
        this(sketcher, false);
    }

    @Override
    public BaseSequence encode(DNAPointer.NativeDNAPointer sketch) {
        int[] badIds = sketch.badIndices();
        Arrays.sort(badIds);
        BaseSequence seq = new BaseSequence();
        DNAPacker.packUnsigned(seq, sketch.seed());
        DNAPacker.packUnsigned(seq, sketch.n() - 1);
        DNAPacker.packUnsigned(seq, badIds.length);
        if (badIds.length == 0)
            return seq;

        int badId_1 = badIds[0];
        int badId;
        DNAPacker.packUnsigned(seq, badId_1);
        for (int i = 1; i < badIds.length; i++) {
            badId = badIds[i];
            DNAPacker.packUnsigned(seq, badId - badId_1);
            badId_1 = badId;
        }

        return seq;
    }

    @Override
    public IDNASketcher.DecodedSketch<DNAPointer.NativeDNAPointer> decode(BaseSequence seq) {
        DNAPacker.LengthBase lb = DNAPacker.LengthBase.parsePrefix(seq);
        long seed = lb.unpackSingle(seq, false).longValue();
        int serializedSize = lb.totalSize();
        BaseSequence window = seq.window(serializedSize);

        lb = DNAPacker.LengthBase.parsePrefix(window);
        int lbTotalSize = lb.totalSize();
        serializedSize += lbTotalSize;
        int size = lb.unpackSingle(window, false).intValue() + 1;
        window = window.window(lbTotalSize);

        lb = DNAPacker.LengthBase.parsePrefix(window);
        lbTotalSize = lb.totalSize();
        serializedSize += lbTotalSize;
        int badIdsLen = lb.unpackSingle(window, false).intValue();
        window = window.window(lbTotalSize);


        IntStream.Builder b = IntStream.builder();
        if (badIdsLen > 0) {
            lb = DNAPacker.LengthBase.parsePrefix(window);
            lbTotalSize = lb.totalSize();
            serializedSize += lbTotalSize;
            int delta = lb.unpackSingle(window, false).intValue();
            b.add(delta);
            window = window.window(lbTotalSize);

            for (int i = 1; i < badIdsLen; i++) {
                lb = DNAPacker.LengthBase.parsePrefix(window);
                lbTotalSize = lb.totalSize();
                serializedSize += lbTotalSize;
                delta += lb.unpackSingle(window, false).intValue();
                b.add(delta);
                window = window.window(lbTotalSize);
            }
        }

        int[] badIds = b.build().toArray();
        return new IDNASketcher.DecodedSketch<>(DNAPointer.NativeDNAPointer.lazy(seed, size, badIds, sketcher), serializedSize);
    }
}

