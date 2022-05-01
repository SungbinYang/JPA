## JPQL 소개
- JPQL은 객체지향 쿼리 언어다.따라서 테이블을 대상으로 쿼리 하는 것이 아니라 엔티티 객체를 대상으로 쿼리한다.
- JPQL은 SQL을 추상화해서 특정데이터베이스 SQL에 의존하지 않는다.
- JPQL은 결국 SQL로 변환된다.

![](https://velog.velcdn.com/images/roberts/post/ff2a03a6-29a2-4cbb-b99c-0f39c6777adc/image.png)

![](https://velog.velcdn.com/images/roberts/post/af508ded-07f9-4a94-8e1c-2fa95af66484/image.png)

## JPQL 문법

``` bash
select_문 :: = 
  select_절
  from_절 
  [where_절] 
  [groupby_절] 
  [having_절] 
  [orderby_절]
update_문 :: = update_절 [where_절] 
delete_문 :: = delete_절 [where_절]
```

- select m from Member as m where m.age > 18
- 엔티티와 속성은 대소문자 구분O (Member, age)
- JPQL 키워드는 대소문자 구분X (SELECT, FROM, where)
- 엔티티 이름 사용, 테이블 이름이 아님(Member)
- 별칭은 필수(m) (as는 생략가능)

## 집합과 정렬

``` sql
select
  COUNT(m), //회원수
  SUM(m.age), //나이 합
  AVG(m.age), //평균 나이 MAX(m.age), //최대 나이 MIN(m.age) //최소 나이
from Member m
```

- GROUP BY, HAVING
- ORDER BY

## TypeQuery, Query
- TypeQuery: 반환타입이 명확할때 사용
- Query: 반환 타입이 명확하지 않을 때 사용

``` java
TypedQuery<Member> query =
em.createQuery("SELECT m FROM Member m", Member.class);
```

``` java
Query query =
em.createQuery("SELECT m.username, m.age from Member m");
```

> 위의 코드를 보면 마지막 코드는 반환타입이 String일지 int일지 명확하지 않으므로 Query로 타입을 지정한다.

## 결과 조회 API
- query.getResultList(): 결과가 하나 이상일 때, 리스트 반환
    * 결과가 없으면  빈 컬렉션을 반환
    * 결과가 컬렉션인 경우에만 사용한다.
- query.getSingleResult(): 결과가 정확히 하나, 단일 객체 반환
    * 결과가 없으면: javax.persistence.NoResultException
    * 둘 이상이면: javax.persistence.NonUniqueResultException

## 파라미터 바인딩 - 이름 기준, 위치 기준

``` java
SELECT m FROM Member m where m.username=:username
query.setParameter("username", usernameParam);
```

``` java
SELECT m FROM Member m where m.username=?1 
query.setParameter(1, usernameParam);
```

> 실제 실무에서는 위치 기준 파라미터는 쓰지 않는다. 왜냐하면 중간에 번호를 끼어넣으면
순서가 밀리고 너무 비효율적이기 때문이다.