package net.blackhacker.ares.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HasherTest {

    private Hasher hasher;

    @BeforeEach
    void setUp() {
        hasher = new Hasher();
    }

    @Test
    void hash_shouldReturnStringOfDefaultLength() {
        String input = "test-string";
        String result = hasher.hash(input);
        
        assertNotNull(result);
        assertEquals(Hasher.MAX_LENGTH, result.length());
    }

    @Test
    void hash_shouldReturnTruncatedString() {
        String input = "test-string";
        int length = 10;
        String result = hasher.hash(input, length);
        
        assertNotNull(result);
        assertEquals(length, result.length());
    }

    @Test
    void hash_shouldCapLengthAtMaxLength() {
        String input = "test-string";
        int length = 100; // Greater than MAX_LENGTH (43)
        String result = hasher.hash(input, length);
        
        assertNotNull(result);
        assertEquals(Hasher.MAX_LENGTH, result.length());
    }

    @Test
    void hash_shouldBeConsistent() {
        String input = "consistent-input";
        String result1 = hasher.hash(input);
        String result2 = hasher.hash(input);
        
        assertEquals(result1, result2);
    }

    @Test
    void hash_shouldBeUniqueForDifferentInputs() {
        String input1 = "input-1";
        String input2 = "input-2";
        
        String result1 = hasher.hash(input1);
        String result2 = hasher.hash(input2);
        
        assertNotEquals(result1, result2);
    }
    
    @Test
    void hash_shouldHandleEmptyString() {
        String input = "";
        String result = hasher.hash(input);
        
        assertNotNull(result);
        assertEquals(Hasher.MAX_LENGTH, result.length());
    }
}
