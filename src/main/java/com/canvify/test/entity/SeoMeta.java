package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "m_seo_meta")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeoMeta extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_type", length = 100)
    private String pageType;

    @Column(name = "page_id")
    private Long pageId;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_keywords", columnDefinition = "TEXT")
    private String metaKeywords;

    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;
}
