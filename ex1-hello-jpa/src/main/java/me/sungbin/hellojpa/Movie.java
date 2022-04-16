package me.sungbin.hellojpa;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * packageName : me.sungbin.hellojpa
 * fileName : Movie
 * author : rovert
 * date : 2022/04/16
 * description :
 * ===========================================================
 * DATE 			AUTHOR			 NOTE
 * -----------------------------------------------------------
 * 2022/04/16       rovert         최초 생성
 */

@Entity
@DiscriminatorValue("M")
public class Movie extends Item {

    private String director;

    private String actor;

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }
}
