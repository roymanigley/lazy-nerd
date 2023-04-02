package ch.bytecrowd.lazynerd;

public class Templates {

    public static final String GETTER_SETTER_EQUALS_AND_HASH_CODE = """
            #forEach(entityTypeFields)
                public ${fieldType} get${fieldNamePascalCase}() {
                    return ${fieldName};
                }
                
                public void set${fieldNamePascalCase}(${fieldType} ${fieldName}) {
                    this.${fieldName} = ${fieldName};
                }
                
                public ${entityTypeSimpleName} ${fieldName}(${fieldType} ${fieldName}) {
                    set${fieldNamePascalCase}(${fieldName});
                    return this;
                }
                
            #end
                @Override
                public boolean equals(Object o) {
                    if (this == o) return true;
                    if (o == null || getClass() != o.getClass()) return false;
                    ${entityTypeSimpleName} entity = (${entityTypeSimpleName}) o;
                    return Objects.equals(id, entity.id);
                }
                            
                /**
                https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
                */
                @Override
                public int hashCode() {
                    return getClass().hashCode();
                }
            """;
    public static final String QUARKUS_REPOSITORY= """
            package ${basePackage}.repository;
                            
            import ${entityTypeCanonicalName};
            import ${idTypeCanonicalName};
            import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
            import javax.inject.Singleton;
                            
            @Singleton
            public class ${entityTypeSimpleName}Repository implements PanacheRepositoryBase<${entityTypeSimpleName}, ${idTypeSimpleName}> {
            }
            """;
    public static final String QUARKUS_SERVICE= """
                package ${basePackage}.service;
                
                import ${entityTypeCanonicalName};
                import ${idTypeCanonicalName};
                import java.util.Optional;
                import io.smallrye.mutiny.Multi;
                import io.smallrye.mutiny.Uni;
                
                public interface ${entityTypeSimpleName}Service {
                    
                    Multi<${entityTypeSimpleName}> getAll();
                                  
                    Uni<Optional<${entityTypeSimpleName}>> findById(${idTypeSimpleName} id);
                                  
                    Uni<${entityTypeSimpleName}> save(${entityTypeSimpleName} ${entityTypeVariableName});
                                  
                    Uni<Boolean> delete(${idTypeSimpleName} id);
                }
                """;

    public static final String QUARKUS_SERVICE_IMPL= """
            package ${basePackage}.service.impl;
                            
            import ${entityTypeCanonicalName};
            import ${idTypeCanonicalName};
            import java.util.Optional;
            import ${basePackage}.repository.${entityTypeSimpleName}Repository;
            import ${basePackage}.service.${entityTypeSimpleName}Service;
            import io.quarkus.hibernate.reactive.panache.Panache;
            import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
            import io.smallrye.mutiny.Multi;
            import io.smallrye.mutiny.Uni;
            import javax.inject.Singleton;
                            
            @Singleton
            public class ${entityTypeSimpleName}ServiceImpl implements ${entityTypeSimpleName}Service {
                
                private final ${entityTypeSimpleName}Repository repository;
                
                public ${entityTypeSimpleName}ServiceImpl(${entityTypeSimpleName}Repository repository) {
                    this.repository = repository;
                }
                
                @Override
                public Multi<${entityTypeSimpleName}> getAll() {
                    return repository.streamAll();
                }
                              
                @Override
                public Uni<Optional<${entityTypeSimpleName}>> findById(${idTypeSimpleName} id) {
                    return repository.findById(id).map(Optional::ofNullable);
                }
                              
                @Override
                @ReactiveTransactional
                public Uni<${entityTypeSimpleName}> save(${entityTypeSimpleName} ${entityTypeVariableName}) {
                    if (${entityTypeVariableName}.getId() != null) {
                        return repository.getSession()
                                .chain(session -> session.merge(${entityTypeVariableName}));
                    }
                    return repository.persist(${entityTypeVariableName});
                }
                              
                @Override
                @ReactiveTransactional
                public Uni<Boolean> delete(${idTypeSimpleName} id) {
                    return repository.deleteById(id);
                }
            }
            """;
    public static final String QUARKUS_REST_RESOURCE = """
            package ${basePackage}.web.rest;
                        
            import ${entityTypeCanonicalName};
            import ${idTypeCanonicalName};
            import ${basePackage}.service.${entityTypeSimpleName}Service;
            import io.smallrye.mutiny.Multi;
            import io.smallrye.mutiny.Uni;
                        
            import javax.ws.rs.*;
            import javax.ws.rs.core.Response;
            import java.net.URI;
            import javax.inject.Singleton;
            
            @Singleton
            @Path("/api/${entityRestResourceName}")
            public class ${entityTypeSimpleName}Resource {
                        
                private final ${entityTypeSimpleName}Service service;
                
                public ${entityTypeSimpleName}Resource(${entityTypeSimpleName}Service service) {
                    this.service = service;
                }
                
                @GET
                public Multi<${entityTypeSimpleName}> findAll() {
                    return service.getAll();
                }
                        
                @GET
                @Path("/{id}")
                public Uni<Response> findById(${idTypeSimpleName} id) {
                    return service.findById(id)
                            .map(optional -> optional
                                .map(${entityTypeVariableName} -> Response
                                    .ok(${entityTypeVariableName})
                                    .build()
                                ).orElseGet(() -> Response
                                        .status(Response.Status.NOT_FOUND)
                                        .build()
                                )
                            );
                }
                        
                @POST
                public Uni<Response> create(${entityTypeSimpleName} ${entityTypeVariableName}) {
                    if (${entityTypeVariableName}.getId() != null) {
                        throw new WebApplicationException("error.badrequest.alreadyexist", Response.Status.BAD_REQUEST);
                    } else {
                        return service.save(${entityTypeVariableName})
                                .map(d -> Response
                                        .created(URI.create("/api/${entityRestResourceName}/" + d.getId()))
                                        .entity(d)
                                        .build()
                                );
                    }
                }
                        
                @PUT
                public Uni<Response> update(${entityTypeSimpleName} ${entityTypeVariableName}) {
                    if (${entityTypeVariableName}.getId() == null) {
                        throw new WebApplicationException("error.badrequest.notexist", Response.Status.BAD_REQUEST);
                    } else {
                        return service.save(${entityTypeVariableName})
                                .map(d -> Response
                                        .ok(d)
                                        .build()
                                );
                    }
                }
                        
                @DELETE
                @Path("/{id}")
                public Uni<Response> delete(${idTypeSimpleName} id) {
                    return service.delete(id)
                            .map(aBoolean -> Response
                                    .accepted()
                                    .build()
                            );
                }
            }
            """;

