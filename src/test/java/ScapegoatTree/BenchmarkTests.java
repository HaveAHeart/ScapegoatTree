package ScapegoatTree;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;


import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.*;

public class BenchmarkTests {
    private static final int ITEM_COUNT = 100_000;
    private static ArrayList<Integer> arr = new ArrayList();

    @State(Scope.Benchmark)
    public static class BenchMarkState {

        private ScapegoatTree tree05 = new ScapegoatTree<Integer>(0.5);
        private ScapegoatTree tree07 = new ScapegoatTree<Integer>(0.7);
        private Set treeSet = new TreeSet<>();

    }

    private static void fillArray() {
        Random r = new Random();
        for (int i = 0; i < 100000; i++) arr.add(r.nextInt(1000000));
    }

    @Benchmark
    public void testScapegoatTree05add(BenchMarkState state) {
        state.tree05.addAll(arr);
    }

    @Benchmark
    public void testScapegoatTree05rem(BenchMarkState state) {
        state.tree05.removeAll(arr);
    }

    @Benchmark
    public void testScapegoatTree07add(BenchMarkState state) {
        state.tree07.addAll(arr);
    }

    @Benchmark
    public void testScapegoatTree07rem(BenchMarkState state) {
        state.tree07.removeAll(arr);
    }

    @Benchmark
    public void testTreeSetAdd(BenchMarkState state) {
        state.treeSet.addAll(arr);
    }

    @Benchmark
    public void testTreeSetRem(BenchMarkState state) {
        state.treeSet.removeAll(arr);
    }

    public static void main(String[] args) throws RunnerException {
        fillArray();
        Options opt = new OptionsBuilder()
                .include(BenchmarkTests.class.getSimpleName())
                .forks(2)
                .build();

        new Runner(opt).run();
    }

}
