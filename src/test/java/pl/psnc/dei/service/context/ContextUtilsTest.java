package pl.psnc.dei.service.context;


import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

public class ContextUtilsTest {
    @Test
    public void executeIfPresent_runsOnlyIfPresent() {
        Object somethingPresent = new Object();
        AtomicBoolean executed = new AtomicBoolean(false);
        ContextUtils.executeIfPresent(null,
                Assert::fail
        );
        ContextUtils.executeIfPresent(somethingPresent,
                () -> executed.set(true)
        );
        assertTrue(executed.get());
    }

    @Test
    public void executeIfNotPresent_runsOnlyIfNotPresent() {
        Object somethingPresent = new Object();
        AtomicBoolean executed = new AtomicBoolean(false);
        ContextUtils.executeIfNotPresent(null, () -> executed.set(true));
        ContextUtils.executeIfNotPresent(somethingPresent, Assert::fail);
        assertTrue(executed.get());
    }

    @Test
    public void executeIf_executesOnlyIfTrue() {
        final boolean alwaysTrue = true;
        AtomicBoolean executed = new AtomicBoolean(false);
        ContextUtils.executeIf(alwaysTrue, () -> executed.set(true));
        assertTrue(executed.get());
    }

    @Test
    public void executeIfNotEmpty_runsOnlyIfArrayNotEmpty() {
        final List<Integer> notEmptyList = new ArrayList<>(List.of(1, 2, 3, 4));
        final List<String> emptyList = new ArrayList<>();
        AtomicBoolean executed = new AtomicBoolean(false);

        ContextUtils.executeIfNotEmpty(notEmptyList, () -> executed.set(true));
        ContextUtils.executeIfNotEmpty(emptyList, Assert::fail);
        assertTrue(executed.get());
    }

    @Test
    public void executeIfEmpty_executesOnlyIfArrayEmpty() {
        final List<Integer> notEmptyList = new ArrayList<>(List.of(1, 2, 3, 4));
        final List<String> emptyList = new ArrayList<>();
        AtomicBoolean executed = new AtomicBoolean(false);

        ContextUtils.executeIfEmpty(notEmptyList, Assert::fail);
        ContextUtils.executeIfEmpty(emptyList, () -> executed.set(true));
        assertTrue(executed.get());
    }
}
