package com.github.chengyuxing.plugin.tests;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class CompletionTests extends LightPlatformCodeInsightFixture4TestCase {
    @Override
    protected String getTestDataPath() {
        return "src/test/java/com/github/chengyuxing/plugin/tests";
    }

    @Test
    public void test1() {
        myFixture.configureByFile("MyCode.java");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings.containsAll(Arrays.asList("$hello", "$world")));
        assertEquals(2, strings.size());
    }
}
