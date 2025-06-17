package com.researchspace.model.externalWorkflows;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = { "extId" })
public class ExternalWorkFlowData implements Serializable {
  enum ExternalService {
    GALAXY;
  }
  enum RSPACE_CONTAINER_TYPE {
    DOCUMENT, FIELD;
  }
  enum RSPACE_DATA_TYPE {
    LOCAL, EXTERNAL;
  }
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /**
   * The service hosting the data
   */
  @NotNull
  private ExternalService externalService;
  /**
   * the rspace id of the data that was uploaded to the external service
   */
  @NotNull
  private long rspacedataid;
  /**
   * whether this is data local to RSpace or External data
   */
  @NotNull
  private RSPACE_DATA_TYPE rspaceDataType;

  /**
   * the id of the rspace field/document which was used to initiate data transfer for the external workflow.
   */
  @NotNull
  private long rspacecontainerid;
  /**
   * the type of RSpace container this data belongs to
   */
  @NotNull
  private RSPACE_CONTAINER_TYPE rspaceContainerType;

  /**
   * Filename for this data on the remote service
   */
  @NotNull
  private String extName;
  /**
   * The ID provided by the external service for this data
   */
  @NotNull
  private String extId;

  /**
   * A secondary ID provided by the external service for this data (optional)
   */
  private String extSecondaryId;

  /**
   * ID for the 'container' (eg folder, history etc) storing the data on the external service
   */
  @NotNull
  private String extContainerID;
  /**
   * Base url to the service this data was uploaded to
   */
  @NotNull
  private String baseUrl;

  @ManyToMany(mappedBy = "externalWorkFlowData")
  @Setter
  private List<ExternalWorkflowInvocation> externalWorkflowInvocations;

  protected ExternalWorkFlowData() {
  }
}
