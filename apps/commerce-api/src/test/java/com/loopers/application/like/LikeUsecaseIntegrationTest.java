package com.loopers.application.like;

import com.loopers.application.product.ProductFacade;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeStatement;
import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.loopers.support.error.ErrorType.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class LikeUsecaseIntegrationTest {
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
    @Autowired
    private LikeService likeService;

    @Autowired
    private LikeFacade likeFacade;

    private UserEntity prepareUser() {
        var prepareUserCommand = UserCommand.Create.of("testUser", "남", "1993-04-05", "test@gmail.com");
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
    class Unlike {
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
            LikeResult.Result result = likeFacade.unlikeProduct(preparedUser.getId(), preparedProduct.getId());

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
            var result = assertThrows(CoreException.class, () -> likeFacade.unlikeProduct(nonExistentUserId, preparedProduct.getId()));

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
            var result = assertThrows(CoreException.class, () -> likeFacade.unlikeProduct(preparedUser.getId(), nonExistentProductId));

            // then
            assertEquals(NOT_FOUND, result.getErrorType());
        }

        @DisplayName("좋아요 되지 않은 상품에 대해 좋아요 취소할 때, 성공적으로 취소된 상태를 보여준다.")
        @Test
        public void returnLikeInfo_whenAlreadyLiked() {
            // given
            UserEntity preparedUser = prepareUser();
            ProductEntity preparedProduct = prepareProduct();

            LikeResult.Result preExecute = likeFacade.unlikeProduct(preparedUser.getId(), preparedProduct.getId());
            assertFalse(preExecute.isLike());

            // when
            LikeResult.Result result = likeFacade.unlikeProduct(preparedUser.getId(), preparedProduct.getId());

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
    class List {
        @DisplayName("존재하는 유저로 좋아요 한 상품 목록을 조회할 때, 성공적으로 목록이 반환된다.")
        @Test
        public void returnLikeList_whenExistsUser() {
            // given
            UserEntity preparedUser = prepareUser();
            ProductEntity preparedProductLike = prepareProduct();
            ProductEntity preparedProductNotLike = prepareProduct();

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
}
