package ch.bytecrowd.lazynerd;

import ch.bytecrowd.lazynerd.model.Book;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParamProviderTest {

    @Test
    void test() {
        var params = ParamProvider.paramsFromEntity(Book.class);

        assertThat(params).hasSize(7);
        assertThat(params.get("entityRestResourceName")).isEqualTo("book");
        assertThat(params.get("basePackage")).isEqualTo("ch.bytecrowd.lazynerd");
        assertThat(params.get("entityTypeSimpleName")).isEqualTo("Book");
        assertThat(params.get("entityTypeVariableName")).isEqualTo("book");
        assertThat(params.get("idTypeCanonicalName")).isEqualTo("java.util.UUID");
        assertThat(params.get("idTypeSimpleName")).isEqualTo("UUID");
        assertThat(params.get("entityTypeCanonicalName")).isEqualTo("ch.bytecrowd.lazynerd.model.Book");
    }

}