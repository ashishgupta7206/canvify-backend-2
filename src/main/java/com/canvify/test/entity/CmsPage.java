package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "m_cms_pages")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CmsPage extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "slug", unique = true)
    private String slug;

    @Column(name = "content_html", columnDefinition = "TEXT")
    private String contentHtml;
}
