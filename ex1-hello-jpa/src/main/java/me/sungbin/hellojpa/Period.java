package me.sungbin.hellojpa;

import javax.persistence.Embeddable;
import java.time.LocalDateTime;

/**
 * packageName : me.sungbin.hellojpa
 * fileName : Period
 * author : rovert
 * date : 2022/04/23
 * description :
 * ===========================================================
 * DATE 			AUTHOR			 NOTE
 * -----------------------------------------------------------
 * 2022/04/23       rovert         최초 생성
 */

@Embeddable
public class Period {

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