    public static final String QUARKUS_REST_RESOURCE_IT = """
            package ${basePackage}.web.rest;
            
            import ${entityTypeCanonicalName};
            import ${idTypeCanonicalName};
            import ${basePackage}.repository.${entityTypeSimpleName}Repository;
            
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
            public class ${entityTypeSimpleName}ResourceIT {
            
                // TODO: set default values and remove default value for ID
                #forEach(entityTypeFields)
                public static final ${fieldType} DEFAULT_${fieldNameUpperCase} = "AAAA";
                #end
            
                // TODO: set updated values and remove updated value for ID
                #forEach(entityTypeFields)
                public static final ${fieldType} UPDATED_${fieldNameUpperCase} = "BBBB";
                #end
            
                @Inject
                ${entityTypeSimpleName}Repository repository;
                        
                @Test
                public void testPostWithNotExistingShouldSaveAndReturnRecord() {
                    ${entityTypeSimpleName} ${entityTypeVariableName} = create${entityTypeSimpleName}();
                    given()
                            .body(${entityTypeVariableName})
                            .contentType(ContentType.JSON)
                            .when().post("/api/${entityTypeVariableName}")
                            .then()
                            .statusCode(HttpResponseStatus.CREATED.code())
                            .body("id", notNullValue())
                            // TODO: remove default id matcher
                            #forEach(entityTypeFields)
                            .body("${fieldName}", is(DEFAULT_${fieldNameUpperCase}))
                            #end
                            ;
                }
                        
                @Test
                public void testPostWithExistingShouldBeStatusBadRequest() {
                    ${entityTypeSimpleName} ${entityTypeVariableName} = create${entityTypeSimpleName}AndPersist(repository);
                    given()
                            .body(${entityTypeVariableName})
                            .contentType(ContentType.JSON)
                            .when().post("/api/${entityTypeVariableName}")
                            .then()
                            .statusCode(HttpResponseStatus.BAD_REQUEST.code());
                }
                        
                @Test
                public void testPutWithExistingShouldUpdateEndReturnRecord() {
                    ${entityTypeSimpleName} ${entityTypeVariableName} = create${entityTypeSimpleName}AndPersist(repository);
                    ${entityTypeSimpleName} ${entityTypeVariableName}Updated = create${entityTypeSimpleName}()
                            .id(${entityTypeVariableName}.getId())
                            // TODO: remove setter for default id
                            #forEach(entityTypeFields)
                            .${fieldName}(UPDATED_${fieldNameUpperCase})
                            #end
                            ;
                        
                    given()
                            .body(${entityTypeVariableName}Updated)
                            .contentType(ContentType.JSON)
                            .when().put("/api/${entityTypeVariableName}")
                            .then()
                            .statusCode(HttpResponseStatus.OK.code())
                            .body("id", is(${entityTypeVariableName}.getId().toString()))
                            // TODO: remove default id matcher
                            #forEach(entityTypeFields)
                            .body("${fieldName}", is(UPDATED_${fieldNameUpperCase}))
                            #end
                            ;
                }
                        
                @Test
                public void testPutWithNotExistingShouldStatusBeBadRequest() {
                    ${entityTypeSimpleName} ${entityTypeVariableName} = create${entityTypeSimpleName}();
                        
                    given()
                            .body(${entityTypeVariableName})
                            .contentType(ContentType.JSON)
                            .when().put("/api/${entityTypeVariableName}")
                            .then()
                            .statusCode(HttpResponseStatus.BAD_REQUEST.code());
                }
                        
                @Test
                public void testFindAllShouldReturnAllRecords() {
                    create${entityTypeSimpleName}AndPersist(repository);
                    create${entityTypeSimpleName}AndPersist(repository);
                    create${entityTypeSimpleName}AndPersist(repository);
                    var count = repository.count().await().indefinitely();
                        
                    given()
                            .contentType(ContentType.JSON)
                            .when().get("/api/${entityTypeVariableName}")
                            .then()
                            .statusCode(HttpResponseStatus.OK.code())
                            .body("$.size()", is(count.intValue()));
                }
                        
                @Test
                public void testFindAllWhenEmptyShouldStillReturnOkStatus() {
                    repository.deleteAll().await().indefinitely();
                        
                    given()
                            .contentType(ContentType.JSON)
                            .when().get("/api/${entityTypeVariableName}")
                            .then()
                            .statusCode(HttpResponseStatus.OK.code())
                            .body("$.size()", is(0));
                }
                        
                @Test
                public void testFindOneWithExistingIdShouldReturnCorrectRecord() {
                    ${entityTypeSimpleName} ${entityTypeVariableName} = create${entityTypeSimpleName}AndPersist(repository);
                        
                    given()
                            .contentType(ContentType.JSON)
                            .when().get("/api/${entityTypeVariableName}/" + ${entityTypeVariableName}.getId())
                            .then()
                            .statusCode(HttpResponseStatus.OK.code())
                            .body("id", is(${entityTypeVariableName}.getId().toString()))
                            // TODO: remove default id matcher
                            #forEach(entityTypeFields)
                            .body("${fieldName}", is(DEFAULT_${fieldNameUpperCase}))
                            #end
                            ;
                }
                        
                @Test
                public void testFindOneWithNotExistingShouldBeStatusNotFound() {
                        
                    given()
                            .contentType(ContentType.JSON)
                            .when().get("/api/${entityTypeVariableName}/" + UUID.randomUUID())
                            .then()
                            .statusCode(HttpResponseStatus.NOT_FOUND.code());
                }
                
                /* TODO: since the ${entityTypeVariableName} is managed Entity, the deletion of the record can not be checked.     
                @Test
                public void testDeleteWithExistingRecordShouldDelete() {
                    ${entityTypeSimpleName} ${entityTypeVariableName} = create${entityTypeSimpleName}AndPersist(repository);

                    given()
                            .contentType(ContentType.JSON)
                            .when().delete("/api/${entityTypeVariableName}/" + ${entityTypeVariableName}.getId())
                            .then()
                            .statusCode(HttpResponseStatus.ACCEPTED.code());

                    repository.findById(${entityTypeVariableName}.getId())
                            .invoke(item -> assertThat(item).isNull())
                            .subscribe().withSubscriber(UniAssertSubscriber.create())
                            .assertCompleted();
                }
                */
                        
                @Test
                public void testDeleteWithNonExistingRecordShouldBeStatusAccepted() {
                    given()
                            .contentType(ContentType.JSON)
                            .when().delete("/api/${entityTypeVariableName}/" + UUID.randomUUID())
                            .then()
                            .statusCode(HttpResponseStatus.ACCEPTED.code());
                }
                        
                public static ${entityTypeSimpleName} create${entityTypeSimpleName}AndPersist(${entityTypeSimpleName}Repository repository) {
                    return repository.persistAndFlush(create${entityTypeSimpleName}()).await().indefinitely();
                }
                        
                public static ${entityTypeSimpleName} create${entityTypeSimpleName}() {
                    return new ${entityTypeSimpleName}()
                            // TODO: remove setter for default id
                            #forEach(entityTypeFields)
                            .${fieldName}(DEFAULT_${fieldNameUpperCase})
                            #end;
                }
            }
            """;

