package net.ylophones.fotilo

import junit.framework.TestCase.fail
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.junit.jupiter.api.Test

import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.util.stream.Stream

class InputValidationTest {

    @ParameterizedTest
    @MethodSource("inRange")
    internal fun checkWithinRange(start: Int, end: Int, value: Int) {
        try {
            checkWithinRange("", value, start, end)
        } catch (e: IllegalArgumentException) {
            fail("$value was found to not be in range $start to $end")
        }
    }

    @ParameterizedTest
    @MethodSource("outsideRange")
    internal fun checkOutsideRange(start: Int, end: Int, value: Int) {
        try {
            checkWithinRange("", value, start, end)
            fail("$value was found to be in range $start to $end")
        } catch (e: Exception) {
        }
    }

    companion object InputValidation {
        @JvmStatic
        private fun inRange(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of(0, 10, 0),
                    Arguments.of(0, 10, 1),
                    Arguments.of(0, 10, 9),
                    Arguments.of(0, 10, 10)
            )
        }

        @JvmStatic
        private fun outsideRange(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of(0, 10, -1),
                    Arguments.of(0, 10, 11)
            )
        }
    }
}

