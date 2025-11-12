package com.researchspace.raid;

import com.researchspace.model.Group;
import com.researchspace.model.User;
import javax.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO[nik]: make it a @Table and move to the model
@Data
@NoArgsConstructor
@Entity
public class UserRaid {

  private Long id;
  private User owner;
  private String raidIdentifier;
  private String raidServerAlias;
  private Group associatedGroup;
}
