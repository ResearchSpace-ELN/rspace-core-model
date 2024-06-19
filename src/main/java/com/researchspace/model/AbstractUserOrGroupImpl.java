package com.researchspace.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.PermissionResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.researchspace.model.audittrail.AuditTrailIdentifier;
import com.researchspace.model.core.GlobalIdPrefix;
import com.researchspace.model.core.GlobalIdentifier;
import com.researchspace.model.core.UniquelyIdentifiable;
import com.researchspace.model.permissions.ConstraintBasedPermission;
import com.researchspace.model.permissions.ConstraintPermissionResolver;

/**
 * Contains common behaviour for both Users and Groups
 */

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
/**
 * By default we must explicitly include XML elements
 */
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractUserOrGroupImpl implements UserOrGroup, UniquelyIdentifiable, Permissable {

	private Long id;

	private PermissionHandler permHandler;

    private Date creationDate;
    
    private Integer version;
    
    private boolean privateProfile;

	@Transient
    public abstract String getDisplayName();
	
	protected AbstractUserOrGroupImpl() {
		permHandler = new PermissionHandler();
		creationDate = new Date();
	}

	/**
	 * Gets the creation date of the user or group
	 * 
	 * @return
	 */
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreationDate() {
		if (creationDate != null) {
			return new Date(creationDate.getTime());
		} else {
			return null;
		}
	}

	void setCreationDate(Date creationDate) {
		if (creationDate != null) {
			this.creationDate = new Date(creationDate.getTime());
		}
	}

	/**
	 * Casts this abstract class to a User
	 * 
	 * @return
	 */
	@Transient
	public User asUser() {
		return (User) this;
	}

	/**
	 * Casts this abstract class to a Group
	 * 
	 * @return
	 */
	@Transient
	public Group asGroup() {
		return (Group) this;
	}

	/**
	 * 
	 * @return
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	// or get an exception if using auto
	public Long getId() {
		return id;
	}

	/**
	 * for hibernate only, don't call in production code.
	 * 
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}

    @Version
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Convert(converter = PermissionStringConverter.class)
	@Column(columnDefinition = "MEDIUMTEXT")
	Set<String> getPermissionStrings() {
		return permHandler.getPermissionStrings();
	}

	@Transient
	public Set<Permission> getPermissions() {
		return permHandler.getPermissions();
	}

	void setPermissionStrings(Set<String> permissionStrings) {
		permHandler.setPermissionStrings(permissionStrings);
	}

	/**
	 * Public API to add a {@link Permission} to this user or group.
	 * 
	 * @param p
	 *            A {@link Permission} object
	 */
	public void addPermission(ConstraintBasedPermission p) {
		permHandler.addPermission(p);
	}

	/**
	 * Public API to add a Set of {@link Permission} to this user or group.
	 * 
	 * @param toAdd
	 *            possibly empty but non-null set of ConstraintBasedPermission;
	 */
	public void addPermissions(Set<ConstraintBasedPermission> toAdd) {
		for (ConstraintBasedPermission cbp : toAdd) {
			addPermission(cbp);
		}
	}

	public void addPermission(String permission) {
		PermissionResolver pr = new ConstraintPermissionResolver();
		addPermission((ConstraintBasedPermission) pr.resolvePermission(permission));
	}

	/**
	 * Public API to remove a Set of {@link Permission} from this group.
	 * 
	 * @param p
	 *            A {@link Permission} object
	 */
	public void removePermission(Permission p) {
		permHandler.removePermission(p);
	}

	/**
	 * Public API to add a Set of {@link Permission} to this user or group.
	 * 
	 * @param toAdd
	 *            possibly empty but non-null set of ConstraintBasedPermission;
	 */
	public void removePermissions(Set<ConstraintBasedPermission> toAdd) {
		for (ConstraintBasedPermission cbp : toAdd) {
			removePermission(cbp);
		}
	}

	/**
	 * Whether this User or Group should be listed in public search
	 */
	@Column(nullable = true)
    public boolean isPrivateProfile() {
        return privateProfile;
    }

    public void setPrivateProfile(Boolean privateProfile) {
        this.privateProfile = privateProfile == null ? false : privateProfile;
    }

	/**
	 * Gets global ID for this object
	 */
	@Override
	@JsonIgnore
	@Transient
	public GlobalIdentifier getOid() {
		return new GlobalIdentifier(getGlobalIdPrefix(), getId());
	}

	/**
	 * Subclasses return a unique {@link GlobalIdPrefix}
	 * 
	 * @return
	 */
	@Transient
	protected abstract GlobalIdPrefix getGlobalIdPrefix();

	@Transient
	@JsonIgnore
	@AuditTrailIdentifier()
	public String getOidString() {
		return getOid().getIdString();
	}

}
