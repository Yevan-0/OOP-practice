package edu.practice.d01.model;

// TODO d01-1: make Book a kind of LibraryItem (use extends).
//             Once you do, Java will insist you also write loanDays() (TODO d01-5).
public class Book extends LibraryItem {

  // TODO d01-2: two fields that can never change after construction:
  //             author (String) and pages (int). Keep them private.
  private final String author;
  private final int pages;

  public Book(long id, String title, String author, int pages) {
    // TODO d01-3: the FIRST line must hand id and title to the parent constructor.
    //             Then validate: author not null/blank (store it trimmed), pages > 0.
    //             Bad values -> throw new IllegalArgumentException("...").
    super(id, title);
    if (author == null || author.isBlank()) {
      throw new IllegalArgumentException("Author cannot be left blank");
    }
    if (pages <= 0) {
      throw new IllegalArgumentException("Cannot contain bad pages");
    }

    this.author = author.trim();
    this.pages = pages;

  }

  // TODO d01-4: public getters getAuthor() and getPages().

  public String getAuthor() {
    return author;
  }

  public int getPages() {
    return pages;
  }

  // TODO d01-5: loanDays(): books over 500 pages may be borrowed for 21 days, all others 14.
  @Override
  public int loanDays() {
    if (pages > 500) {
      return 21;
    } else {
      return 14;
    }
  }

}
