    ██       █████  ███████ ██    ██     ███    ██ ███████ ██████  ██████  
    ██      ██   ██    ███   ██  ██      ████   ██ ██      ██   ██ ██   ██
    ██      ███████   ███     ████       ██ ██  ██ █████   ██████  ██   ██
    ██      ██   ██  ███       ██        ██  ██ ██ ██      ██   ██ ██   ██
    ███████ ██   ██ ███████    ██        ██   ████ ███████ ██   ██ ██████   🤓

# Lazy Nerd
> Include this library in to your maven project and generate boilerplate code according to a domain class

The template which is passed to the `TemplateFiller.fillUpTemplate()` method is a `String` and the placeholders have to be declared like this: `${MY_PLACEHOLDER}` the second parameter is an optional interface which has to return a `Map<String, String>` for example `() -> Map.of("MY_PLACEHOLDER", "Placeholder Value")`  

## Build

    mvn clean install

## Usage

1. add the dependency to your `pom.xml`  
```xml
<dependency>
    <groupId>ch.bytecrowd</groupId>
    <artifactId>lazy-nerd</artifactId>
    <version>1.0.0</version>
</dependency>
```
2. create an Entity class (only add the fields, boilerplate will be generated)
```java
package ch.bytecrowd.lazynerd.model;

import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.UUID;

public class Book {

    @Id
    private UUID id;

    private String title;

    @OneToMany
    private List<Author> authors;

    @ManyToOne
    private Category category;
}
```
3. generate CRUD boilerplate for your entity class (`Repository`, `Service`, `ServiceImpl`, `RestController`)
```java
public static void main(String[] args){
        var clazz = Book.class;
        var filler = new TemplateFiller();
        // Generate as String
        var generatedGetterSetterEqualsAndHashCode = filler.fillUpTemplate(
            Templates.GETTER_SETTER_EQUALS_AND_HASH_CODE,
            () -> ParamProvider.paramsFromEntity(clazz)
        );

        var generatedRepopsitory = filler.fillUpTemplate(
            Templates.QUARKUS_REPOSITORY,
            () -> ParamProvider.paramsFromEntity(clazz)
        );

        var generatedService = filler.fillUpTemplate(
            Templates.QUARKUS_SERVICE,
            () -> ParamProvider.paramsFromEntity(clazz)
        );

        var generatedServiceImpl = filler.fillUpTemplate(
            Templates.QUARKUS_SERVICE_IMPL,
            () -> ParamProvider.paramsFromEntity(clazz)
        );

        var generatedRestController = filler.fillUpTemplate(
            Templates.QUARKUS_REST_RESOURCE,
            () -> ParamProvider.paramsFromEntity(clazz)
        );
        
        // Generate to file
        var baseDir = "src/main/java"

        filler.fillUpTemplateAndWriteToFile(
            baseDir,
            Templates.GETTER_SETTER_EQUALS_AND_HASH_CODE,
            () -> ParamProvider.paramsFromEntity(clazz)
        );
        
        filler.fillUpTemplateAndWriteToFile(
            baseDir,
            Templates.QUARKUS_REPOSITORY,
            () -> ParamProvider.paramsFromEntity(clazz)
        );

        filler.fillUpTemplateAndWriteToFile(
            baseDir,
            Templates.QUARKUS_SERVICE,
            () -> ParamProvider.paramsFromEntity(clazz)
        );

        filler.fillUpTemplateAndWriteToFile(
            baseDir,
            Templates.QUARKUS_SERVICE_IMPL,
            () -> ParamProvider.paramsFromEntity(clazz)
        );

        filler.fillUpTemplateAndWriteToFile(
            baseDir,
            Templates.QUARKUS_REST_RESOURCE,
            () -> ParamProvider.paramsFromEntity(clazz)
        );
}
```
### Output
```java
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
```

```java
package ch.bytecrowd.lazynerd.repository;

import ch.bytecrowd.lazynerd.model.Book;
import java.util.UUID;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;

public class BookRepository implements PanacheRepositoryBase<Book, UUID> {
}
```
```java
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
```
```java
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
```
```java
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
```

```java
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

    public Book addToAuthors(Author author) {
        authors.add(author);
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
```