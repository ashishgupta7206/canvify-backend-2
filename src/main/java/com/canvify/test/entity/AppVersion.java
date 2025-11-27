package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "m_app_version")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AppVersion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "platform", length = 50)
    private String platform;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "force_update")
    private Boolean forceUpdate;

    @Column(name = "release_notes", columnDefinition = "TEXT")
    private String releaseNotes;
}
