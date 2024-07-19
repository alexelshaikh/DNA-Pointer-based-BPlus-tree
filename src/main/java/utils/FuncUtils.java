package utils;

import org.json.JSONObject;
import utils.rand.Ranlux;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class FuncUtils {
    @FunctionalInterface
    public interface RunnableAttempt {
        void run() throws Exception;
    }

    /**
     * Runs the given runnableAttempt and converts all exceptions to RuntimeExceptions.
     * @param runnableAttempt the RunnableAttempt object.
     */
    public static void safeRun(RunnableAttempt runnableAttempt) {
        try {
            runnableAttempt.run();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param callable the Callable object.
     * @return the result of the callable object. Converts all exceptions to RuntimeExceptions.
     */
    public static <T> T safeCall(Callable<T> callable) {
        try {
            return callable.call();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param callable the Callable object.
     * @return the result of the callable object, ignoring all exceptions. Returns null in the case of exceptions.
     */
    public static <T> T superSafeCall(Callable<T> callable) {
        try {
            return callable.call();
        }
        catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Runs the given runnableAttempt and ignores all exceptions.
     * @param runnableAttempt the RunnableAttempt object.
     * @return true if no exception was thrown, and false otherwise.
     */
    public static boolean superSafeRun(RunnableAttempt runnableAttempt) {
        try {
            runnableAttempt.run();
            return true;
        }
        catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Parses text from the beginning of the given string to produce a number. The method may not use the entire text of the given string.
     * @param s the input String.
     * @return the Number for the input String.
     */
    public static Number parseNumber(String s) {
        return safeCall(() -> NumberFormat.getInstance(Locale.ENGLISH).parse(s));
    }

    /**
     * Attempts to run rTry. If an exception was thrown, it is ignored and escape is run instead.
     * @param rTry the RunnableAttempt object that is attempted to run.
     * @param escape the other RunnableAttempt object that is run if rTry threw an exception.
     */
    public static void tryOrElse(RunnableAttempt rTry, RunnableAttempt escape) {
        if (!superSafeRun(rTry))
            safeRun(escape);
    }

    /**
     * Attempts to call and return rCallable. If an exception was thrown or the result was null, escapeCallable is called and returned instead.
     * @param rCallable the Callable object that is attempted to be called and returned.
     * @param escapeCallable the other Callable object that is called and returned if rCallable threw an exception or returned null.
     */
    public static <T> T tryOrElse(Callable<T> rCallable, Callable<T> escapeCallable) {
        try {
            return rCallable.call();
        }
        catch (Exception ignored) {
            return safeCall(escapeCallable);
        }
    }

    /**
     * Shuffles the given int array in-place.
     * @param array the supplied array to shuffle.
     */
    public static void shuffle(int[] array) {
        Ranlux rand = new Ranlux(3, ThreadLocalRandom.current().nextLong());
        int takes = array.length - 1;
        int z;
        for (int i = takes; i > 0; i--) {
            z = rand.choose(0, i);
            int iArray = array[i];
            array[i] = array[z];
            array[z] = iArray;
        }
    }

    /**
     * Returns a Permutation (using a seed) with the Fisher-Yates method that is uniform for the given seed and length of sequence.
     * @param seed the seed for the random numbers' generator.
     * @return the Permutation instance.
     */
    public static Permutation getUniformPermutation(long seed, int length) {
        return getUniformPermutation(Ranlux.MAX_LUXURY_LEVEL, seed, length);
    }

    /**
     * Returns a Permutation (using a seed) with the Fisher-Yates method that is uniform for the given seed and length of sequence.
     * @param seed the seed for the random numbers' generator.
     * @param luxuryLevel the luxury level (1,2,3,4) for RandLux. The luxury level 4 is the highest.
     * @return the Permutation instance.
     */
    public static Permutation getUniformPermutation(int luxuryLevel, long seed, int length) {
        Ranlux rand = new Ranlux(luxuryLevel, seed);
        int takes = length - 1;
        int[] swapIndexes = new int[takes * 2];
        int c = 0;
        for (int i = takes; i > 0; i--) {
            swapIndexes[c++] = i;
            swapIndexes[c++] = rand.choose(0, i);
        }

        return new Permutation(swapIndexes);
    }

    /**
     * Converts an iterator object to a respective stream.
     * @param it the input iterator.
     * @return a stream containing the elements of the given iterator.
     */
    public static <T> Stream<T> stream(Iterable<T> it) {
        return StreamSupport.stream(it.spliterator(), false);
    }

    /**
     * @param s the stream.
     * @param parallel specifies whether this stream should be parallel.
     * @return the same stream that is now parallel or sequential depending on the specified boolean.
     */
    public static <S extends BaseStream<?, S>> S stream(S s, boolean parallel) {
        return parallel? s.parallel() : s.sequential();
    }

    /**
     * Converts a list of bytes to a byte array.
     * @param data the list of bytes.
     * @return the byte array.
     */
    public static byte[] transformByteListToPrimitive(List<Byte> data) {
        byte[] result = new byte[data.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = data.get(i);

        return result;
    }

    /**
     * Creates a stream of lists or chunks for an input stream, each of the lists is of the same chunk size (besides the last chunk/list that could contain up to 2 * chunkSize - 1 elements).
     * @param stream the input stream.
     * @param chunkSize the chunk size.
     * @return the chunked stream.
     */
    public static <T> Stream<List<T>> chunkConservative(Stream<T> stream, int chunkSize) {
        return stream(() -> new Iterator<>() {
            final Iterator<T> it = stream.iterator();
            List<T> buffer = buffer();

            @Override
            public boolean hasNext() {
                return buffer != null && !buffer.isEmpty();
            }

            @Override
            public List<T> next() {
                List<T> lookahead = buffer();
                int lookaheadSize = lookahead.size();
                if (lookaheadSize < chunkSize) {
                    buffer.addAll(lookahead);
                    List<T> r = buffer;
                    buffer = null;
                    return r;
                }
                List<T> r = buffer;
                if (lookaheadSize == 0)
                    buffer = null;
                else
                    buffer = lookahead;
                return r;
            }

            private List<T> buffer() {
                List<T> buff = new ArrayList<>(chunkSize);
                int c = chunkSize;
                while(c-- > 0 && it.hasNext())
                    buff.add(it.next());

                return buff;
            }
        });
    }

    /**
     * Creates a zipped stream of two input streams. The resulting zipped stream contains pairs in which the first object is from the first stream, and the second object is from the second stream at the same position. The objects are filled up with null if the two streams are not of equal length.
     * @param s1 the first input stream.
     * @param s2 the second input stream.
     * @return the zipped stream.
     */
    public static <T1, T2, S1 extends BaseStream<T1, S1>, S2 extends BaseStream<T2, S2>> Stream<Pair<T1, T2>> zip(S1 s1, S2 s2) {
        return zip(s1, s2, Pair::new);
    }

    /**
     * Creates a zipped stream of two input streams. The resulting zipped stream contains Objects created by the BiFunction<T1, T2, Z> supplied in which the first object is from the first stream, and the second object is from the second stream at the same position. The objects are filled up with null if the two streams are not of equal length.
     * @param s1 the first input stream.
     * @param s2 the second input stream.
     * @param zof the function that zips one object from s1 and another from s2 to a zipped object.
     * @return the zipped stream.
     */
    public static <T1, T2, Z, S1 extends BaseStream<T1, S1>, S2 extends BaseStream<T2, S2>> Stream<Z> zip(S1 s1, S2 s2, BiFunction<T1, T2, Z> zof) {
        return stream(() ->  new Iterator<>() {
            final Iterator<T1> it1 = s1.iterator();
            final Iterator<T2> it2 = s2.iterator();

            @Override
            public boolean hasNext() {
                return it1.hasNext() || it2.hasNext();
            }

            @Override
            public Z next() {
                return zof.apply(nextT(it1), nextT(it2));
            }

            private <T> T nextT(Iterator<T> it) {
                return it.hasNext() ? it.next() : null;
            }
        });
    }

    /**
     * Returns a consistent hash value for an arbitrary object. The input object can be a reference, primitive, or an array of the previous options. This method always returns the same hash value for two equal objects.
     * @param o the object for which a consistent hash value is to be computed.
     * @return the consistent hash value.
     */
    public static int consistentHash(Object o) {
        if (o == null)
            return 0;

        if (o.getClass().isArray()) {
            if (o instanceof Object[] oo)
                return Arrays.hashCode(oo);
            else if (o instanceof boolean[] oo)
                return Arrays.hashCode(oo);
            else if (o instanceof byte[] oo)
                return Arrays.hashCode(oo);
            else if (o instanceof char[] oo)
                return Arrays.hashCode(oo);
            else if (o instanceof double[] oo)
                return Arrays.hashCode(oo);
            else if (o instanceof float[] oo)
                return Arrays.hashCode(oo);
            else if (o instanceof int[] oo)
                return Arrays.hashCode(oo);
            else if (o instanceof long[] oo)
                return Arrays.hashCode(oo);
            else if (o instanceof short[] oo)
                return Arrays.hashCode(oo);
            else
                throw new AssertionError();
        }
        else
            return Objects.hashCode(o);
    }

    /**
     * Creates a stream that consists of the same elements as the input stream, but numbered from 0 to n-1.
     * @param stream the input stream.
     * @return an enumerated stream.
     */
    public static <T, S extends BaseStream<T, S>> Stream<Pair<Integer, T>> enumerate(S stream) {
        AtomicInteger i = new AtomicInteger(0);
        return StreamSupport.stream(stream.spliterator(), stream.isParallel()).map(e -> new Pair<>(i.getAndIncrement(), e));
    }

    /**
     * Serializes a given Serializable object to a byte array.
     * @param object the Serializable object.
     * @return the byte array encoding the Serializable object.
     */
    public static byte[] serializeToByteArray(Serializable object) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(object);
        objOut.close();
        return byteOut.toByteArray();
    }

    /**
     * Serializes a given Serializable object to a byte array safely, i.e., Exceptions are converted to RunTimeExceptions.
     * @param object the Serializable object.
     * @return the byte array encoding the Serializable object.
     */
    public static byte[] serializeToByteArraySafe(Serializable object) {
        return safeCall(() -> serializeToByteArray(object));
    }

    /**
     * Reads the given byte array and deserializes it to back the Java Serializable object.
     * @param byteArray the byte array encoding the Serializable object.
     * @return the deserialized object.
     * @param <T> the cast type for the deserialized object.
     */
    public static <T> T deserializeFromByteArray(byte[] byteArray) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteArray);
        ObjectInputStream objIn = new ObjectInputStream(byteIn);
        Object object = objIn.readObject();
        objIn.close();
        return (T) object;
    }

    /**
     * Reads the given byte array and deserializes it to back the Java Serializable object safely, i.e., Exceptions are converted to RunTimeExceptions.
     * @param byteArray the byte array encoding the Serializable object.
     * @return the deserialized object.
     * @param <T> the cast type for the deserialized object.
     */
    public static <T> T deserializeFromByteArraySafe(byte[] byteArray) {
        return safeCall(() -> deserializeFromByteArray(byteArray));
    }

    /**
     * Reverses a given character sequence.
     * @param s the character sequence.
     * @return a new character sequence that represents the input characters in reverse order.
     */
    public static CharSequence reverseCharSequence(CharSequence s) {
        int len = s.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = len - 1; i >= 0; i--)
            sb.append(s.charAt(i));

        return sb;
    }

    /**
     * @param array the array containing all possible outcomes.
     * @return a random element in array.
     */
    public static <T> T random(T... array) {
        return array[(int) (Math.random() * array.length)];
    }


    public static <T> T nullEscape(T t, Supplier<T> escape) {
        return conditionOrElse(Objects::nonNull, t, escape);
    }

    public static <T> T nullEscape(T t, T escape) {
        return conditionOrElse(Objects::nonNull, t, () -> escape);
    }

    public static <T> T conditionOrElse(Predicate<T> p, T t, Supplier<T> escape) {
        return p.test(t) ? t : escape.get();
    }

    public static JSONObject loadConfigFile(String configPath) {
       return new JSONObject(FuncUtils.safeCall(() -> Files.readAllLines(Path.of(configPath)).stream().filter(l -> !l.matches("[ \t]*#.*")).collect(Collectors.joining())));
    }
}
