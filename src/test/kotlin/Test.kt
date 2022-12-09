import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

class Test {
    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3, 4, 5, 6, 7, 8])
    fun day(day: Int) {
        val folder = dataFolder(day)
        val input = File(folder, "test_in.txt")
        val output = File(folder, "test_out.txt")
        val ref = File(folder, "test_ref.txt").readLines()

        days[day - 1](PuzzleData(input, output))
        val out = output.readLines()
        assert(out == ref)
    }
}