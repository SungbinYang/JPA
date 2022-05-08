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

## JPQL 타입 표현
- 문자: ‘HELLO’, ‘She’’s’
- 숫자: 10L(Long), 10D(Double), 10F(Float)
- Boolean: TRUE, FALSE
- ENUM: jpabook.MemberType.Admin (패키지명 포함)
- 엔티티 타입: TYPE(m) = Member (상속 관계에서 사용)

## JPQL 기타
- SQL과 문법이 같은 식
- EXISTS, IN
- AND, OR, NOT
- =, >, >=, <, <=, <>
- BETWEEN, LIKE, IS NULL

## 조건식 - CASE 식
- 기본 CASE 식

``` sql
select
  case when m.age <= 10 then '학생요금' 
  	   when m.age >= 60 then '경로요금'
  end
  else '일반요금'
from Member m
```

- 단순 CASE 식

``` sql
select
  case t.name
    when '팀A' then '인센티브110%' 
    when '팀B' then '인센티브120%'
  end
else '인센티브105%'
from Team t
```

- COALESCE: 하나씩 조회해서 null이 아니면 반환
- NULLIF: 두 값이 같으면 null 반환, 다르면 첫번째 값 반환

- 사용자 이름이 없으면 이름 없는 회원을 반환

``` sql
select coalesce(m.username,'이름 없는 회원') from Member m
```

- 사용자 이름이 ‘관리자’면 null을 반환하고 나머지는 본인의 이름을 반환

``` sql
select NULLIF(m.username, '관리자') from Member m
```
## JPQL 기본 함수
- CONCAT
- SUBSTRING
- TRIM
- LOWER, UPPER
- LENGTH
- LOCATE
- ABS, SORT, MOD
- SIZE, INDEX(JPA 용도)

## 사용자 정의 함수 호출
- 하이버네이트는 사용전 방언에 추가해야 한다.
  * 사용하는 DB 방언을 상속받고, 사용자 정의 함수를 등록한다.

``` sql
select function('group_concat', i.name) from Item i
```

## 경로 표현식
- .(점)을 찍어 객체 그래프를 탐색하는 것

``` sql
select m.username -> 상태 필드 
from Member m
join m.team t -> 단일 값 연관 필드
join m.orders o -> 컬렉션 값 연관 필드 
where t.name = '팀A'
```

## 경로 표현식 용어 정리
- 상태 필드(state field): 단순히 값을 저장하기 위한 필드 (ex: m.username)
- 연관 필드(association field): 연관관계를 위한 필드
  * 단일 값 연관 필드: @ManyToOne, @OneToOne, 대상이 엔티티(ex: m.team)
  * 컬렉션 값 연관 필드: @OneToMany, @ManyToMany, 대상이 컬렉션(ex: m.orders)

## 경로 표현식 특징
- 상태 필드(state field): 경로 탐색의 끝, 탐색X
- 단일 값 연관 경로: 묵시적 내부 조인(inner join) 발생, 탐색O
- 컬렉션 값 연관 경로: 묵시적 내부 조인 발생, 탐색X
  * FROM 절에서 명시적 조인을 통해 별칭을 얻으면 별칭을 통해 탐색 가능

## 상태 필드 경로 탐색
- JPQL: select m.username, m.age from Member m
- SQL: select m.username, m.age from Member m

## 단일 값 연관 경로 탐색
- JPQL: select o.member from Order o
- SQL:  select m.* from Orders o inner join Member m on o.member_id = m.id

## 명시적 조인, 묵시적 조인
- 명시적 조인: join 키워드 직접 사용
  * select m from Member m join m.team t
- 묵시적 조인: 경로 표현식에 의해 묵시적으로 SQL 조인 발생 (내부 조인만 가능)
  * select m.team from Member m

## 경로 표현식 - 예제
- select o.member.team from Order o -> 성공
- select t.members from Team -> 성공
- select t.members.username from Team t -> 실패
- select m.username from Team t join t.members m -> 성공

## 경로 탐색을 사용한 묵시적 조인 시 주의사항
- 항상 내부 조인이다.
- 컬렉션은 경로 탐색의 끝, 명시적 조인을 통해 별칭을 얻어야함
- 경로 탐색은 주로 SELECT, WHERE 절에서 사용하지만 묵시 적 조인으로 인해 SQL의 FROM (JOIN) 절에 영향을 줌

## 실무 조언
- 가급적 묵시적 조인 대신에 명시적 조인 사용
- 조인은 SQL 튜닝에 중요 포인트
- 묵시적 조인은 조인이 일어나는 상황을 한눈에 파악하기 어려움