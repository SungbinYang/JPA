## 요구사항 분석
- 회원은 상품을 주문할 수 있다.
- 주문 시 여러 종류의 상품을 선택할 수 있다.

## 도메인 모델 분석
- 회원과 주문의 관계: 회원은 여러 번 주문할 수 있다. (일대다)
- 주문과상품의관계:주문할때여러상품을선택할수있다.반 대로 같은 상품도 여러 번 주문될 수 있다. 주문상품 이라는 모델 을 만들어서 다대다 관계를 일다대, 다대일 관계로 풀어냄

![](https://velog.velcdn.com/images/roberts/post/c43158a0-7c40-4966-b7f4-33482a915e34/image.png)

## 테이블 설계

![](https://velog.velcdn.com/images/roberts/post/7635dfa4-39b2-49ed-bbc3-fcaf70205966/image.png)

## 엔티티 설계와 매핑

![](https://velog.velcdn.com/images/roberts/post/46a7419e-0a9c-4649-ab6c-8443c96d74f2/image.png)

## 데이터 중심 설계의 문제점
- 현재 방식은 객체 설계를 테이블 설계에 맞춘 방식
- 테이블의 외래키를 객체에 그대로 가져옴
- 객체 그래프 탐색이 불가능
- 참조가 없으므로 UML도 잘못됨

> 위의 ERD와 UML은 너무 객체지향스럽지가 않고 객체그래프탐색이 전혀 안되어져 있다.
그래서 결국 우리는 테이블간 연관관계 매핑에 대해 학습을 하고 이 문제점을 해결해야할것이다.

## 테이블 구조
- 테이블 구조는 이전과 같다.

![](https://velog.velcdn.com/images/roberts/post/03559177-e1e9-4299-84df-ab58bbc2008f/image.png)

## 객체 구조
- 참조를 사용하도록 변경

![](https://velog.velcdn.com/images/roberts/post/af118d12-b211-40d9-a328-d544a13c8fef/image.png)

> 최대한 단방향으로 해결하자!