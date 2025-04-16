import tester.Tester;
/**
 * HtDC Lectures
 * Lecture 2: Designing Simple Classes: Books and Authors
 * 
 * Copyright 2013 Viera K. Proulx
 * This program is distributed under the terms of the 
 * GNU Lesser General Public License (LGPL)
 * 
 * @since 29 August 2013
 */

/*
We want to represent an book with an author as data in DrRacket
and then in Java.

Here is the data definition in DrRacket:

;; to represent a book in a bookstore
;; A Book is (make-book String Author Number)
(define-struct book (title author price))

;; to represent an author of a book (with the year of birth)
;; An Author is (make-author String Number)
(define-struct author (name yob))

;; Examples:
(define pat (make-author "Pat Conroy" 1948))
(define beaches (make-book "Beaches" pat 20))


We have seen that all this information can be represented concisely 
as s class diagram like this:

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

We discussed the difference between exact and inexact numbers
and the fact that in Java we have to specify the kind of numbers
we intend to use. int stands for Integer.

We record the book price in whole dollars only (for convenience).

The code below shows how this class diagram and our examples can be
translated into a representation as Java classes
(a Java class hierarchy):
*/

// to represent a book in a bookstore
class Book{
  String title;
  Author author;
  int price;

  // the constructor
  Book(String title, Author author, int price){
    this.title = title;
    this.author = author;
    this.price = price;
  }
}

// to represent a author of a book in a bookstore
class Author{
  String name;
  int yob;

  // the constructor
  Author(String name, int yob){
    this.name = name;
    this.yob = yob;
  }
}

// examples and tests for the class hierarchy that represents 
// books and authors
class ExamplesBooks{
  ExamplesBooks(){}
  
  Author pat = new Author("Pat Conroy", 1948);
  Book beaches = new Book("Beaches", this.pat, 20);
}
