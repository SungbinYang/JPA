package me.sungbin.hellpjpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

/**
 * packageName : me.sungbin.hellpjpa
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
            Member member = entityManager.find(Member.class, 5L);
            member.setName("Ronaldo");

            entityManager.close();

            Member member2 = entityManager.find(Member.class, 5L);

            System.out.println("=================================");

            transaction.commit(); // 이때 DB에 쿼리를 날린다.
        } catch (Exception e) {
            transaction.rollback();
        } finally {
            entityManager.close();
        }

        entityManagerFactory.close();
    }
}
