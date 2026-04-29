package com.example.task.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidTaskExceptionTest {

    @Test
    void constructor_validMessage_setsMessage() {
        InvalidTaskException ex = new InvalidTaskException("bad input");
        assertEquals("bad input", ex.getMessage());
    }

    @Test
    void isRuntimeException_validInput_returnsTrue() {
        InvalidTaskException ex = new InvalidTaskException("bad input");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
