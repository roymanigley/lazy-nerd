package ch.bytecrowd.lazynerd;

public class Templates {

    public static final String QUARKUS_REPOSITORY= """
                package ${basePackage}.repository;
                
                import ${entityTypeCanonicalName};
                import ${idTypeCanonicalName};
                import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
                
                public class ${entityTypeSimpleName}Repository implements PanacheRepositoryBase<${entityTypeSimpleName}, ${idTypeSimpleName}> {
                }
                """;
    public static final String QUARKUS_SERVICE= """
                package ${basePackage}.service;
                
                import ${entityTypeCanonicalName};
                import ${idTypeCanonicalName};
                import io.smallrye.mutiny.Multi;
                import io.smallrye.mutiny.Uni;
                
                public class ${entityTypeSimpleName}Service {
                    
                    public Multi<${entityTypeSimpleName}> getAll();
                                  
                    public Uni<${entityTypeSimpleName}> findById(${idTypeSimpleName} id);
                                  
                    public Uni<${entityTypeSimpleName}> save(${entityTypeSimpleName} ${entityTypeVariableName});
                                  
                    public Uni<Boolean> delete(${idTypeSimpleName} id);
                }
                """;

    public static final String QUARKUS_SERVICE_IMPL= """
                package ${basePackage}.service.impl;
                
                import ${entityTypeCanonicalName};
                import ${idTypeCanonicalName};
                import ${basePackage}.repository.${entityTypeSimpleName}Repository;
                import ${basePackage}.service.${entityTypeSimpleName}Service;
                import io.quarkus.hibernate.reactive.panache.Panache;
                import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
                import io.smallrye.mutiny.Multi;
                import io.smallrye.mutiny.Uni;
                
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
                    public Uni<${entityTypeSimpleName}> findById(${idTypeSimpleName} id) {
                        return repository.findById(id);
                    }
                                  
                    @Override
                    @ReactiveTransactional
                    public Uni<${entityTypeSimpleName}> save(${entityTypeSimpleName} ${entityTypeVariableName}) {
                        if (${entityTypeVariableName}.getId() != null) {
                            return Panache.getSession()
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
                            .map(d -> Response
                                    .ok(d)
                                    .build()
                            );
                }
                        
                @POST
                public Uni<Response> create(${entityTypeSimpleName} ${entityTypeVariableName}) {
                    if (${entityTypeVariableName}.getId() != null) {
                        return Uni.createFrom().item(() ->
                                Response
                                    .status(Response.Status.BAD_REQUEST)
                                    .entity("ResourceExistsAlready")
                                    .build()
                        );
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
                        return Uni.createFrom().item(() ->
                                Response
                                    .status(Response.Status.BAD_REQUEST)
                                    .entity("ResourceDoesNotExistsAlready")
                                    .build()
                        );
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
}
