---
title: "시퀀스 다이어그램"
---

- 상품 목록 / 상품 상세 / 브랜드 조회
- 상품 좋아요 등록/취소 (멱등 동작)
- 주문 생성 및 결제 흐름 (재고 차감, 포인트 차감, 외부 시스템 연동)

## 브랜드 & 상품 (Brands / Products)

### 브랜드 조회

```mermaid
sequenceDiagram
    actor User
    User->>+BrandUsecase: get(brandId)
    BrandUsecase->>+BrandService: find(brandId): Optional<BrandEntity>

    alt 브랜드 조회 실패
        BrandUsecase->>User: ApiResponse<Metadata.fail(NotFound)>
    end
    BrandUsecase->>-User: ApiResponse<Metadata.success(BrandResponse)>
```

### 상품 목록 조회

- 브랜드 필터링
- 정렬 기준 (최신순, 가격 오름차순, 좋아요 내림차순 등)
- 페이징 처리
- 좋아요 표기

```mermaid
sequenceDiagram
    actor User
    User->>+ProductUsecase: get(ProductCriteria)
    opt 브랜드 필터링 시
        ProductUsecase->>+BrandService: find(brandId): Optional<BrandEntity>
        alt 브랜드 조회 실패 시
            ProductUsecase->>User: ApiResponse<Metadata.fail(NotFound)>
        end
    end
    ProductUsecase->>+ProductService: find(ProductCriteria): List<ProductEntity>
    ProductUsecase->>-User: ApiResponse<Metadata.success(List<ProductResponse>)>
```

### 상품 정보 조회

- 좋아요 표기

```mermaid
sequenceDiagram
    actor User
    User->>+ProductUsecase: get(ProductCriteria)
    ProductUsecase->>+ProductService: find(ProductCriteria): Optional<ProductEntity>
    alt 상품 조회 실패 시
        ProductUsecase->>User: ApiResponse<Metadata.fail(NotFound)>
    end
    ProductUsecase->>-User: ApiResponse<Metadata.success(ProductResponse)>
```

## 좋아요 (Likes)

- 상품 좋아요 등록/취소 (멱등 동작)
- 사용자는 **각 상품에 한 번만 좋아요** 를 누를 수 있습니다.
- 상품에 대한 **좋아요 추가/해제** 기능은 멱등하게 동작하여야 합니다.
- 상품 목록, 상품 상세 정보 조회 시 **총 좋아요 수**를 표기해야 합니다.

### 상품 좋아요 등록

```mermaid
sequenceDiagram
    actor User
    User->>+LikeUsecase: like(userId, productId)
    LikeUsecase->>+UserService: find(userId): Optional<UserEntity>
    alt 사용자 조회 실패 시
        LikeUsecase->>User: ApiResponse<Metadata.fail(NotFound)>
    end
    LikeUsecase->>+ProductService: addLike(userId, productId): ProductEntity
    alt 상품 조회 실패 시
        LikeUsecase->>User: ApiResponse<Metadata.fail(NotFound)>
    end
    LikeUsecase->>-User: ApiResponse<Metadata.success(ProductUserLikeResponse)>
```

### 상품 좋아요 취소

```mermaid
sequenceDiagram
    actor User
    User->>+LikeUsecase: unlike(userId, productId)
    LikeUsecase->>+UserService: find(userId): Optional<UserEntity>
    alt 사용자 조회 실패 시
        LikeUsecase->>User: ApiResponse<Metadata.fail(NotFound)>
    end
    LikeUsecase->>+ProductService: removeLike(userId, productId): ProductEntity
    alt 상품 조회 실패 시
        LikeUsecase->>User: ApiResponse<Metadata.fail(NotFound)>
    end
    LikeUsecase->>-User: ApiResponse<Metadata.success(ProductUserLikeResponse)>
```

### 내가 좋아요 한 상품 목록 조회

```mermaid
sequenceDiagram
    actor User
    User->>+LikeUsecase: get(LikeCriteria)
    LikeUsecase->>+UserService: find(userId): Optional<UserEntity>
    alt 사용자 조회 실패 시
        LikeUsecase->>User: ApiResponse<Metadata.fail(NotFound)>
    end
    LikeUsecase->>+ProductService: find(ProductCriteria): List<ProductEntity>
    LikeUsecase->>-User: ApiResponse<Metadata.success(List<ProductResponse>)>
```

## 주문 / 결제 (Orders/Payments)

- 주문 생성 및 결제 흐름 (재고 차감, 포인트 차감, 외부 시스템 연동)
- {상품 아이디, 수량} 리스트
- 상품 재고 확인 및 차감
- 포인트 확인 및 차감
- 주문 정보 외부 시스템 전송 (Mock 처리 가능)

### 주문 요청

```mermaid
sequenceDiagram
    actor User
    User->>+OrderUsecase: order(OrderCommand.Create)
    OrderUsecase->>UserService: find(userId): Optional<UserEntity>
    alt 사용자 조회 실패 시
        OrderUsecase->>User: ApiResponse<Metadata.fail(NotFound)>
    end
    OrderUsecase->>OrderService: create(OrderCommand.Create): OrderEntity
    OrderUsecase->>ProductService: decreaseStock(ProductEntity): ProductEntity
    alt 상품 조회 실패 시
        OrderUsecase->>User: ApiResponse<Metadata.fail(NotFound)>
    end
    alt 상품 재고 부족으로 차감 실패 시
        OrderUsecase->>User: ApiResponse<Metadata.fail(BadRequest)>
    end
    OrderUsecase->>-User: ApiResponse<Metadata.success(OrderResponse)>
```

### 결제 요청

```mermaid
sequenceDiagram
    actor User
    User->>+PaymentUsecase: pay(orderId)
    PaymentUsecase->>OrderService: find: Order
    alt 주문 조회 실패 시
        PaymentUsecase->>User: ApiResponse<Metadata.fail(NotFound)>
    end
    PaymentUsecase->>+PaymentWay: pay (Point)
    alt 결제 처리 실패 시
        PaymentUsecase->>User: ApiResponse<Metadata.fail(InternalServerError)>
    end
    PaymentUsecase->>+PaymentService: save
    PaymentUsecase->>+OrderService: updateStatus(orderId, OrderStatus.PAID)
    PaymentUsecase->>-User: ApiResponse<Metadata.success(PaymentResponse)>
```

### 유저의 주문 목록 조회

```mermaid
sequenceDiagram
    actor User
    User->>+OrderUsecase: get(OrderCriteria)
    OrderUsecase->>UserService: find(userId): Optional<UserEntity>
    alt 사용자 조회 실패 시
        OrderUsecase->>User: ApiResponse<Metadata.fail(NotFound)>
    end
    OrderUsecase->>+OrderService: find(OrderCriteria): List<OrderEntity>
    OrderUsecase->>-User: ApiResponse<Metadata.success(List<OrderResponse>)>
```

### 단일 주문 상세 조회

```mermaid
sequenceDiagram
    actor User
    User->>+OrderUsecase: get(orderId)
    OrderUsecase->>+OrderService: find(orderId): Optional<OrderEntity>
    alt 주문 조회 실패 시
        OrderUsecase->>User: ApiResponse<Metadata.fail(NotFound)>
    end
    OrderUsecase->>+ProductService: find(ProductCriteria): Optional<ProductEntity>
    alt 상품 조회 실패 시
        OrderUsecase->>User: ApiResponse<Metadata.fail(InternalServerError)>
    end
    OrderUsecase->>-User: ApiResponse<Metadata.success(OrderDetailResponse)> 
```
