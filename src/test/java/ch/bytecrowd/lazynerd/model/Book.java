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
