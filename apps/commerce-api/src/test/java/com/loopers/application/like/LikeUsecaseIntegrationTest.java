package com.loopers.application.like;

import com.loopers.application.product.ProductFacade;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.*;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static com.loopers.support.error.ErrorType.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
public class LikeUsecaseIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(LikeUsecaseIntegrationTest.class);
    
    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private ProductFacade productFacade;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;
    @Autowired
    private BrandService brandService;
    @MockitoSpyBean
    private LikeService likeService;

    @Autowired
    private LikeFacade likeFacade;

    private UserEntity prepareUser() {
        String loginId = "user" + Instancio.create(Integer.class);
        var prepareUserCommand = UserCommand.Create.of(loginId, "남", "1993-04-05", "test@gmail.com");
        UserEntity user = userService.create(prepareUserCommand);
        assertTrue(userService.find(user.getId()).isPresent());
        return user;
    }
    private ProductEntity prepareProduct() {
        Long brandId = brandService.create("Test Brand").getId();
        assertTrue(brandService.find(brandId).isPresent());
        var productCommand = new ProductCommand.Register("Test Product", brandId, 10000L, 1L);
        ProductEntity product = productService.register(productCommand);
        assertTrue(productService.find(product.getId()).isPresent());
        return product;
    }

    @DisplayName("상품 좋아요 등록")
    @Nested
    class Like {
        @DisplayName("존재하는 유저와 상품으로 좋아요를 등록할 때, 성공적으로 등록된다.")
        @Test
        public void returnLikeInfo_whenExistsUserAndProduct() {
            // given
            Long userId = prepareUser().getId();
            ProductEntity preparedProduct = prepareProduct();

            // when
            LikeResult.Result result = likeFacade.likeProduct(userId, preparedProduct.getId());

            // then
            assertNotNull(result);
            assertEquals(userId, result.userId());
            assertEquals(LikeEntity.TargetType.PRODUCT.name(), result.targetType());
            assertEquals(preparedProduct.getId(), result.targetId());
            assertTrue(result.isLike());
        }

        @DisplayName("존재하지 않는 유저로 좋아요를 등록할 때, NOT_FOUND 예외가 발생한다.")
        @Test
        public void throwNotFound_whenUserNotExists() {
            // given
            Long nonExistentUserId = 999L;
            assertTrue(userService.find(nonExistentUserId).isEmpty());
            ProductEntity preparedProduct = prepareProduct();

            // when
            var result = assertThrows(CoreException.class, () -> likeFacade.likeProduct(nonExistentUserId, preparedProduct.getId()));

            // then
            assertEquals(NOT_FOUND, result.getErrorType());
        }

        @DisplayName("존재하지 않는 상품으로 좋아요를 등록할 때, NOT_FOUND 예외가 발생한다.")
        @Test
        public void throwNotFound_whenProductNotExists() {
            // given
            UserEntity preparedUser = prepareUser();
            Long nonExistentProductId = 999L;
            assertTrue(productService.find(nonExistentProductId).isEmpty());

            // when
            var result = assertThrows(CoreException.class, () -> likeFacade.likeProduct(preparedUser.getId(), nonExistentProductId));

            // then
            assertEquals(NOT_FOUND, result.getErrorType());
        }

        @DisplayName("이미 좋아요가 등록된 상품에 대해 다시 좋아요를 등록할 때, 성공적으로 등록된 상태를 보여준다.")
        @Test
        public void returnLikeInfo_whenAlreadyLiked() {
            // given
            UserEntity preparedUser = prepareUser();
            ProductEntity preparedProduct = prepareProduct();
            LikeResult.Result preExecutedInfo = likeFacade.likeProduct(preparedUser.getId(), preparedProduct.getId());
            assertTrue(preExecutedInfo.isLike());

            // when
            LikeResult.Result result = likeFacade.likeProduct(preparedUser.getId(), preparedProduct.getId());

            // then
            assertNotNull(result);
            assertEquals(preparedUser.getId(), result.userId());
            assertEquals(LikeEntity.TargetType.PRODUCT.name(), result.targetType());
            assertEquals(preparedProduct.getId(), result.targetId());
            assertTrue(result.isLike());
            var criteria = LikeStatement.builder()
                    .userId(preparedUser.getId()).build();
            var likes = likeService.find(criteria);
            assertEquals(1, likes.size());
        }
    }

    @DisplayName("상품 좋아요 취소")
    @Nested
    class Dislike {
        @DisplayName("존재하는 유저와 상품으로 좋아요를 취소할 때, 성공적으로 취소된다.")
        @Test
        public void returnLikeInfo_whenExistsUserAndProduct() {
            // given
            UserEntity preparedUser = prepareUser();
            ProductEntity preparedProduct = prepareProduct();
            likeFacade.likeProduct(preparedUser.getId(), preparedProduct.getId());
            var productWithSignal = productService.findWithSignal(preparedProduct.getId());
            assertFalse(productWithSignal.isEmpty());
            assertEquals(1, productWithSignal.get().getLikeCount());

            // when
            LikeResult.Result result = likeFacade.dislikeProduct(preparedUser.getId(), preparedProduct.getId());

            // then
            assertNotNull(result);
            assertEquals(preparedUser.getId(), result.userId());
            assertEquals(LikeEntity.TargetType.PRODUCT.name(), result.targetType());
            assertEquals(preparedProduct.getId(), result.targetId());
            assertFalse(result.isLike());
        }

        @DisplayName("존재하지 않는 유저로 좋아요를 등록할 때, NOT_FOUND 예외가 발생한다.")
        @Test
        public void throwNotFound_whenUserNotExists() {
            // given
            Long nonExistentUserId = 999L;
            assertTrue(userService.find(nonExistentUserId).isEmpty());
            ProductEntity preparedProduct = prepareProduct();

            // when
            var result = assertThrows(CoreException.class, () -> likeFacade.dislikeProduct(nonExistentUserId, preparedProduct.getId()));

            // then
            assertEquals(NOT_FOUND, result.getErrorType());
        }

        @DisplayName("존재하지 않는 상품으로 좋아요를 등록할 때, NOT_FOUND 예외가 발생한다.")
        @Test
        public void throwNotFound_whenProductNotExists() {
            // given
            UserEntity preparedUser = prepareUser();
            Long nonExistentProductId = 999L;
            assertTrue(productService.find(nonExistentProductId).isEmpty());

            // when
            var result = assertThrows(CoreException.class, () -> likeFacade.dislikeProduct(preparedUser.getId(), nonExistentProductId));

            // then
            assertEquals(NOT_FOUND, result.getErrorType());
        }

        @DisplayName("좋아요 되지 않은 상품에 대해 좋아요 취소할 때, 성공적으로 취소된 상태를 보여준다.")
        @Test
        public void returnLikeInfo_whenAlreadyLiked() {
            // given
            UserEntity preparedUser = prepareUser();
            ProductEntity preparedProduct = prepareProduct();

            LikeResult.Result preExecute = likeFacade.dislikeProduct(preparedUser.getId(), preparedProduct.getId());
            assertFalse(preExecute.isLike());

            // when
            LikeResult.Result result = likeFacade.dislikeProduct(preparedUser.getId(), preparedProduct.getId());

            // then
            assertNotNull(result);
            assertEquals(preparedUser.getId(), result.userId());
            assertEquals(LikeEntity.TargetType.PRODUCT.name(), result.targetType());
            assertEquals(preparedProduct.getId(), result.targetId());
            assertFalse(result.isLike());
        }
    }

    @DisplayName("내가 좋아요 한 상품 목록 조회")
    @Nested
    class ListLike {
        @DisplayName("존재하는 유저로 좋아요 한 상품 목록을 조회할 때, 성공적으로 목록이 반환된다.")
        @Test
        public void returnLikeList_whenExistsUser() {
            // given
            UserEntity preparedUser = prepareUser();
            ProductEntity preparedProductLike = prepareProduct();

            likeFacade.likeProduct(preparedUser.getId(), preparedProductLike.getId());

            // when
            var likeList = productFacade.list(preparedUser.getId());

            // then
            assertFalse(likeList.isEmpty());
            assertEquals(1, likeList.size());
            assertEquals(preparedProductLike.getId(), likeList.stream().findFirst().get().id());
        }

        @DisplayName("존재하지 않는 유저로 좋아요 한 상품 목록을 조회할 때, NOT_FOUND 예외가 발생한다.")
        @Test
        public void throwNotFound_whenUserNotExists() {
            // given
            Long nonExistentUserId = 999L;
            assertTrue(userService.find(nonExistentUserId).isEmpty());

            // when
            var result = assertThrows(CoreException.class, () -> productFacade.list(nonExistentUserId));

            // then
            assertEquals(NOT_FOUND, result.getErrorType());
        }

        @DisplayName("좋아요 한 상품이 없는 유저로 좋아요 한 상품 목록을 조회할 때, 빈 목록이 반환된다.")
        @Test
        public void returnEmptyList_whenNoLikedProducts() {
            // given
            UserEntity preparedUser = prepareUser();

            // when
            var likeList = productFacade.list(preparedUser.getId());

            // then
            assertTrue(likeList.isEmpty());
        }
    }

    @DisplayName("좋아요 동시성 처리")
    @Nested
    class Concurrency {
        @DisplayName("동시에 좋아요를 등록할 때, 정상적으로 좋아요 등록된 만큼 좋아요 수가 조회된다")
        @Test
        public void returnLikeInfo_whenConcurrentLikes() throws InterruptedException {
            // given
            int threadCount = 5;

            List<UserEntity> users = IntStream.range(0, threadCount)
                    .mapToObj(i -> prepareUser()).toList();
            ProductEntity product = prepareProduct();

            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            // when
            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                executor.submit(() -> {
                    try {
                        UserEntity user = users.get((int) (Math.random() * threadCount));
                        log.info("스레드 {}: 사용자 {}가 상품 {}에 좋아요 등록 시작", threadIndex, user.getId(), product.getId());
                        LikeResult.Result result = likeFacade.likeProduct(user.getId(), product.getId());
                        log.info("스레드 {}: 사용자 {}가 상품 {}에 좋아요 등록 완료 - 결과: {}",
                                threadIndex, user.getId(), product.getId(), result.isLike());
                    } catch (Exception e) {
                        log.error("스레드 {}: 좋아요 등록 실패", threadIndex, e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            log.info("=== 모든 스레드 작업 완료 ===");

            // then
            List<LikeEntity> likes = likeService.find(LikeStatement.builder()
                    .likeTypeAndTargetId(LikeEntity.TargetType.PRODUCT, product.getId())
                    .build());
            LikeSummaryEntity summary = likeService.findSummary(product.getId(), LikeEntity.TargetType.PRODUCT).orElseThrow();

            log.info("=== 테스트 결과 ===");
            log.info("전체 좋아요 시도 수: {}", threadCount);
            log.info("실제 좋아요 엔티티 수: {}", likes.size());
            log.info("좋아요 카운트: {}", summary.getLikeCount());
            assertEquals(likes.size(), summary.getLikeCount());
        }

        @DisplayName("동시에 좋아요를 취소할 때, 정상적으로 취소 처리된 만큼 좋아요 수가 조회된다")
        @Test
        public void returnLikeInfo_whenConcurrentDislikes() throws InterruptedException {
            // given
            int threadCount = 5;

            List<UserEntity> users = IntStream.range(0, threadCount)
                    .mapToObj(i -> prepareUser()).toList();
            ProductEntity product = prepareProduct();
            users.forEach(user -> {
                likeFacade.likeProduct(user.getId(), product.getId());
            });

            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            // when
            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                executor.submit(() -> {
                    try {
                        UserEntity user = users.get((int) (Math.random() * threadCount));
                        log.info("스레드 {}: 사용자 {}가 상품 {}에 좋아요 취소 시작", threadIndex, user.getId(), product.getId());
                        LikeResult.Result result = likeFacade.dislikeProduct(user.getId(), product.getId());
                        log.info("스레드 {}: 사용자 {}가 상품 {}에 좋아요 취소 완료 - 결과: {}",
                                threadIndex, user.getId(), product.getId(), result.isLike());
                    } catch (Exception e) {
                        log.error("스레드 {}: 좋아요 취소 실패", threadIndex, e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            log.info("=== 모든 스레드 작업 완료 ===");

            // then
            List<LikeEntity> likes = likeService.find(LikeStatement.builder()
                    .likeTypeAndTargetId(LikeEntity.TargetType.PRODUCT, product.getId())
                    .build());
            LikeSummaryEntity summary = likeService.findSummary(product.getId(), LikeEntity.TargetType.PRODUCT).orElseThrow();

            log.info("=== 테스트 결과 ===");
            log.info("전체 좋아요 취소 시도 수: {}", threadCount);
            log.info("실제 좋아요 엔티티 수: {}", likes.size());
            log.info("좋아요 카운트: {}", summary.getLikeCount());
            assertEquals(likes.size(), summary.getLikeCount());
        }

        @DisplayName("동시에 좋아요와 좋아요 취소를 섞어서 실행할 때, 정상적으로 처리된다")
        @Test
        public void returnLikeInfo_whenConcurrentLikesAndDislikes() throws InterruptedException {
            // given
            int threadCount = 5;

            List<UserEntity> users = IntStream.range(0, threadCount)
                    .mapToObj(i -> prepareUser()).toList();
            ProductEntity product = prepareProduct();
            likeFacade.likeProduct(users.get(0).getId(), product.getId());
            likeFacade.likeProduct(users.get(1).getId(), product.getId());
            likeFacade.likeProduct(users.get(2).getId(), product.getId());
            likeFacade.likeProduct(users.get(3).getId(), product.getId());

            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            // when
            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                executor.submit(() -> {
                    try {
                        UserEntity user = users.get(threadIndex);
                        if (threadIndex > 3) {
                            log.info("스레드 {}: 사용자 {}가 상품 {}에 좋아요 등록 시작", threadIndex, user.getId(), product.getId());
                            LikeResult.Result result = likeFacade.likeProduct(user.getId(), product.getId());
                            log.info("스레드 {}: 사용자 {}가 상품 {}에 좋아요 등록 완료 - 결과: {}",
                                    threadIndex, user.getId(), product.getId(), result.isLike());
                        } else {
                            log.info("스레드 {}: 사용자 {}가 상품 {}에 좋아요 취소 시작", threadIndex, user.getId(), product.getId());
                            LikeResult.Result result = likeFacade.dislikeProduct(user.getId(), product.getId());
                            log.info("스레드 {}: 사용자 {}가 상품 {}에 좋아요 취소 완료 - 결과: {}",
                                    threadIndex, user.getId(), product.getId(), result.isLike());
                        }
                    } catch (Exception e) {
                        log.error("스레드 {}: 좋아요/취소 작업 실패", threadIndex, e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            log.info("=== 모든 스레드 작업 완료 ===");

            // then
            List<LikeEntity> likes = likeService.find(LikeStatement.builder()
                    .likeTypeAndTargetId(LikeEntity.TargetType.PRODUCT, product.getId())
                    .build());
            LikeSummaryEntity summary = likeService.findSummary(product.getId(), LikeEntity.TargetType.PRODUCT).orElseThrow();

            ArgumentCaptor<Long> targetIdCaptor = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<LikeEntity.TargetType> targetTypeCaptor = ArgumentCaptor.forClass(LikeEntity.TargetType.class);

            verify(likeService, atLeast(threadCount)).increaseLikeCount(targetIdCaptor.capture(), targetTypeCaptor.capture());
            List<Long> capturedTargetIds = targetIdCaptor.getAllValues();
            Set<Long> uniqueTargetIds = new HashSet<>(capturedTargetIds);

            log.info("=== 테스트 결과 ===");
            log.info("전체 좋아요/취소 시도 수: {}", threadCount);
            log.info("전체 좋아요 시도 수: {}", 1);
            log.info("전체 좋아요 취소 시도 수: {}", 4);
            log.info("실제 좋아요 엔티티 수: {}", likes.size());
            log.info("좋아요 카운트: {}", summary.getLikeCount());
            log.info("increaseLikeCount 실제 호출 횟수: {}", capturedTargetIds.size());
            log.info("예상 최소 호출 횟수: {}", threadCount);
            log.info("재시도로 인한 추가 호출 횟수: {}", capturedTargetIds.size() - uniqueTargetIds.size());
            assertTrue(likes.size() != 4);
            assertTrue(summary.getLikeCount() >= 0);
            assertEquals(likes.size(), summary.getLikeCount());
            assertTrue(capturedTargetIds.size() >= threadCount);
        }
    }
}
