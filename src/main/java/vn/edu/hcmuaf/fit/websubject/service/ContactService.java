package vn.edu.hcmuaf.fit.websubject.service;

import org.springframework.data.domain.Page;
import vn.edu.hcmuaf.fit.websubject.entity.Contact;

public interface ContactService {
    void sendContact(String fullName, String email, String title, String content);

    Page<Contact> findAll(int page, int size, String sort, String order, String filter);

    void replyContact(int id, String email, String title, String content);

    Contact findById(int id);

    int checkReply(int id);

    void deleteContact(int id);
}
