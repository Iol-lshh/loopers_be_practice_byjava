package com.loopers.infrastructure.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.loopers.domain.product.ProductInfo;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
public class ProductCacheSerializer {
    private final static String VERSION = "-v1";
    private final static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
    }

    @Component
    public static class Serializer {
        public String serializeWithSignal(List<ProductInfo.ProductWithSignal> productWithSignals) {
            log.info(productWithSignals.toString());
            if (productWithSignals.isEmpty()) {
                return "";
            }
            try {
                String result = mapper.writeValueAsString(productWithSignals);
                log.info(result);
                return result;
            } catch (Exception e) {
                throw new CoreException(ErrorType.INTERNAL_ERROR, "직렬화 실패" + e.getMessage());
            }
        }
    }

    @Component
    public static class Deserializer {

        public List<ProductInfo.ProductWithSignal> deserializeWithSignal(String serialized) {
            if (serialized == null || serialized.isEmpty()) {
                return List.of();
            }
            try {
                log.info(serialized);
                // 직접 ProductWithSignal 리스트로 역직렬화
                TypeReference<List<ProductInfo.ProductWithSignal>> typeRef = new TypeReference<List<ProductInfo.ProductWithSignal>>() {};
                return mapper.readValue(serialized, typeRef);
            } catch (Exception e) {
                throw new CoreException(ErrorType.INTERNAL_ERROR, "역직렬화 실패" + e.getMessage());
            }
        }
    }
}
