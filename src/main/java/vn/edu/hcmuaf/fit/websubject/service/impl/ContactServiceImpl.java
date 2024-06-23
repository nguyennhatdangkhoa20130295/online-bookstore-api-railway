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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.websubject.entity.Contact;
import vn.edu.hcmuaf.fit.websubject.entity.User;
import vn.edu.hcmuaf.fit.websubject.payload.others.CurrentTime;
import vn.edu.hcmuaf.fit.websubject.service.ContactService;
import vn.edu.hcmuaf.fit.websubject.repository.ContactRepository;
import vn.edu.hcmuaf.fit.websubject.repository.UserRepository;
import vn.edu.hcmuaf.fit.websubject.service.EmailService;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.log4j.Logger;

@Service
public class ContactServiceImpl implements ContactService {
    private static final Logger Log = Logger.getLogger(ContactServiceImpl.class);
    @Autowired
    UserRepository userRepository;

    @Autowired
    ContactRepository contactRepository;

    @Autowired
    EmailService emailService;

    @Override
    public void sendContact(String fullName, String email, String title, String content) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
            Optional<User> user = userRepository.findByUsername(customUserDetails.getUsername());
            if (user.isPresent()) {
                User currentUser = user.get();
                Contact contact = new Contact();
                contact.setFullName(fullName);
                contact.setEmail(email);
                contact.setTitle(title);
                contact.setContent(content);
                contact.setUser(currentUser);
                contact.setReply(false);
                contact.setCreatedDate(CurrentTime.getCurrentTimeInVietnam());
                Log.info(user.get().getUserInfo().getFullName() + " đã gửi liên hệ");
                contactRepository.save(contact);
            }
        } catch (Exception e) {
            Log.error("Lỗi khi gửi liên hệ: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<Contact> findAll(int page, int size, String sort, String order, String filter) {
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

        Specification<Contact> specification = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (jsonFilter.has("q")) {
                String searchStr = jsonFilter.get("q").asText();
                predicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + searchStr.toLowerCase() + "%");
            }
            return predicate;
        };

        return contactRepository.findAll(specification, pageable);
    }

    @Override
    public void replyContact(int id, String email, String title, String content) {
        try {
            emailService.sendEmailContact(email, title, content);
            System.out.println(email + " " + title + " " + content);
            contactRepository.findById(id).ifPresent(contact -> {
                contact.setReply(true);
                contact.setReplyContent(content);
                contact.setReplyDate(CurrentTime.getCurrentTimeInVietnam());
                contactRepository.save(contact);
                Log.info("Đã trả lời liên hệ của " + contact.getFullName());
            });
        } catch (Exception e) {
            Log.error("Lỗi khi trả lời liên hệ: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Contact findById(int id) {
        return contactRepository.findById(id).orElse(null);
    }

    @Override
    public int checkReply(int id) {
        Optional<Contact> contact = contactRepository.findByAlreadyReply(id);
        if (contact.isPresent()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void deleteContact(int id) {
        contactRepository.deleteById(id);
    }
}
