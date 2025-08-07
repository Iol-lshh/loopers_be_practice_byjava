package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentCriteria;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@RestController
public class PaymentV1Controller {

    private final PaymentFacade paymentFacade;

    @PostMapping
    public ApiResponse<PaymentV1Dto.Response.Summary> pay(@RequestBody PaymentV1Dto.Request.Pay request) {
        PaymentCriteria.Pay criteria = new PaymentCriteria.Pay(
                request.userId(),
                request.orderId(),
                request.paymentType()
        );
        var result = paymentFacade.pay(criteria);
        return ApiResponse.success(PaymentV1Dto.Response.Summary.from(result));
    }
}
