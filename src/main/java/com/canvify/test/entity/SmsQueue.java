package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_sms_queue")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SmsQueue extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "status", length = 50)
    private String status;  // pending / sent / failed

    @Column(name = "sent_on")
    private java.time.LocalDateTime sentOn;
}
