package me.sungbin.hellojpa;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * packageName : me.sungbin.hellojpa
 * fileName : Book
 * author : rovert
 * date : 2022/04/16
 * description :
 * ===========================================================
 * DATE 			AUTHOR			 NOTE
 * -----------------------------------------------------------
 * 2022/04/16       rovert         최초 생성
 */

@Entity
@DiscriminatorValue("B")
public class Book extends Item{

    private String author;

    private String isbn;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}
