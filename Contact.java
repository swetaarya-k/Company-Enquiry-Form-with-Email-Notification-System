model.java
----------
package com.example.ENQUIRYFORM.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contact")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String mobile;
    private String inquiryType;
    private String country;
    private String industry;

    @Column(length = 1000)
    private String message;
}

Repository.java
-----------------------
package com.example.ENQUIRYFORM.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.ENQUIRYFORM.model.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}


Service.java
------------
package com.example.ENQUIRYFORM.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendMail(String to, String subject, String body) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(body);
        mailSender.send(mailMessage);
    }
}


Controller.java
---------------
package com.example.ENQUIRYFORM.controller;

import com.example.ENQUIRYFORM.model.Contact;
import com.example.ENQUIRYFORM.repository.ContactRepository;
import com.example.ENQUIRYFORM.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "http://localhost:5173")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private EmailService emailService;

    @PostMapping
    public ResponseEntity<String> submitContact(@RequestBody Contact contact) {
        try {
            // Save to DB
            contactRepository.save(contact);
            logger.info("Enquiry saved for: {}", contact.getEmail());

            // Email to Admin
            String adminMessage =
                    "New Enquiry Received\n\n" +
                            "Name: " + contact.getName() +
                            "\nEmail: " + contact.getEmail() +
                            "\nMobile: " + contact.getMobile() +
                            "\nInquiry Type: " + contact.getInquiryType() +
                            "\nCountry: " + contact.getCountry() +
                            "\nIndustry: " + contact.getIndustry() +
                            "\nMessage:\n" + contact.getMessage();

            emailService.sendMail(
                    "admin@gmail.com",  // replace with your admin email
                    "New Enquiry - " + contact.getInquiryType(),
                    adminMessage
            );
            logger.info("Admin email sent for: {}", contact.getEmail());

            // Auto-reply to user
            String userMessage =
                    "Hi " + contact.getName() + ",\n\n" +
                            "Thank you for contacting us.\n" +
                            "We have received your enquiry regarding \"" + contact.getInquiryType() + "\".\n\n" +
                            "Our team will reach out to you shortly.\n\n" +
                            "Best Regards,\nSupport Team";

            emailService.sendMail(contact.getEmail(),
                    "We Received Your Enquiry",
                    userMessage
            );
            logger.info("User email sent to: {}", contact.getEmail());

            return ResponseEntity.ok("Enquiry submitted successfully");
        } catch (Exception e) {
            logger.error("Error submitting enquiry: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body("Failed to submit enquiry: " + e.getMessage());
        }
    }
}



