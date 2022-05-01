package me.sungbin.jpql;

import javax.persistence.*;
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
            Team team = new Team();
            team.setName("teamA");
            entityManager.persist(team);

            Member member = new Member();
            member.setUsername("teamA");
            member.setAge(10);
            member.changeTeam(team);

            entityManager.persist(member);

            entityManager.flush();
            entityManager.clear();

            String qlString = "select m from Member m join Team t on m.username = t.name";
            List<Member> resultList = entityManager.createQuery(qlString, Member.class)
                    .getResultList();

            System.out.println("resultList.size() = " + resultList.size());

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
