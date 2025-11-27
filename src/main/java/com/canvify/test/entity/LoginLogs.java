package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_login_logs")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginLogs extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "login_time")
    private LocalDateTime loginTime;

    @Column(name = "ip", length = 100)
    private String ip;

    @Column(name = "device_info")
    private String deviceInfo;
}
