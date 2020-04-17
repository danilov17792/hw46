import org.junit.Assert;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class javaTest {
    TestJava tj;

    public javaTest() {
        this.tj = new TestJava();
    }

    @Test
    public void pullOutMethodShouldReturnValuesAfterLastValue4() {
        int[] input = {1, 2, 4, 4, 2, 3, 4, 1, 7};
        int[] output = {1, 7};

        Assert.assertArrayEquals(output, tj.pullOut(input));
    }

    @Test
    public void pullOutMethodShouldReturnEmptyArrayWhenLastElementValue4() {
        int[] input = {1, 2, 3, 4};
        int[] output = {};

        Assert.assertArrayEquals(output, tj.pullOut(input));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void pullOutMethodShouldThrowRuntimeExceptionWhenArrayDoesNotContainValue4() {
        int[] input = {1, 2, 3};
        int[] output = {};

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("В массиве нет элемента, содержащего значение 4");

        Assert.assertArrayEquals(output, tj.pullOut(input));
    }

    @Test
    public void pullOutMethodShouldThrowRuntimeExceptionWhenArrayContainsNoElements() {
        int[] input = {};
        int[] output = {};

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Массив должен содержать хотя бы один элемент");

        Assert.assertArrayEquals(output, tj.pullOut(input));
    }

    @Test
    public void checkMethodTest1() {
        int[] input = {2, 3, 4};
        Assert.assertTrue(tj.check(input));
    }

    @Test
    public void checkMethodTest2() {
        int[] input = {1, 2, 3};
        Assert.assertTrue(tj.check(input));
    }

    @Test
    public void checkMethodTest3() {
        int[] input = {1, 2, 3, 4};
        Assert.assertTrue(tj.check(input));
    }

    @Test
    public void checkMethodTest4() {
        int[] input = {2, 3};
        Assert.assertFalse(tj.check(input));
    }

    @Test
    public void checkMethodTest5() {
        int[] input = {};
        Assert.assertFalse(tj.check(input));
    }
}