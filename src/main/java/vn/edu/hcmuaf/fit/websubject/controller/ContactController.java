package vn.edu.hcmuaf.fit.websubject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.websubject.entity.Contact;
import vn.edu.hcmuaf.fit.websubject.payload.request.ContactRequest;
import vn.edu.hcmuaf.fit.websubject.service.ContactService;

@RestController
@RequestMapping("/api/contact")
public class ContactController {
    @Autowired
    ContactService contactService;

    @GetMapping("")
    public ResponseEntity<Page<Contact>> getAllContacts(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "") String filter,
                                                     @RequestParam(defaultValue = "5") int perPage,
                                                     @RequestParam(defaultValue = "id") String sort,
                                                     @RequestParam(defaultValue = "DESC") String order) {
        try {
            Page<Contact> contacts = contactService.findAll(page, perPage, sort, order, filter);
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContactById(@PathVariable int id) {
        try {
            Contact contact = contactService.findById(id);
            return ResponseEntity.ok(contact);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/check-reply/{id}")
    public ResponseEntity<?> checkReply(@PathVariable int id) {
        try {
            int isReply = contactService.checkReply(id);
            if (isReply == 1) {
                return ResponseEntity.ok("Replied");
            } else {
                return ResponseEntity.ok("Not reply yet");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/send")
    public ResponseEntity<String> addContact(@RequestBody ContactRequest contactRequest) {
        try {
            contactService.sendContact(contactRequest.getFullName(), contactRequest.getEmail(), contactRequest.getTitle(), contactRequest.getContent());
            return ResponseEntity.ok("Send contact successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to add blog" + contactRequest);
        }
    }
    @PutMapping("/edit/{id}")
    public ResponseEntity<String> replyContact(@PathVariable int id, @RequestBody ContactRequest contactRequest) {
        try {
            contactService.replyContact(id, contactRequest.getEmail(), contactRequest.getTitle(), contactRequest.getContentReply());
            return ResponseEntity.ok("Reply contact successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to reply contact" + contactRequest + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteContact(@PathVariable int id) {
        try {
            contactService.deleteContact(id);
            return ResponseEntity.ok("Delete contact successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete contact");
        }
    }
}
