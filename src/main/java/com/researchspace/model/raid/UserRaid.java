package com.researchspace.model.raid;

import com.researchspace.model.Group;
import com.researchspace.model.User;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@EqualsAndHashCode(of = {"raidServerAlias", "raidIdentifier"})
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(
    columnNames = {"raid_server_alias", "raid_natural_key"}))
@ToString(of = {"raidServerAlias", "raidIdentifier"})
public class UserRaid {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(name = "raid_natural_key")
  private String raidIdentifier;

  @Column(name = "raid_server_alias")
  private String raidServerAlias;

  @ManyToOne
  @JoinColumn(name = "owner_id", referencedColumnName = "id")
  private User owner;

  @OneToOne(fetch = FetchType.LAZY, mappedBy = "raid")
  private Group groupAssociated;

  public UserRaid(User owner, Group groupAssociated, String raidServerAlias,
      String raidIdentifier) {
    this.owner = owner;
    this.raidIdentifier = raidIdentifier;
    this.raidServerAlias = raidServerAlias;
    this.groupAssociated = groupAssociated;
  }
}
