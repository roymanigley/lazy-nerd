package ch.bytecrowd.lazynerd;

import ch.bytecrowd.lazynerd.model.Book;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class ReflectionHelperTest {

    @Test
    void testGetIdType() {
        Assertions.assertThat(
                ReflectionHelper.getIdType(Book.class).get()
        ).isEqualTo(UUID.class);

        Assertions.assertThat(
                ReflectionHelper.getIdType(Object.class)
        ).isNotPresent();
    }

    @Test
    void testGetParentPackageName() {
        Assertions.assertThat(
                ReflectionHelper.getParentPackageName(Book.class)
        ).isEqualTo("ch.bytecrowd.lazynerd");
    }


    @Test
    void testIsAnnotatedWithId() throws NoSuchFieldException {
        Assertions.assertThat(
                ReflectionHelper.isAnnotatedWithId(Book.class.getDeclaredField("id"))
        ).isTrue();

        Assertions.assertThat(
                ReflectionHelper.isAnnotatedWithId(Book.class.getDeclaredField("title"))
        ).isFalse();
    }

    @Test
    void testIsAnnotatedWithOneToMany() throws NoSuchFieldException {
        Assertions.assertThat(
                ReflectionHelper.isAnnotatedWithOneToMany(Book.class.getDeclaredField("authors"))
        ).isTrue();

        Assertions.assertThat(
                ReflectionHelper.isAnnotatedWithOneToMany(Book.class.getDeclaredField("category"))
        ).isFalse();
    }

    @Test
    void testIsAnnotatedWithManyToOne() throws NoSuchFieldException {
        Assertions.assertThat(
                ReflectionHelper.isAnnotatedWithManyToOne(Book.class.getDeclaredField("category"))
        ).isTrue();

        Assertions.assertThat(
                ReflectionHelper.isAnnotatedWithManyToOne(Book.class.getDeclaredField("authors"))
        ).isFalse();
    }
}