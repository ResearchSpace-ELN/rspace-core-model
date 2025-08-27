package com.researchspace.model.dmps;

import com.researchspace.model.EcatDocumentFile;
import com.researchspace.model.User;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Join table for User to DMP, storing date of import.
 */
@Entity
@Data
@NoArgsConstructor
public class DMPUser {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(optional = false)
    private User user;

    @Column(nullable = false)
    private Instant timestamp;

    @ManyToOne
    private EcatDocumentFile dmpDownloadFile;

    @Enumerated(EnumType.STRING)
    private DMPSource source;

    private String doiLink;
    private String dmpLink;
    private String dmpId;
    private String title;

    public DMPUser(User user, DmpDto dmpDto) {
        this.user = user;
        this.dmpId = dmpDto.getDmpId();
        this.title = dmpDto.getTitle();
        this.source = dmpDto.getSource();
        this.dmpLink = dmpDto.getDmpLink();
        this.doiLink = dmpDto.getDoiLink();
        this.timestamp = Instant.now();
    }

}
