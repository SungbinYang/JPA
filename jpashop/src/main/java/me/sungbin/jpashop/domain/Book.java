package me.sungbin.jpashop.domain;

import javax.persistence.Entity;

/**
 * packageName : me.sungbin.jpashop.domain
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
public class Book extends Item {

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
