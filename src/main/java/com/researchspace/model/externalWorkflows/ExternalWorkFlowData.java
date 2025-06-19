package com.researchspace.model.externalWorkflows;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Getter
@EqualsAndHashCode(of = { "extId" })
public class ExternalWorkFlowData implements Serializable {
  public enum ExternalService {
    GALAXY;
  }
  public enum RSPACE_CONTAINER_TYPE {
    DOCUMENT, FIELD;
  }
  public enum RSPACE_DATA_TYPE {
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
   * the name of the rspace field/document which was used to initiate data transfer for the external workflow.
   */
  @NotNull
  private String rspacecontainerName;

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
   * Name for the 'container' storing the data on the external service
   */
  @NotNull
  private String extContainerName;
  /**
   * Base url to the service this data was uploaded to
   */
  @NotNull
  private String baseUrl;

  @ManyToMany(mappedBy = "externalWorkFlowData")
  @Setter
  private List<ExternalWorkFlowInvocation> externalWorkflowInvocations;
  @Builder()
  public ExternalWorkFlowData(
      @NonNull ExternalService externalService,  @NonNull long rspacedataid,  @NonNull RSPACE_DATA_TYPE rspaceDataType,
      @NonNull long rspacecontainerId,  @NonNull String rspaceContainerName,   @NonNull RSPACE_CONTAINER_TYPE rspaceContainerType,  @NonNull String extName,
      @NonNull String extId,  @NonNull String extSecondaryId,  @NonNull String extContainerID,  @NonNull String extContainerName,  @NonNull String baseUrl
      ){
      this.externalService = externalService;
      this.rspacedataid = rspacedataid;
      this.rspaceDataType = rspaceDataType;
      this.rspacecontainerid = rspacecontainerId;
      this.rspacecontainerName = rspaceContainerName;
      this.rspaceContainerType = rspaceContainerType;
      this.extName = extName;
      this.extId = extId;
      this.extSecondaryId = extSecondaryId;
      this.extContainerID = extContainerID;
      this.extContainerName = extContainerName;
      this.baseUrl = baseUrl;
  }
  protected ExternalWorkFlowData() {
  }

}
