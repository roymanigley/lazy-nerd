package ch.bytecrowd.lazynerd;

import ch.bytecrowd.lazynerd.model.Book;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateFillerTest {


    public static void main(String[] args) {
        System.out.println(EntityFieldStepper.generateEntityBoilerPlate(Book.class));
    }
    public static final TemplateFiller TEMPLATE_FILLER = new TemplateFiller();

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
                               
                                public class BookService {
                               
                                    public Multi<Book> getAll();
                               
                                    public Uni<Book> findById(UUID id);
                               
                                    public Uni<Book> save(Book book);
                               
                                    public Uni<Boolean> delete(UUID id);
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
                                            return Uni.createFrom().item(() ->
                                                    Response
                                                        .status(Response.Status.BAD_REQUEST)
                                                        .entity("ResourceExistsAlready")
                                                        .build()
                                            );
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
                                            return Uni.createFrom().item(() ->
                                                    Response
                                                        .status(Response.Status.BAD_REQUEST)
                                                        .entity("ResourceDoesNotExistsAlready")
                                                        .build()
                                            );
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
}