package me.sungbin.hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * packageName : me.sungbin.hellojpa
 * fileName : JpaMain
 * author : rovert
 * date : 2022/03/28
 * description :
 * ===========================================================
 * DATE 			AUTHOR			 NOTE
 * -----------------------------------------------------------
 * 2022/03/28       rovert         최초 생성
 */

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("hello");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        try {
            Member member = new Member();
            member.setName("member1");
            entityManager.persist(member);

            // flush -> commit, query

            List<Member> resultList = entityManager.createNativeQuery("select MEMBER_ID, city, street, zipcode, USERNAME from MEMBER", Member.class).getResultList();

            for (Member member1 : resultList) {
                System.out.println("member1 = " + member1);
            }

            transaction.commit(); // 이때 DB에 쿼리를 날린다.
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            entityManager.close();
        }

        entityManagerFactory.close();
    }
}
