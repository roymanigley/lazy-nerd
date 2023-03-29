package ch.bytecrowd.lazynerd;

import ch.bytecrowd.lazynerd.model.Book;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class EntityFieldStepperTest {

    @Test
    void testEntityGeneration() {
        var clazz = Book.class;
        var generated = EntityFieldStepper.generateEntityBoilerPlate(clazz);
        Assertions.assertThat(generated)
                .isEqualTo("""
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
                            }public UUID getId() {
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
                                
                        """);

    }
}
