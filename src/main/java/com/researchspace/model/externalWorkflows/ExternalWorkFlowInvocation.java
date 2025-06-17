package com.researchspace.model.externalWorkflows;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = { "extId"})
public class ExternalWorkFlowInvocation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /**
   * The ID provided by the external service for this invocation
   */
  @NotNull
  private String extId;
  @ManyToMany
  @JoinTable(name = "ExtInvoc_ExtData", joinColumns = {
      @JoinColumn(name = "invocation_id") }, inverseJoinColumns = @JoinColumn(name = "data_id"))
  @NotNull
  private List<ExternalWorkFlowData> externalWorkFlowData;
  @Setter
  @NotNull
  private String status;

  @ManyToOne
  private ExternalWorkFlow externalWorkFlow;

  protected ExternalWorkFlowInvocation() {
  }

}
