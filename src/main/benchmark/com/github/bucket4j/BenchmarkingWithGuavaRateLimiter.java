/*
 * Copyright 2015 Vladimir Bukhtoyarov
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.bucket4j;

import com.github.bucket4j.state.GuavaNanotimePrecisionLimiterState;
import com.github.bucket4j.state.LocalNanotimePrecisionState;
import com.github.bucket4j.state.ThreadDistributionState;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class BenchmarkingWithGuavaRateLimiter {

    @Benchmark
    public double baseline(ThreadDistributionState threadLocalCounter) {
        return threadLocalCounter.invocationCount++;
    }

    @Benchmark
    public boolean benchmarkLocalThreadSafe(LocalNanotimePrecisionState state, ThreadDistributionState threadLocalCounter) {
        boolean result = state.bucket.tryConsumeSingleToken();
        threadLocalCounter.invocationCount++;
        return result;
    }

    @Benchmark
    public boolean benchmarkGuavaLimiter(GuavaNanotimePrecisionLimiterState state, ThreadDistributionState threadLocalCounter) {
        boolean result = state.guavaRateLimiter.tryAcquire();
        threadLocalCounter.invocationCount++;
        return result;
    }

    public static class OneThread {

        public static void main(String[] args) throws RunnerException {
            benchmark(1);
        }

    }

    public static class TwoThreads {

        public static void main(String[] args) throws RunnerException {
            benchmark(2);
        }

    }

    public static class FourThreads {

        public static void main(String[] args) throws RunnerException {
            benchmark(4);
        }

    }

    private static void benchmark(int threadCount) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkingWithGuavaRateLimiter.class.getSimpleName())
                .warmupIterations(10)
                .measurementIterations(10)
                .threads(threadCount)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}
