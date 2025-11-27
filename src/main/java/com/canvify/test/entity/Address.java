package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_address")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Address extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "mobile", length = 50)
    private String mobile;

    @Column(name = "pincode", length = 20)
    private String pincode;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "address_line1", length = 500)
    private String addressLine1;

    @Column(name = "address_line2", length = 500)
    private String addressLine2;

    @Column(name = "landmark")
    private String landmark;

    @Column(name = "address_type", length = 20)
    private String addressType;
}
