package com.loopers.domain.payment;

import com.loopers.domain.point.PointService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.utils.DatabaseCleanUp;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class PaymentServiceIntegrationTest {


    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private UserService userService;
    @Autowired
    private PointService pointService;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Autowired
    private PaymentService paymentService;

    @MockitoSpyBean
    private PgClient pgClient;

    private UserEntity prepareUser() {
        String loginId = "user" + Instancio.create(Integer.class);
        String gender = "남";
        String birthDate = "1993-04-05";
        String email = "test" + Instancio.create(Integer.class) + "@gmail.com";

        var prepareUserCommand = UserCommand.Create.of(loginId, gender, birthDate, email);
        UserEntity user = userService.create(prepareUserCommand);
        assertTrue(userService.find(user.getId()).isPresent());
        pointService.charge(user.getId(), 10000L);
        return user;
    }
    private UserCardEntity prepareUserCard(Long userId) {
        UserCardCommand.Register userCardEntity = new UserCardCommand.Register(
                userId,
                "1234-5678-9012-3456",
                "SAMSUNG"
        );
        UserCardEntity card = paymentService.registerCard(userCardEntity);
        assertTrue(userService.find(userId).isPresent());
        return card;
    }

    @DisplayName("결제 요청")
    @Nested
    public class Request {
        @DisplayName("결제 요청 시, PG 클라이언트에 요청을 보낸다.")
        @Test
        void requestPgClient_whenRequestPayment() {
            // given
            UserEntity user = prepareUser();
            prepareUserCard(user.getId());
            Long orderId = 1L;
            Long totalPrice = 10000L;
            when(pgClient.request(any(PgStatement.Request.class)))
                    .thenReturn(new PgInfo.TransactionStatus("SUCCESS"));

            // when
            paymentService.request(new PaymentCommand.Request(user.getId(), orderId, totalPrice));

            // then
            verify(pgClient, times(1)).request(any(PgStatement.Request.class));
        }
    }

    @DisplayName("결제 처리 저장")
    @Nested
    public class Pay{
        @DisplayName("결제 처리시, PG 클라이언트에 상태를 조회하고, 성공시 저장한다.")
        @Test
        void savePayment_whenPgStateSuccess() {
            // given
            Long userId = 1L;
            Long orderId = 1L;
            Long totalPrice = 10000L;
            when(pgClient.find(orderId)).thenReturn(
                    Optional.of(new PgInfo.TransactionStatus("SUCCESS")));

            // when
            PaymentEntity paymentEntity = paymentService.pay(new PaymentCommand.Pay(userId, orderId, totalPrice));

            // then
            assertNotNull(paymentEntity);
            verify(pgClient, atLeastOnce()).find(orderId);
        }
    }
}
