import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

class Test {
    @ParameterizedTest
    @MethodSource
    fun day(day: Int) {
        val folder = dataFolder(day)
        val input = File(folder, "test_in.txt")
        val output = File(folder, "test_out.txt")
        val ref = File(folder, "test_ref.txt").readLines()

        days[day - 1](PuzzleData(input, output))
        val out = output.readLines()
        assert(out == ref)
    }

    @Test
    fun lastDay() = day(days.size)

    companion object {
        @JvmStatic
        fun day() = IntArray(days.size) { it + 1 }
    }
}