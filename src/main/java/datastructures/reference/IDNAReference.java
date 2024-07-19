package datastructures.reference;

import core.BaseSequence;
import utils.AddressedDNA;
import utils.FuncUtils;

import java.util.Arrays;
import java.util.function.Function;

public interface IDNAReference<S extends DNAPointer> {
    S sketch();

    default BaseSequence[] addresses() {
        return sketch().addresses();
    }
    BaseSequence[] payloads();
    AddressedDNA[] oligos();
    BaseSequence[] joinedOligos();

    <T> IDNAFedReference<T, S> toFedReference(Function<IDNAReference<S>, T> decoder);

    static IDNAReference<DNAPointer.NativeDNAPointer> of(DNAPointer.NativeDNAPointer sketch, BaseSequence[] payloads) {
        return new DNAReference<>(sketch, __ -> payloads);
    }

    static IDNAReference<DNAPointer.ContainerDNAPointer> of(DNAPointer.ContainerDNAPointer sketch, BaseSequence[] payloads) {
        return new DNAReference<>(sketch, __ -> payloads);
    }

    static IDNAReference<DNAPointer.ContainerDNAPointer> of(DNAPointer.ContainerDNAPointer sketch) {
        return new DNAReference<>(sketch, s -> Arrays.stream(s.container().getOligos(s.id())).map(AddressedDNA::payload).toArray(BaseSequence[]::new));
    }

    class DNAReference<S extends DNAPointer> implements IDNAReference<S> {
        protected final S sketch;
        protected final Function<S, BaseSequence[]> payloadExtractor;

        public DNAReference(S sketch, Function<S, BaseSequence[]> payloadExtractor) {
            this.sketch = sketch;
            this.payloadExtractor = payloadExtractor;
        }

        public DNAReference(S sketch, BaseSequence[] payloads) {
            this.sketch = sketch;
            this.payloadExtractor = __ -> payloads;
        }

        @Override
        public BaseSequence[] addresses() {
            return isNullPointer() ? null : sketch.addresses();
        }

        public BaseSequence[] payloads() {
            return isNullPointer() ? null : payloadExtractor.apply(sketch);
        }

        public int size() {
            return isNullPointer() ? 0 : addresses().length;
        }

        @Override
        public AddressedDNA[] oligos() {
            return FuncUtils.zip(Arrays.stream(addresses()), Arrays.stream(payloads()), AddressedDNA::new).toArray(AddressedDNA[]::new);
        }

        @Override
        public BaseSequence[] joinedOligos() {
            return FuncUtils.zip(Arrays.stream(addresses()), Arrays.stream(payloads()), BaseSequence::join).toArray(BaseSequence[]::new);
        }

        @Override
        public S sketch() {
            return sketch;
        }

        public boolean isNullPointer() {
            if (sketch == null)
                return true;

            var addrs = sketch.addresses();
            return addrs == null || addrs.length == 0;
        }

        @Override
        public <T> IDNAFedReference<T, S> toFedReference(Function<IDNAReference<S>, T> decoder) {
            return new IDNAFedReference.DNAFedReference<>(sketch, payloadExtractor, decoder);
        }
    }
}


