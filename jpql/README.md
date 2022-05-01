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

## 프로젝션
- SELECT 절에 조회할 대상을 지정하는 것
- 프로젝션 대상: 엔티티, 임베디드 타입, 스칼라 타입(숫자, 문자등 기본 데이터 타입)
- SELECT m FROM Member m -> 엔티티 프로젝션
- SELECT m.team FROM Member m -> 엔티티 프로젝션
- SELECT m.address FROM Member m -> 임베디드 타입 프로젝션
- SELECT m.username, m.age FROM Member m -> 스칼라 타입 프로젝션
- DISTINCT로 중복 제거

## 프로젝션 - 여러 값 조회
- SELECT m.username, m.age FROM Member m
- Query 타입으로 조회
- Object[] 타입으로 조회
- new 명령어로 조회
  * 단순 값을 DTO로 바로 조회
    * SELECT new jpabook.jpql.UserDTO(m.username, m.age) FROM Member m
  * 패키지 명을 포함한 전체클래스 명 입력
  * 순서와 타입이 일치하는 생성자 필요

## 페이징 API
- JPA는 페이징을 다음 두 API로 추상화
- setFirstResult(int startPosition) : 조회 시작 위치 (0부터 시작)
- setMaxResults(int maxResult) : 조회할 데이터 수

``` java
//페이징 쿼리
String jpql = "select m from Member m order by m.name desc";
List<Member> resultList = em.createQuery(jpql, Member.class)
.setFirstResult(10).setMaxResults(20).getResultList();
```

## 페이징 API - MySQL 방언

``` sql
SELECT
  M.ID AS ID,
  M.AGE AS AGE,
  M.TEAM_ID AS TEAM_ID,
FROM
ORDER BY
  M.NAME AS NAME
  MEMBER M
M.NAME DESC LIMIT ?, ?
```

## 페이징 API - Oracle 방언

``` sql
SELECT * FROM
  ( SELECT ROW_.*, ROWNUM ROWNUM_
FROM
  ( SELECT
    M.ID AS ID,
    M.AGE AS AGE,
    M.TEAM_ID AS TEAM_ID,
  ) ROW_
    M.NAME AS NAME
    FROM MEMBER M
    ORDER BY M.NAME
    WHERE ROWNUM <= ?
  )
WHERE ROWNUM_ > ?
```

## 조인
- 내부 조인:SELECT m FROM Member m [INNER] JOIN m.team t
- 외부 조인:SELECT m FROM Member m LEFT [OUTER] JOIN m.team t
- 세타 조인:select count(m) from Member m, Team t where m.username = t.name

## 조인 - ON 절
- ON절을 활용한 조인 (JPA 2.1부터 지원)
  * 조인대상필터링
  * 연관관계 없는 엔티티 외부 조인(하이버네이트 5.1부터)

### 조인 대상 필터링
- 예) 회원과 팀을 조인하면서, 팀 이름이 A인 팀만 조인

``` sql
// JPQL
SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'A'
```

``` sql
// SQL
SELECT m.*, t.* FROM
Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and t.name='A
```

### 연관관계 없는 엔티티 외부 조인
- 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인

``` sql
// JPQL
SELECT m, t FROM=
Member m LEFT JOIN Team t on m.username = t.name
```

``` sql
// SQL
SELECT m.*, t.* FROM
Member m LEFT JOIN Team t ON m.username = t.name
```

## 서브 쿼리
- 나이가 평균보다 많은 회원

``` sql
select m from Member m
where m.age > (select avg(m2.age) from Member m2)
```

- 한건이라도주문한고객

``` sql
select m from Member m
where (select count(o) from Order o where m = o.member) > 0
```

## 서브 쿼리 지원 함수
- [NOT] EXISTS (subquery): 서브쿼리에 결과가 존재하면 참
  * {ALL | ANY | SOME} (subquery)
  * ALL 모두 만족하면 참
  * ANY, SOME: 같은 의미, 조건을 하나라도 만족하면 참
- [NOT] IN (subquery): 서브쿼리의 결과 중 하나라도 같은 것이 있으면 참

## 서브 쿼리 - 예제
- 팀A 소속인 회원

``` sql
select m from Member m
where exists (select t from m.team t where t.name = ‘팀A')
```

- 전체 상품 각각의 재고보다 주문량이 많은 주문들

``` sql
select o from Order o 
where o.orderAmount > ALL (select p.stockAmount from Product p)
```

- 어떤 팀이든 팀에 소속된 회원

``` sql
select m from Member m
where m.team = ANY (select t from Team t)
```

## JPA 서브 쿼리 한계
- JPA는 WHERE, HAVING절에서만 서브 쿼리 가능
- SELECT 절도 가능(하이버네이트에서 지원)
- FROM 절의 서브 쿼리는 현재 JPQL에서 불가능
  * 조인으로 풀 수 있으면 풀어서 해결
  * 조인으로도 못 풀면 쿼리를 2번 날리거나 애플리케이션 단에서 조작