    public static String QUARKUS_SERVICE_TEST = """
            package ${basePackage}.service;
            
            import ${entityTypeCanonicalName};
            import ${idTypeCanonicalName};
            import ${basePackage}.repository.${entityTypeSimpleName}Repository;
            import ${basePackage}.service.impl.${entityTypeSimpleName}ServiceImpl;
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
            class ${entityTypeSimpleName}ServiceTest {
                        
                @Mock
                ${entityTypeSimpleName}Repository repository;
                ${entityTypeSimpleName}Service service;
                        
                @BeforeEach
                void init() {
                    service = new ${entityTypeSimpleName}ServiceImpl(repository);
                }
                        
                @Test
                void testFindAll() {
                    var itemsMocked = List.of(
                            mock(${entityTypeSimpleName}.class),
                            mock(${entityTypeSimpleName}.class),
                            mock(${entityTypeSimpleName}.class),
                            mock(${entityTypeSimpleName}.class),
                            mock(${entityTypeSimpleName}.class)
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
                    ${entityTypeSimpleName} itemMock = mock(${entityTypeSimpleName}.class);
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
                    ${entityTypeSimpleName} itemMock = mock(${entityTypeSimpleName}.class);
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
                    ${entityTypeSimpleName} itemMock = mock(${entityTypeSimpleName}.class);
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
            """;
}
