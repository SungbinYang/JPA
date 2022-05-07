package me.sungbin.jpql;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

/**
 * packageName : me.sungbin.jpql
 * fileName : App
 * author : rovert
 * date : 2022/05/01
 * description :
 * ===========================================================
 * DATE 			AUTHOR			 NOTE
 * -----------------------------------------------------------
 * 2022/05/01       rovert         최초 생성
 */

public class App {
    public static void main(String[] args) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("hello");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        try {
            Member member1 = new Member();
            member1.setUsername("관리자1");
            entityManager.persist(member1);

            Member member2 = new Member();
            member2.setUsername("관리자2");
            entityManager.persist(member2);

            entityManager.flush();
            entityManager.clear();

            String qlString = "select m.team from Member m";
            List<Team> resultList = entityManager.createQuery(qlString, Team.class).getResultList();

            for (Team s : resultList) {
                System.out.println("s = " + s);
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
