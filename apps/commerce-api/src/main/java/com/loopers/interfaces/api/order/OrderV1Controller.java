package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderFacade;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@RestController
public class OrderV1Controller {
    private final OrderFacade orderFacade;

    @PostMapping
    public ApiResponse<OrderV1Dto.Response.Summary> order(@RequestBody OrderV1Dto.Request.Order order) {
        OrderCriteria.Order criteria = order.toCriteria();
        var result = orderFacade.order(criteria);
        return ApiResponse.success(OrderV1Dto.Response.Summary.from(result));
    }

    @GetMapping
    public ApiResponse<List<OrderV1Dto.Response.Summary>> list(@RequestHeader("X-User-ID") Long userId) {
        var result = orderFacade.list(userId);
        return ApiResponse.success(OrderV1Dto.Response.Summary.of(result));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderV1Dto.Response.Detail> detail(
            @PathVariable Long orderId,
            @RequestHeader("X-User-ID") Long userId) {
        var result = orderFacade.detail(orderId, userId);
        return ApiResponse.success(OrderV1Dto.Response.Detail.from(result));
    }

}
