package ch.bytecrowd.lazynerd;

import ch.bytecrowd.lazynerd.model.Book;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.linesOf;

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
    void testTemplateGenerationWithForLoopForEmptyList() {
        Map<String, Object> params = Map.of(
                "emptyList", Collections.emptyList(),
                "title", "MyTitle"
        );


        String template = """
                title: ${title}
                
                #forEach(emptyList)
                name: ${name}
                #end
                """;
        String generated = new TemplateFiller().fillUpTemplate(template, () -> params);
        assertThat(generated).isEqualTo(
                """
                        title: MyTitle
                               
                               
                        """
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
    void testServiceTestGenerationToFile() throws IOException {
        Path pathToGeneratedRepository = generateServiceTestToFile(Book.class);
        String content = Files.readString(pathToGeneratedRepository.resolve(Paths.get("ch/bytecrowd/lazynerd/service/BookServiceTest.java")));
        assertThat(content).contains("package ch.bytecrowd.lazynerd.service;");
        deleteAllfilesInDirectory(pathToGeneratedRepository);
    }

    @Test
    void testRestControllerGenerationToFile() throws IOException {
        Path pathToGeneratedRepository = generateRestResourceTestToFile(Book.class);
        String content = Files.readString(pathToGeneratedRepository.resolve(Paths.get("ch/bytecrowd/lazynerd/web/rest/BookResource.java")));
        assertThat(content).contains("package ch.bytecrowd.lazynerd.web.rest;");
        deleteAllfilesInDirectory(pathToGeneratedRepository);
    }

    private void deleteAllfilesInDirectory(Path pathToGeneratedRepository) throws IOException {
        Files.walk(pathToGeneratedRepository)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    void testRepositoryGenerationToFile() throws IOException {
        Path pathToGeneratedRepository = generateRepositoryToFile(Book.class);
        String content = Files.readString(pathToGeneratedRepository.resolve(Paths.get("ch/bytecrowd/lazynerd/repository/BookRepository.java")));
        deleteAllfilesInDirectory(pathToGeneratedRepository);
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
                                import java.util.Optional;
                                import io.smallrye.mutiny.Multi;
                                import io.smallrye.mutiny.Uni;
                               
                                public interface BookService {
                               
                                    Multi<Book> getAll();
                               
                                    Uni<Optional<Book>> findById(UUID id);
                               
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
                                import java.util.Optional;
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
                                    public Uni<Optional<Book>> findById(UUID id) {
                                        return repository.findById(id).map(Optional::ofNullable);
                                    }
                                                              
                                    @Override
                                    @ReactiveTransactional
                                    public Uni<Book> save(Book book) {
                                        if (book.getId() != null) {
                                            return repository.getSession()
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
                                             .map(optional -> optional
                                                 .map(book -> Response
                                                     .ok(book)
                                                     .build()
                                                 ).orElseGet(() -> Response
                                                         .status(Response.Status.NOT_FOUND)
                                                         .build()
                                                 )
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

    @Test
    void testRestControllerITGeneration() {
        String restControllerIT = generateRestControllerIT(Book.class);
        assertThat(restControllerIT)
                .isEqualTo("""
                        package ch.bytecrowd.lazynerd.web.rest;
                                             
                        import ch.bytecrowd.lazynerd.model.Book;
                        import java.util.UUID;
                        import ch.bytecrowd.lazynerd.repository.BookRepository;
                                             
                        import io.netty.handler.codec.http.HttpResponseStatus;
                        import io.quarkus.test.junit.QuarkusTest;
                        import io.restassured.http.ContentType;
                        import org.junit.jupiter.api.Test;
                        import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
                                             
                        import javax.inject.Inject;
                                             
                        import static io.restassured.RestAssured.given;
                        import static org.assertj.core.api.Assertions.*;
                        import static org.hamcrest.CoreMatchers.is;
                        import static org.hamcrest.CoreMatchers.notNullValue;
                                             
                        @QuarkusTest
                        public class BookResourceIT {
                                             
                            // TODO: set default values and remove default value for ID
                                public static final UUID DEFAULT_ID = "AAAA";
                                public static final String DEFAULT_TITLE = "AAAA";
                                public static final List<Author> DEFAULT_AUTHORS = "AAAA";
                                public static final Category DEFAULT_CATEGORY = "AAAA";
                           \s
                            // TODO: set updated values and remove updated value for ID
                                public static final UUID UPDATED_ID = "BBBB";
                                public static final String UPDATED_TITLE = "BBBB";
                                public static final List<Author> UPDATED_AUTHORS = "BBBB";
                                public static final Category UPDATED_CATEGORY = "BBBB";
                           \s
                            @Inject
                            BookRepository repository;
                                             
                            @Test
                            public void testPostWithNotExistingShouldSaveAndReturnRecord() {
                                Book book = createBook();
                                given()
                                        .body(book)
                                        .contentType(ContentType.JSON)
                                        .when().post("/api/book")
                                        .then()
                                        .statusCode(HttpResponseStatus.CREATED.code())
                                        .body("id", notNullValue())
                                        // TODO: remove default id matcher
                                                        .body("id", is(DEFAULT_ID))
                                                        .body("title", is(DEFAULT_TITLE))
                                                        .body("authors", is(DEFAULT_AUTHORS))
                                                        .body("category", is(DEFAULT_CATEGORY))
                                                        ;
                            }
                                             
                            @Test
                            public void testPostWithExistingShouldBeStatusBadRequest() {
                                Book book = createBookAndPersist(repository);
                                given()
                                        .body(book)
                                        .contentType(ContentType.JSON)
                                        .when().post("/api/book")
                                        .then()
                                        .statusCode(HttpResponseStatus.BAD_REQUEST.code());
                            }
                                             
                            @Test
                            public void testPutWithExistingShouldUpdateEndReturnRecord() {
                                Book book = createBookAndPersist(repository);
                                Book bookUpdated = createBook()
                                        .id(book.getId())
                                        // TODO: remove setter for default id
                                                        .id(UPDATED_ID)
                                                        .title(UPDATED_TITLE)
                                                        .authors(UPDATED_AUTHORS)
                                                        .category(UPDATED_CATEGORY)
                                                        ;
                                             
                                given()
                                        .body(bookUpdated)
                                        .contentType(ContentType.JSON)
                                        .when().put("/api/book")
                                        .then()
                                        .statusCode(HttpResponseStatus.OK.code())
                                        .body("id", is(book.getId().toString()))
                                        // TODO: remove default id matcher
                                                        .body("id", is(UPDATED_ID))
                                                        .body("title", is(UPDATED_TITLE))
                                                        .body("authors", is(UPDATED_AUTHORS))
                                                        .body("category", is(UPDATED_CATEGORY))
                                                        ;
                            }
                                             
                            @Test
                            public void testPutWithNotExistingShouldStatusBeBadRequest() {
                                Book book = createBook();
                                             
                                given()
                                        .body(book)
                                        .contentType(ContentType.JSON)
                                        .when().put("/api/book")
                                        .then()
                                        .statusCode(HttpResponseStatus.BAD_REQUEST.code());
                            }
                                             
                            @Test
                            public void testFindAllShouldReturnAllRecords() {
                                createBookAndPersist(repository);
                                createBookAndPersist(repository);
                                createBookAndPersist(repository);
                                var count = repository.count().await().indefinitely();
                                             
                                given()
                                        .contentType(ContentType.JSON)
                                        .when().get("/api/book")
                                        .then()
                                        .statusCode(HttpResponseStatus.OK.code())
                                        .body("$.size()", is(count.intValue()));
                            }
                                             
                            @Test
                            public void testFindAllWhenEmptyShouldStillReturnOkStatus() {
                                repository.deleteAll().await().indefinitely();
                                             
                                given()
                                        .contentType(ContentType.JSON)
                                        .when().get("/api/book")
                                        .then()
                                        .statusCode(HttpResponseStatus.OK.code())
                                        .body("$.size()", is(0));
                            }
                                             
                            @Test
                            public void testFindOneWithExistingIdShouldReturnCorrectRecord() {
                                Book book = createBookAndPersist(repository);
                                             
                                given()
                                        .contentType(ContentType.JSON)
                                        .when().get("/api/book/" + book.getId())
                                        .then()
                                        .statusCode(HttpResponseStatus.OK.code())
                                        .body("id", is(book.getId().toString()))
                                        // TODO: remove default id matcher
                                                        .body("id", is(DEFAULT_ID))
                                                        .body("title", is(DEFAULT_TITLE))
                                                        .body("authors", is(DEFAULT_AUTHORS))
                                                        .body("category", is(DEFAULT_CATEGORY))
                                                        ;
                            }
                                             
                            @Test
                            public void testFindOneWithNotExistingShouldBeStatusNotFound() {
                                             
                                given()
                                        .contentType(ContentType.JSON)
                                        .when().get("/api/book/" + UUID.randomUUID())
                                        .then()
                                        .statusCode(HttpResponseStatus.NOT_FOUND.code());
                            }
                            
                            /* TODO: since the book is managed Entity, the deletion of the record can not be checked.
                            @Test
                            public void testDeleteWithExistingRecordShouldDelete() {
                                Book book = createBookAndPersist(repository);
                     
                                given()
                                        .contentType(ContentType.JSON)
                                        .when().delete("/api/book/" + book.getId())
                                        .then()
                                        .statusCode(HttpResponseStatus.ACCEPTED.code());
                     
                                repository.findById(book.getId())
                                        .invoke(item -> assertThat(item).isNull())
                                        .subscribe().withSubscriber(UniAssertSubscriber.create())
                                        .assertCompleted();
                            }
                            */
                                             
                            @Test
                            public void testDeleteWithNonExistingRecordShouldBeStatusAccepted() {
                                given()
                                        .contentType(ContentType.JSON)
                                        .when().delete("/api/book/" + UUID.randomUUID())
                                        .then()
                                        .statusCode(HttpResponseStatus.ACCEPTED.code());
                            }
                                             
                            public static Book createBookAndPersist(BookRepository repository) {
                                return repository.persistAndFlush(createBook()).await().indefinitely();
                            }
                                             
                            public static Book createBook() {
                                return new Book()
                                        // TODO: remove setter for default id
                                                        .id(DEFAULT_ID)
                                                        .title(DEFAULT_TITLE)
                                                        .authors(DEFAULT_AUTHORS)
                                                        .category(DEFAULT_CATEGORY)
                                        ;
                            }
                        }
                        """);
    }


    @Test
    void testServiceTestGeneration() {
        String serviceTest = generateServiceTest(Book.class);
        assertThat(serviceTest)
                .isEqualTo("""
                     package ch.bytecrowd.lazynerd.service;
                     
                     import ch.bytecrowd.lazynerd.model.Book;
                     import java.util.UUID;
                     import ch.bytecrowd.lazynerd.repository.BookRepository;
                     import ch.bytecrowd.lazynerd.service.impl.BookServiceImpl;
                     import io.smallrye.mutiny.Multi;
                     import io.smallrye.mutiny.Uni;
                     import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
                     import org.hibernate.reactive.mutiny.Mutiny;
                     import org.junit.jupiter.api.BeforeEach;
                     import org.junit.jupiter.api.Test;
                     import org.junit.jupiter.api.extension.ExtendWith;
                     import org.mockito.Mock;
                     import org.mockito.junit.jupiter.MockitoExtension;
                     
                     import java.util.List;
                     import java.util.Optional;
                     import java.util.UUID;
                     
                     import static org.assertj.core.api.Assertions.*;
                     import static org.mockito.Mockito.*;
                     
                     
                     @ExtendWith(MockitoExtension.class)
                     class BookServiceTest {
                     
                         @Mock
                         BookRepository repository;
                         BookService service;
                     
                         @BeforeEach
                         void init() {
                             service = new BookServiceImpl(repository);
                         }
                     
                         @Test
                         void testFindAll() {
                             var itemsMocked = List.of(
                                     mock(Book.class),
                                     mock(Book.class),
                                     mock(Book.class),
                                     mock(Book.class),
                                     mock(Book.class)
                             );
                     
                             when(repository.streamAll()).thenReturn(
                                     Multi.createFrom()
                                             .items(
                                                     itemsMocked.stream()
                                             )
                             );
                     
                             service.getAll()
                                     .collect().asList()
                                     .invoke(items -> assertThat(items).hasSize(itemsMocked.size()))
                                     .subscribe().withSubscriber(UniAssertSubscriber.create())
                                     .assertCompleted();
                     
                             verify(repository).streamAll();
                             verifyNoMoreInteractions(repository);
                         }
                     
                         @Test
                         void testFindOne() {
                             UUID id = UUID.randomUUID();
                             Book itemMock = mock(Book.class);
                             when(itemMock.getId()).thenReturn(id);
                             when(repository.findById(id)).thenReturn(
                                     Uni.createFrom()
                                             .item(itemMock)
                             );
                     
                             service.findById(id)
                                     .invoke(item -> assertThat(item).isPresent())
                                     .map(Optional::get)
                                     .invoke(item -> assertThat(item.getId()).isEqualTo(id))
                                     .subscribe().withSubscriber(UniAssertSubscriber.create())
                                     .assertCompleted();
                     
                             verify(repository).findById(id);
                             verifyNoMoreInteractions(repository);
                         }
                     
                     
                         @Test
                         void testSaveNew() {
                             Book itemMock = mock(Book.class);
                             when(repository.persist(itemMock)).thenReturn(
                                     Uni.createFrom()
                                             .item(itemMock)
                             );
                     
                             service.save(itemMock)
                                     .invoke(item -> assertThat(item).isEqualTo(itemMock))
                                     .subscribe().withSubscriber(UniAssertSubscriber.create())
                                     .assertCompleted();
                     
                             verify(repository).persist(itemMock);
                             verifyNoMoreInteractions(repository);
                         }
                     
                         @Test
                         void testSaveExisting() {
                             UUID id = UUID.randomUUID();
                             Book itemMock = mock(Book.class);
                             Mutiny.Session sessionMock = mock(Mutiny.Session.class);
                     
                             when(itemMock.getId()).thenReturn(id);
                             when(sessionMock.merge(itemMock))
                                     .thenReturn(Uni.createFrom().item(itemMock));
                             when(repository.getSession()).thenReturn(
                                     Uni.createFrom().item(sessionMock)
                             );
                     
                             service.save(itemMock)
                                     .invoke(item -> assertThat(item.getId()).isEqualTo(id))
                                     .subscribe().withSubscriber(UniAssertSubscriber.create())
                                     .assertCompleted();
                     
                             verify(repository).getSession();
                             verify(sessionMock).merge(itemMock);
                             verifyNoMoreInteractions(repository);
                             verifyNoMoreInteractions(sessionMock);
                         }
                     
                         @Test
                         void testDelete() {
                             UUID id = UUID.randomUUID();
                             when(repository.deleteById(id)).thenReturn(
                                     Uni.createFrom()
                                             .item(true)
                             );
                     
                             service.delete(id)
                                     .subscribe().withSubscriber(UniAssertSubscriber.create())
                                     .assertCompleted();
                     
                             verify(repository).deleteById(id);
                             verifyNoMoreInteractions(repository);
                         }
                     }
                     """);
    }

    public static String generateRepository(Class clazz) {
        return TEMPLATE_FILLER.fillUpTemplate(
                Templates.QUARKUS_REPOSITORY,
                () -> ParamProvider.paramsFromEntity(clazz)
        );
    }

    public static Path generateRepositoryToFile(Class clazz) throws IOException {
        Path tempFile = Files.createTempDirectory("generateRepositoryToFileTest");

        TEMPLATE_FILLER.fillUpTemplateAndWriteToFile(
                tempFile.toString(),
                Templates.QUARKUS_REPOSITORY,
                () -> ParamProvider.paramsFromEntity(clazz)
        );
        return tempFile;
    }

    public static Path generateServiceTestToFile(Class clazz) throws IOException {
        Path tempFile = Files.createTempDirectory("generateServiceToFileTest");

        TEMPLATE_FILLER.fillUpTemplateAndWriteToFile(
                tempFile.toString(),
                Templates.QUARKUS_SERVICE_TEST,
                () -> ParamProvider.paramsFromEntity(clazz)
        );
        return tempFile;
    }

    public static Path generateRestResourceTestToFile(Class clazz) throws IOException {
        Path tempFile = Files.createTempDirectory("generateResourceToFileTest");

        TEMPLATE_FILLER.fillUpTemplateAndWriteToFile(
                tempFile.toString(),
                Templates.QUARKUS_REST_RESOURCE,
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

    public static String generateRestControllerIT(Class clazz) {
        return TEMPLATE_FILLER.fillUpTemplate(
                Templates.QUARKUS_REST_RESOURCE_IT,
                () -> ParamProvider.paramsFromEntity(clazz)
        );
    }

    public static String generateServiceTest(Class clazz) {
        return TEMPLATE_FILLER.fillUpTemplate(
                Templates.QUARKUS_SERVICE_TEST,
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