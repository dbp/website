import tester.*;

/*
  +---------------+
  | Book          | 
  +---------------+
  | String title  |
  | Author author |--+
  | int price     |  |
  +---------------+  |
                     v
              +-------------+
              | Author      |
              +-------------+
              | String name |
              | int yob     |
              +-------------+
 */


// to represent a book in a bookstore
class Book {
    String title;
    Author author;
    int price;
    
    // the constructor
    Book(String title, Author author, int price) {
        this.title = title;
        this.author = author;
        this.price = price;
    }
    
    /* TEMPLATE:
       Fields:
       ... this.title ...         -- String
       ... this.author ...        -- Author
       ... this.price ...         -- int
       
       Methods:
       ... this.salePrice(int) ...   -- int
       ... this.sameAuthor(Book) ... -- boolean
       
       Methods for fields:
       ... this.author.sameAuthor(Author) ...    -- boolean
     */
    
    
    // Compute the sale price of this Book given using 
    // the given discount rate (as a percentage)
    int salePrice(int discount) {
        return this.price - (this.price * discount) / 100;
    }
    //is this book written by the same author as the given book?
    boolean sameAuthor(Book that) {
        return this.author.sameAuthor(that.author);
    }
}

//to represent a author of a book in a bookstore
class Author {
    String name;
    int yob;
    
    // the constructor
    Author(String name, int yob) {
        this.name = name;
        this.yob = yob;
    }
    
    /* TEMPLATE
     Fields:
     ... this.name ...    -- String
     ... this.yob ...     -- int
     
     Methods:
     ... this.sameAuthor(Author) ... -- boolean
     */
    
    // is this the same author as the given one?
    boolean sameAuthor(Author that) {
        return this.name.equals(that.name)
            && this.yob == that.yob;
    }
}

// examples and tests for the classes that represent
// books and authors
class ExamplesBooks {
    ExamplesBooks() {}
    
    // examples of authors
    Author pat = new Author("Pat Conroy", 1948);
    Author dan = new Author("Dan Brown", 1962);
    
    // examples of books
    Book beaches = new Book("Beaches", this.pat, 20);
    Book prince = new Book("Prince of Tides", this.pat, 15);
    Book code = new Book("Da Vinci Code", this.dan, 20);
    
    // test the method sameAuthor in the class Book
    boolean testSameBookAuthor(Tester t) {
        return t.checkExpect(this.beaches.sameAuthor(this.prince), true)
            && t.checkExpect(this.beaches.sameAuthor(this.code), false);
    }
    
    // test the method sameAuthor in the class Author
    boolean testSameAuthor(Tester t) {
        return t.checkExpect(this.pat.sameAuthor(new Author("Pat Conroy", 1948)), true)
            && t.checkExpect(this.pat.sameAuthor(this.dan), false);
    }
}
