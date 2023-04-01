package ch.bytecrowd.lazynerd;

import javassist.NotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class UnitTestGeneratorTest {

    @Test
    void test() {
        String generated = UnitTestGenerator.generate(DummyClass.class, "src/test/java", true);
        Assertions.assertThat(
                generated
        ).isNotBlank()
                .isEqualTo("""
                        package ch.bytecrowd.lazynerd;
                                                
                        import org.junit.jupiter.api.BeforeEach;
                        import org.junit.jupiter.api.Test;
                        import org.mockito.junit.jupiter.MockitoExtension;
                        import org.junit.jupiter.api.extension.ExtendWith;
                        import org.mockito.Mock;
                                                
                        import static org.assertj.core.api.Assertions.*;
                        import static org.mockito.Mockito.*;
                                                
                        @ExtendWith(MockitoExtension.class)
                        class DummyClassTest {
                                                
                            String a = "42";
                            String b = "42";
                            String c = "42";
                                                
                            DummyClass target;
                                                
                            @BeforeEach
                            void init() {
                                target = new DummyClass();
                                target = new DummyClass(a);
                                target = new DummyClass(a, b);
                                target = new DummyClass(a, b, c);
                            }
                                                
                            @Test
                            void testWierdMethodWhereNIsNullAndQIsNull() {
                                // GIVEN
                                Integer n = null;
                                Point q = null;
                                                
                                // WHEN
                                var actual = target.wierdMethod(n, q);
                                                
                                // THEN
                                assertThat(actual).isNotNull();
                            }
                                                
                            @Test
                            void testWierdMethodWhereNIs42AndQIsNull() {
                                // GIVEN
                                int n = 42;
                                Point q = null;
                                                
                                // WHEN
                                var actual = target.wierdMethod(n, q);
                                                
                                // THEN
                                assertThat(actual).isNotNull();
                            }
                                                
                            @Test
                            void testWierdMethodWhereNIsNullAndQIsIsMocked() {
                                // GIVEN
                                Integer n = null;
                                Point q = mock(Point.class);
                                                
                                // WHEN
                                var actual = target.wierdMethod(n, q);
                                                
                                // THEN
                                assertThat(actual).isNotNull();
                            }
                                                
                            @Test
                            void testWierdMethodWhereNIs42AndQIsIsMocked() {
                                // GIVEN
                                int n = 42;
                                Point q = mock(Point.class);
                                                
                                // WHEN
                                var actual = target.wierdMethod(n, q);
                                                
                                // THEN
                                assertThat(actual).isNotNull();
                            }
                                                
                            @Test
                            // (n > 0) && ((n % 2) == 0)
                            void testIfConditionInWierdMethod() {
                                // GIVEN
                                int n = 42;
                                Point q = mock(Point.class);
                                                
                                // WHEN
                                var actual = target.wierdMethod(n, q);
                                                
                                // THEN
                                assertThat(actual).isNotNull();
                            }
                                                
                            @Test
                            // n > 0 && n % 2 != 0
                            void testIfConditionInWierdMethod() {
                                // GIVEN
                                int n = 42;
                                Point q = mock(Point.class);
                                                
                                // WHEN
                                var actual = target.wierdMethod(n, q);
                                                
                                // THEN
                                assertThat(actual).isNotNull();
                            }
                                                
                            @Test
                            // n % 2 == 0
                            void testIfConditionInWierdMethod() {
                                // GIVEN
                                int n = 42;
                                Point q = mock(Point.class);
                                                
                                // WHEN
                                var actual = target.wierdMethod(n, q);
                                                
                                // THEN
                                assertThat(actual).isNotNull();
                            }
                                                
                            @Test
                            // n % 2 == 0
                            void testElseConditionInWierdMethod() {
                                // GIVEN
                                int n = 42;
                                Point q = mock(Point.class);
                                                
                                // WHEN
                                var actual = target.wierdMethod(n, q);
                                                
                                // THEN
                                assertThat(actual).isNotNull();
                            }
                                                
                            @Test
                            // q + "" == "0"
                            void testTernaryConditionInWierdMethod() {
                                // GIVEN
                                int n = 42;
                                Point q = mock(Point.class);
                                                
                                // WHEN
                                var actual = target.wierdMethod(n, q);
                                                
                                // THEN
                                assertThat(actual).isNotNull();
                            }
                                                
                            @Test
                            // q + "" == "0"
                            void testTernaryElseConditionInWierdMethod() {
                                // GIVEN
                                int n = 42;
                                Point q = mock(Point.class);
                                                
                                // WHEN
                                var actual = target.wierdMethod(n, q);
                                                
                                // THEN
                                assertThat(actual).isNotNull();
                            }
                                                
                            @Test
                            // case : throw new IllegalArgumentException(q + " is wierd");
                            void testSwitchCaseConditionInWierdMethod() {
                                // GIVEN
                                int n = 42;
                                Point q = mock(Point.class);
                                                
                                // WHEN
                                var actual = target.wierdMethod(n, q);
                                                
                                // THEN
                                assertThat(actual).isNotNull();
                            }
                                                
                            @Test
                            // case 1: System.out.println("its one");break;
                            void testSwitchCaseConditionInWierdMethod() {
                                // GIVEN
                                int n = 42;
                                Point q = mock(Point.class);
                                                
                                // WHEN
                                var actual = target.wierdMethod(n, q);
                                                
                                // THEN
                                assertThat(actual).isNotNull();
                            }
                                                
                            @Test
                            // case 2: System.out.println("its two");break;
                            void testSwitchCaseConditionInWierdMethod() {
                                // GIVEN
                                int n = 42;
                                Point q = mock(Point.class);
                                                
                                // WHEN
                                var actual = target.wierdMethod(n, q);
                                                
                                // THEN
                                assertThat(actual).isNotNull();
                            }
                                                
                            @Test
                            // case 3: System.out.println("its three");break;
                            void testSwitchCaseConditionInWierdMethod() {
                                // GIVEN
                                int n = 42;
                                Point q = mock(Point.class);
                                                
                                // WHEN
                                var actual = target.wierdMethod(n, q);
                                                
                                // THEN
                                assertThat(actual).isNotNull();
                            }
                                                
                            @Test
                            // case 42: System.out.println("its three");break;
                            void testSwitchCaseConditionInWierdMethod() {
                                // GIVEN
                                int n = 42;
                                Point q = mock(Point.class);
                                                
                                // WHEN
                                var actual = target.wierdMethod(n, q);
                                                
                                // THEN
                                assertThat(actual).isNotNull();
                            }
                                                
                        }
                        """);
    }
}