package vn.edu.hcmuaf.fit.websubject.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.websubject.entity.Product;
import vn.edu.hcmuaf.fit.websubject.entity.Promotion;
import vn.edu.hcmuaf.fit.websubject.payload.others.CurrentTime;
import vn.edu.hcmuaf.fit.websubject.repository.ProductRepository;
import vn.edu.hcmuaf.fit.websubject.repository.PromotionRepository;
import vn.edu.hcmuaf.fit.websubject.service.PromotionService;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

@Service
public class PromotionServiceImpl implements PromotionService {

    private static final Logger Log = Logger.getLogger(PromotionServiceImpl.class);
    final
    PromotionRepository promotionRepository;

    private final ProductRepository productRepository;

    @Autowired
    public PromotionServiceImpl(PromotionRepository promotionRepository, ProductRepository productRepository) {
        this.promotionRepository = promotionRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    @Override
    public Page<Promotion> findAllByIsCode(int page, int size, String sort, String order, String filter) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (order.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }
        Sort sortPa = Sort.by(direction, sort);
        Pageable pageable = PageRequest.of(page, size, sortPa);

        JsonNode jsonFilter;
        try {
            jsonFilter = new ObjectMapper().readTree(java.net.URLDecoder.decode(filter, StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Specification<Promotion> specification = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (jsonFilter.has("q")) {
                String searchStr = jsonFilter.get("q").asText();
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(criteriaBuilder.lower(root.get("discount")), "%" + searchStr.toLowerCase() + "%"));
            }

            if (jsonFilter.has("isCode")) {
                boolean isCode = jsonFilter.get("isCode").asInt() == 1;
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isCode"), isCode));
            }

            return predicate;
        };

        return promotionRepository.findAll(specification, pageable);
    }

    @Override
    public List<Promotion> findAllByIsCode() {
        List<Promotion> promotions = promotionRepository.findByIsCode();
        return promotions;
    }


    @Override
    public Promotion getPromotionById(int id) {
        return promotionRepository.findById(id).orElse(null);
    }

    @Override
    public Promotion getPromotionByCode(String code) {
        Optional<Promotion> promoCode = promotionRepository.findByCode(code);
        return promoCode.orElse(null);
    }

    @Override
    public boolean checkPromoCode(String code) {
        Optional<Promotion> promoCode = promotionRepository.findByCode(code);
        return promoCode.isPresent();
    }

    @Override
    public boolean checkPromoCodeDate(String code) {
        try {
            Optional<Promotion> promoCode = promotionRepository.findByCode(code);
            if (promoCode.isPresent()) {
                Promotion promotion = promoCode.get();
                Date currentDate = CurrentTime.getCurrentTimeInVietnam();
                System.out.println("Current date: " + currentDate);
                System.out.println("Start date: " + promotion.getStartDate());
                System.out.println("End date: " + promotion.getEndDate());
                return promotion.getStartDate().before(currentDate) && promotion.getEndDate().after(currentDate);
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.error("Lỗi khi kiểm tra mã khuyến mãi: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addPromotion(Integer idProduct, String code, int discount, Date startDate, Date endDate) {
        try {
            Promotion promotion = new Promotion();
            if (idProduct != null) {
                Product product = productRepository.findById(idProduct).orElse(null);
                promotion.setProduct(product);
            } else {
                promotion.setProduct(null);
            }
            promotion.setCode(code);
            promotion.setDiscount(discount);
            promotion.setStartDate(startDate);
            promotion.setEndDate(endDate);
            promotion.setIsCode(code != null);
            promotionRepository.save(promotion);
            Log.info("Thêm khuyến mãi với mức discount " + discount + " thành công");
        } catch (Exception e) {
            Log.error("Lỗi khi thêm khuyến mãi: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updatePromotion(int id, Integer idProduct, String code, int discount, Date startDate, Date endDate) {
        try {
            Promotion promotion = promotionRepository.findById(id).orElse(null);
            assert promotion != null;
            if (idProduct != null) {
                Product product = productRepository.findById(idProduct).orElse(null);
                promotion.setProduct(product);
            } else {
                promotion.setProduct(null);
            }
            promotion.setCode(code);
            promotion.setDiscount(discount);
            promotion.setStartDate(startDate);
            promotion.setEndDate(endDate);
            promotion.setIsCode(code != null);
            promotionRepository.save(promotion);
            Log.info("Cập nhật khuyến mãi với id#" + id + " thành công");
        } catch (Exception e) {
            Log.error("Lỗi khi cập nhật khuyến mãi: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deletePromotion(int id) {
        promotionRepository.deleteById(id);
    }
}
