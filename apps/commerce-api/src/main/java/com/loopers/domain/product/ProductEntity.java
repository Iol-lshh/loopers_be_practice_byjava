package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@Entity
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductEntity extends BaseEntity {
    private String name;
    private Long brandId;
    private Long price;
    private Long stock;
    @Embedded
    private State state;

    public ProductEntity(String name, Long brandId, Long price, Long stock) {
         super();
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        if (brandId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        if (price == null || price < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        if (stock == null || stock < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }

        this.name = name;
        this.brandId = brandId;
        this.price = price;
        this.stock = stock;
        this.state = State.close();
    }

    public static ProductEntity from(ProductCommand.Register command) {
        return new ProductEntity(
                command.name(),
                command.brandId(),
                command.price(),
                command.stock()
        );
    }

    public void deductStock(Long quantity) {
        if (quantity == null || quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        if (this.stock < quantity) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다.");
        }
        if (this.state.value != State.StateType.OPEN) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품이 판매 중이 아닙니다.");
        }
        this.stock -= quantity;
        if (this.stock == 0) {
            this.state = State.outOfStock();
        }
    }

    public void release() {
        if (this.stock < 1) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족하여 상품을 출시할 수 없습니다.");
        }
        this.state = State.release();
    }

    @Getter
    @Embeddable
    public static class State {
        @Enumerated(EnumType.STRING)
        @Column(name = "state", nullable = false)
        private StateType value;

        @Column(name = "released_at")
        private ZonedDateTime releasedAt;

        public State() {
            this.value = StateType.CLOSED;
        }

        public State(StateType value) {
            this.value = value;
            if (value == StateType.OPEN) {
                this.releasedAt = ZonedDateTime.now();
            } else {
                this.releasedAt = null;
            }
        }

        public static State release() {
            return new State(StateType.OPEN);
        }

        public static State close() {
            return new State(StateType.CLOSED);
        }

        public static State outOfStock() {
            return new State(StateType.OUT_OF_STOCK);
        }

        public enum StateType {
            CLOSED,
            OPEN,
            OUT_OF_STOCK
        }
    }
}
