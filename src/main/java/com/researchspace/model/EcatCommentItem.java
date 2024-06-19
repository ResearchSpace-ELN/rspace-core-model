package com.researchspace.model;

/* EcatCommentItem is atomic  document comment items and annotation
 * It replaces the ECATComment which is not comply with bean name conversion
 * @sunny
 */

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.researchspace.core.util.DateUtil;
import com.researchspace.model.core.GlobalIdPrefix;
import com.researchspace.model.core.GlobalIdentifier;
import com.researchspace.session.SessionTimeZoneUtils;



@Entity
@Audited
@Table(name = "ecat_comm_item")
@Indexed
@Analyzer(definition = "structureAnalyzer")
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class EcatCommentItem implements Serializable, IFieldLinkableElement {
	@Transient
	private static final long serialVersionUID = BaseEntity.serialVersionUID;

	private Long itemId;
	private Long comId;
	@Field(analyze = Analyze.YES, store = Store.NO)
	private String itemName;
	@Field(analyze = Analyze.YES, name = "fields.fieldData", store = Store.NO)
	private String itemContent;
	@Field(analyze = Analyze.NO, name = "owner.username")
	private String lastUpdater;
	private Date createDate;
	private Date updateDate;
	private int gmt_offset;

	private EcatComment ecatComment;

	/**
	 * Default constructor for hibernate
	 */
	public EcatCommentItem() {
		itemName = "NONE";
		lastUpdater = "NONE";
		createDate = new Date();
		updateDate = new Date();
	}

	/**
	 * Public constructor for test/prod usage
	 */
	public EcatCommentItem(EcatComment parentComment, String comment, User user) {
		this();
		setComId(parentComment.getComId());
		setItemContent(comment);
		setLastUpdater(user.getUsername());
		setGmt_offset(DateUtil.getRealGMT(gmt_offset));
	}

	public EcatCommentItem shallowCopy() {
		EcatCommentItem copy = new EcatCommentItem();
		copy.setCreateDate(new Date(getCreateDate().getTime()));
		copy.setGmt_offset(gmt_offset);
		copy.setItemContent(itemContent);
		copy.setItemName(itemName);

		copy.setUpdateDate(new Date(getCreateDate().getTime()));
		copy.setLastUpdater(lastUpdater);
		return copy;
	}

	@Id
	@Column(name = "item_id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	@Column(name = "com_id")
	public Long getComId() {
		return comId;
	}

	public void setComId(Long comId) {
		this.comId = comId;
	}

	@Column(name = "item_name")
	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	@Column(name = "item_content", length = 1000)
	@XmlElement
	public String getItemContent() {
		return itemContent;
	}

	public void setItemContent(String itemContent) {
		this.itemContent = itemContent;
	}

	@Column(name = "updater_id")
	@XmlElement
	public String getLastUpdater() {
		return lastUpdater;
	}

	public void setLastUpdater(String lastUpdater) {
		this.lastUpdater = lastUpdater;
	}

	@Column(name = "create_date")
	@XmlElement
	public Date getCreateDate() {
		return createDate == null ? null : new Date(createDate.getTime());
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate == null ? null : new Date(createDate.getTime());
	}

	@Column(name = "update_date")
	@XmlElement
	public Date getUpdateDate() {
		return updateDate == null ? null : new Date(updateDate.getTime());
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate == null ? null : new Date(updateDate.getTime());
	}

	@Column(name = "gmt_offset")
	public int getGmt_offset() {
		return gmt_offset;
	}

	public void setGmt_offset(int gmt_offset) {
		this.gmt_offset = gmt_offset;
	}

	@ManyToOne
	@JoinColumn(name = "com_id", insertable = false, updatable = false, nullable = false)
	public EcatComment getEcatComment() {
		return ecatComment;
	}

	public void setEcatComment(EcatComment ecatComment) {
		this.ecatComment = ecatComment;
	}

	@Transient
	public String getFormatDate() {
		String out = new SessionTimeZoneUtils().formatDateTimeForClient(createDate);
		return out;
	}

	@Override
	@Transient
	public Long getId() {
		return getItemId();
	}

	@Override
	@Transient
	public GlobalIdentifier getOid() {
		return new GlobalIdentifier(GlobalIdPrefix.CM, getId());
	}

}