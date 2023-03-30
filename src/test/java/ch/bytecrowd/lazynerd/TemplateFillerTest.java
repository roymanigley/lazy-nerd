package ch.bytecrowd.lazynerd;

import ch.bytecrowd.lazynerd.model.Book;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateFillerTest {

    public static final TemplateFiller TEMPLATE_FILLER = new TemplateFiller();

    @Test
    void testTemplateGenerationWithForLoop() {

        var random = new SecureRandom();
        var dummies = List.of(
                new Dummy().name(UUID.randomUUID().toString()).age(random.nextInt()),
                new Dummy().name(UUID.randomUUID().toString()).age(random.nextInt()),
                new Dummy().name(UUID.randomUUID().toString()).age(random.nextInt()),
                new Dummy().name(UUID.randomUUID().toString()).age(random.nextInt())
        );
        Map<String, Object> params = Map.of(
                "dummies",
                dummies,
                "title", "MyTitle",
                "dummy", new Dummy().name(UUID.randomUUID().toString()).age(random.nextInt())
        );


        String template = """
                title: ${title}
                
                #forEach(dummies)
                name: ${name}
                #end
                
                #forEach(dummies)
                age: ${age}
                #end
                """;
        String generated = new TemplateFiller().fillUpTemplate(template, () -> params);
        assertThat(generated).isEqualTo(
                String.format("""
                        title: MyTitle
                                                
                        name: %s
                        name: %s
                        name: %s
                        name: %s
                                                
                        age: %d
                        age: %d
                        age: %d
                        age: %d
                        """,
                        dummies.get(0).getName(),
                        dummies.get(1).getName(),
                        dummies.get(2).getName(),
                        dummies.get(3).getName(),
                        dummies.get(0).getAge(),
                        dummies.get(1).getAge(),
                        dummies.get(2).getAge(),
                        dummies.get(3).getAge())
        );
    }
    @Test
    void testTemplateGenerationWithNestedForLoop() {

        var random = new SecureRandom();
        var dummies = List.of(
                new Dummy().name(UUID.randomUUID().toString()).age(random.nextInt()),
                new Dummy().name(UUID.randomUUID().toString()).age(random.nextInt()),
                new Dummy().name(UUID.randomUUID().toString()).age(random.nextInt()),
                new Dummy().name(UUID.randomUUID().toString()).age(random.nextInt())
        );
        Map<String, Object> params = Map.of(
                "dummies",
                dummies,
                "title", "MyTitle",
                "dummy", new Dummy().name(UUID.randomUUID().toString()).age(random.nextInt())
        );


        String template = """
                #forEach(dummies)
                name: ${name}
                    #forEach(dummies)
                    age: ${age}
                    #end
                #end
                """;

        Assertions.assertThrows(IllegalArgumentException.class, () -> new TemplateFiller().fillUpTemplate(template, () -> params));
    }

    @Test
    void testTemplateGenerationWithForLoopWhenNoCollection() {
        Map<String, Object> params = Map.of(
                "title", "MyTitle"
        );

        String template = """
                #forEach(title)
                #end
                """;

        Assertions.assertThrows(IllegalArgumentException.class, () -> new TemplateFiller().fillUpTemplate(template, () -> params));
    }

    @Test
    void testGetterSetterEqualsAndHashCodeGeneration() {
        String getterSetterEqualsAndHashCode = generateGetterSetterEqualsAndHashCode(Book.class);
        assertThat(getterSetterEqualsAndHashCode)
                .isEqualTo(
                        """
                                    public UUID getId() {
                                        return id;
                                    }
                                                                
                                    public void setId(UUID id) {
                                        this.id = id;
                                    }
                                                                
                                    public Book id(UUID id) {
                                        setId(id);
                                        return this;
                                    }
                                                                
                                    public String getTitle() {
                                        return title;
                                    }
                                                                
                                    public void setTitle(String title) {
                                        this.title = title;
                                    }
                                                                
                                    public Book title(String title) {
                                        setTitle(title);
                                        return this;
                                    }
                                                                
                                    public List<Author> getAuthors() {
                                        return authors;
                                    }
                                                                
                                    public void setAuthors(List<Author> authors) {
                                        this.authors = authors;
                                    }
                                                                
                                    public Book authors(List<Author> authors) {
                                        setAuthors(authors);
                                        return this;
                                    }
                                                                
                                    public Category getCategory() {
                                        return category;
                                    }
                                                                
                                    public void setCategory(Category category) {
                                        this.category = category;
                                    }
                                                                
                                    public Book category(Category category) {
                                        setCategory(category);
                                        return this;
                                    }
                                                                
                                    @Override
                                    public boolean equals(Object o) {
                                        if (this == o) return true;
                                        if (o == null || getClass() != o.getClass()) return false;
                                        Book entity = (Book) o;
                                        return Objects.equals(id, entity.id);
                                    }
                                                                
                                    /**
                                    https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
                                    */
                                    @Override
                                    public int hashCode() {
                                        return getClass().hashCode();
                                    }
                                """
                );
    }

    @Test
    void testRepositoryGeneration() {
        String repository = generateRepository(Book.class);
        assertThat(repository)
                .isEqualTo(
                        """
                                package ch.bytecrowd.lazynerd.repository;
                                                        
                                import ch.bytecrowd.lazynerd.model.Book;
                                import java.util.UUID;
                                import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
                                import javax.inject.Singleton;
                                
                                @Singleton
                                public class BookRepository implements PanacheRepositoryBase<Book, UUID> {
                                }
                                """
                );
    }

    @Test
    void testRepositoryGenerationToFile() throws IOException {
        Path pathToGeneratedRepository = generateRepositoryToFile(Book.class);
        String content = Files.readString(pathToGeneratedRepository);
        Files.deleteIfExists(pathToGeneratedRepository);
        assertThat(content)
                .isEqualTo(
                        """
                                package ch.bytecrowd.lazynerd.repository;
                                                        
                                import ch.bytecrowd.lazynerd.model.Book;
                                import java.util.UUID;
                                import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
                                import javax.inject.Singleton;
                                
                                @Singleton
                                public class BookRepository implements PanacheRepositoryBase<Book, UUID> {
                                }
                                """
                );
    }

    @Test
    void testServiceGeneration() {
        String service = generateService(Book.class);
        assertThat(service)
                .isEqualTo(
                        """
                                package ch.bytecrowd.lazynerd.service;
                               
                                import ch.bytecrowd.lazynerd.model.Book;
                                import java.util.UUID;
                                import io.smallrye.mutiny.Multi;
                                import io.smallrye.mutiny.Uni;
                               
                                public interface BookService {
                               
                                    Multi<Book> getAll();
                               
                                    Uni<Book> findById(UUID id);
                               
                                    Uni<Book> save(Book book);
                               
                                    Uni<Boolean> delete(UUID id);
                                }
                                """
                );
    }

    @Test
    void testServiceImplGeneration() {
        String service = generateServiceImpl(Book.class);
        assertThat(service)
                .isEqualTo(
                        """
                                package ch.bytecrowd.lazynerd.service.impl;
                                                              
                                import ch.bytecrowd.lazynerd.model.Book;
                                import java.util.UUID;
                                import ch.bytecrowd.lazynerd.repository.BookRepository;
                                import ch.bytecrowd.lazynerd.service.BookService;
                                import io.quarkus.hibernate.reactive.panache.Panache;
                                import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
                                import io.smallrye.mutiny.Multi;
                                import io.smallrye.mutiny.Uni;
                                import javax.inject.Singleton;
                                
                                @Singleton
                                public class BookServiceImpl implements BookService {
                                                              
                                    private final BookRepository repository;
                                                              
                                    public BookServiceImpl(BookRepository repository) {
                                        this.repository = repository;
                                    }
                                                              
                                    @Override
                                    public Multi<Book> getAll() {
                                        return repository.streamAll();
                                    }
                                                              
                                    @Override
                                    public Uni<Book> findById(UUID id) {
                                        return repository.findById(id);
                                    }
                                                              
                                    @Override
                                    @ReactiveTransactional
                                    public Uni<Book> save(Book book) {
                                        if (book.getId() != null) {
                                            return Panache.getSession()
                                                    .chain(session -> session.merge(book));
                                        }
                                        return repository.persist(book);
                                    }
                                                              
                                    @Override
                                    @ReactiveTransactional
                                    public Uni<Boolean> delete(UUID id) {
                                        return repository.deleteById(id);
                                    }
                                }
                                 """
                );
    }

    @Test
    void testRestControllerGeneration() {

        String restController = generateRestController(Book.class);
        assertThat(restController)
                .isEqualTo(
                        """
                                package ch.bytecrowd.lazynerd.web.rest;
                                                                
                                import ch.bytecrowd.lazynerd.model.Book;
                                import java.util.UUID;
                                import ch.bytecrowd.lazynerd.service.BookService;
                                import io.smallrye.mutiny.Multi;
                                import io.smallrye.mutiny.Uni;
                                                                
                                import javax.ws.rs.*;
                                import javax.ws.rs.core.Response;
                                import java.net.URI;
                                import javax.inject.Singleton;
                                
                                @Singleton
                                @Path("/api/book")
                                public class BookResource {
                                                                
                                    private final BookService service;
                                                                
                                    public BookResource(BookService service) {
                                        this.service = service;
                                    }
                                                                
                                    @GET
                                    public Multi<Book> findAll() {
                                        return service.getAll();
                                    }
                                                                
                                    @GET
                                    @Path("/{id}")
                                    public Uni<Response> findById(UUID id) {
                                        return service.findById(id)
                                                .map(d -> Response
                                                        .ok(d)
                                                        .build()
                                                );
                                    }
                                                                
                                    @POST
                                    public Uni<Response> create(Book book) {
                                        if (book.getId() != null) {
                                            throw new WebApplicationException("error.badrequest.alreadyexist", Response.Status.BAD_REQUEST);
                                        } else {
                                            return service.save(book)
                                                    .map(d -> Response
                                                            .created(URI.create("/api/book/" + d.getId()))
                                                            .entity(d)
                                                            .build()
                                                    );
                                        }
                                    }
                                                                
                                    @PUT
                                    public Uni<Response> update(Book book) {
                                        if (book.getId() == null) {
                                            throw new WebApplicationException("error.badrequest.notexist", Response.Status.BAD_REQUEST);
                                        } else {
                                            return service.save(book)
                                                    .map(d -> Response
                                                            .ok(d)
                                                            .build()
                                                    );
                                        }
                                    }
                                                                
                                    @DELETE
                                    @Path("/{id}")
                                    public Uni<Response> delete(UUID id) {
                                        return service.delete(id)
                                                .map(aBoolean -> Response
                                                        .accepted()
                                                        .build()
                                                );
                                    }
                                }
                                """
                );
    }
    public static String generateRepository(Class clazz) {
        return TEMPLATE_FILLER.fillUpTemplate(
                Templates.QUARKUS_REPOSITORY,
                () -> ParamProvider.paramsFromEntity(clazz)
        );
    }

    public static Path generateRepositoryToFile(Class clazz) throws IOException {
        Path tempFile = Files.createTempFile("QuarkusRepository", ".java");

        TEMPLATE_FILLER.fillUpTemplateAndWriteToFile(
                tempFile.toString(),
                Templates.QUARKUS_REPOSITORY,
                () -> ParamProvider.paramsFromEntity(clazz)
        );
        return tempFile;
    }

    public static String generateService(Class clazz) {
        return TEMPLATE_FILLER.fillUpTemplate(
                Templates.QUARKUS_SERVICE,
                () -> ParamProvider.paramsFromEntity(clazz)
        );
    }

    public static String generateServiceImpl(Class clazz) {
        return TEMPLATE_FILLER.fillUpTemplate(
                Templates.QUARKUS_SERVICE_IMPL,
                () -> ParamProvider.paramsFromEntity(clazz)
        );
    }

    public static String generateRestController(Class clazz) {
        return TEMPLATE_FILLER.fillUpTemplate(
                Templates.QUARKUS_REST_RESOURCE,
                () -> ParamProvider.paramsFromEntity(clazz)
        );
    }

    public static String generateGetterSetterEqualsAndHashCode(Class clazz) {
        return TEMPLATE_FILLER.fillUpTemplate(
                Templates.GETTER_SETTER_EQUALS_AND_HASH_CODE,
                () -> ParamProvider.paramsFromEntity(clazz)
        );
    }
}

class Dummy {
    private String name;
    private Integer age;
    private List<Integer> numbers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Dummy name(String name) {
        setName(name);
        return this;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Dummy age(Integer age) {
        setAge(age);
        return this;
    }

    public List<Integer> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<Integer> numbers) {
        this.numbers = numbers;
    }

    public Dummy numbers(List<Integer> numbers) {
        setNumbers(numbers);
        return this;
    }

    @Override
    public String toString() {
        return "Dummy{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}