
package com.researchspace.model.inventory;

import com.researchspace.model.User;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import com.researchspace.core.util.JacksonUtil;
import com.researchspace.core.util.SecureStringUtils;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.IndexedEmbedded;

/**
 * Basic model used to represent all identifiers added to inventory items
 * 
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"id", "type", "identifier", "state", "title", "otherData"})
@Audited
public class DigitalObjectIdentifier extends InventoryRecordConnectedEntity implements Serializable {

	private static final long serialVersionUID = 1015505407767178312L;

	public enum IdentifierType {
		DATACITE_IGSN
	}
	
	private Long id;
	
	private IdentifierType type = IdentifierType.DATACITE_IGSN; // only one supported right now

	private String identifier;
	
	private String title;

	private String state;

	private User owner;

	@Setter(AccessLevel.PRIVATE)
	private String publicLink;

	private boolean customFieldsOnPublicPage;
	
	public enum IdentifierOtherProperty { 
		CREATOR_NAME, CREATOR_TYPE, CREATOR_AFFILIATION, CREATOR_AFFILIATION_IDENTIFIER, PUBLISHER, PUBLICATION_YEAR, RESOURCE_TYPE, RESOURCE_TYPE_GENERAL, LOCAL_URL, PUBLIC_URL
	}

	public enum IdentifierOtherListProperty {
		SUBJECTS, DESCRIPTIONS, RELATED_IDENTIFIERS, DATES, GEOLOCATIONS
	}
	private String otherDataJsonString;

	private boolean deleted;

	public DigitalObjectIdentifier(String identifier, String title) {
		setIdentifier(identifier);
		setTitle(title);
		setPublicLink(SecureStringUtils.getURLSafeSecureRandomString(16));
	}

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	public Long getId() {
		return id;
	}

	@ManyToOne
	@JoinColumn(nullable = true)
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@IndexedEmbedded
	public User getOwner() {
		return owner;
	}

	@Lob
	protected String getOtherDataJsonString() {
		return otherDataJsonString;
	}

	protected void setOtherDataJsonString(String otherDataJsonString) {
		this.otherDataJsonString = otherDataJsonString;
		resetOtherDataMap();
	}

	@Transient
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private Map<String, String> otherDataMap = new HashMap<>(); 

	public void resetOtherDataMap() {
		if (StringUtils.isNotEmpty(otherDataJsonString)) {
			otherDataMap = JacksonUtil.fromJson(otherDataJsonString, Map.class);
		} else {
			otherDataMap = new HashMap<>();
		}
	}

	@Transient
	private String getOtherData(String propertyName) {
		return otherDataMap.get(propertyName);
	}

	private void addOtherData(String propertyName, String data) {
		otherDataMap.put(propertyName, data);
		setOtherDataJsonString(JacksonUtil.toJson(otherDataMap));
	}

	@Transient
	public String getOtherData(IdentifierOtherProperty property) {
		return getOtherData(property.toString());
	}

	public void addOtherData(IdentifierOtherProperty property, String data) {
		addOtherData(property.toString(), data);
	}

	@Transient
	public List<String> getOtherListData(IdentifierOtherListProperty property) {
		String otherData = getOtherData(property.toString());
		if (otherData == null) {
			return null;
		}
		return JacksonUtil.fromJson(otherData, List.class);
	}

	public void addOtherListData(IdentifierOtherListProperty property, List<String> data) {
		addOtherData(property.toString(), JacksonUtil.toJson(data));
	}

	@Transient
	public boolean isAssociated(){
		return getInventoryRecord() != null;
	}

	@Transient
	public boolean canBeAssigned(){
		return !this.isDeleted() && !this.isAssociated() && "draft".equals(this.getState());
	}
	
}
