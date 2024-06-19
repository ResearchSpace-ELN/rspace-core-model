package com.researchspace.model.netfiles;

import com.researchspace.model.EcatMediaFile;
import com.researchspace.model.User;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Entity class for external storage locations
 */
@Entity
@Data
@ToString
@EqualsAndHashCode(of = { "id" })
public class ExternalStorageLocation implements Serializable {

	@Transient
	private static final long serialVersionUID = 153465452212L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

	@NotNull
	@ManyToOne
	private NfsFileStore fileStore;

	@NotNull
	private Long externalStorageId;

	@NotNull
	@ManyToOne
	private EcatMediaFile connectedMediaFile;

	@NotNull
	@ManyToOne
	private User operationUser;

	private Long operationDate;


	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getOperationDate() {
		if (operationDate == null) {
			operationDate = new Date().getTime();
		}
		return new Date(operationDate);
	}

}
