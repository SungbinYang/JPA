package me.sungbin.hellojpa;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * packageName : me.sungbin.hellojpa
 * fileName : Album
 * author : rovert
 * date : 2022/04/16
 * description :
 * ===========================================================
 * DATE 			AUTHOR			 NOTE
 * -----------------------------------------------------------
 * 2022/04/16       rovert         최초 생성
 */

@Entity
@DiscriminatorValue("A")
public class Album extends Item {

    private String artist;

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
