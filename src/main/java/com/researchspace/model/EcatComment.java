package com.researchspace.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.core.KeywordTokenizerFactory;
import org.apache.lucene.analysis.pattern.PatternReplaceFilterFactory;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.researchspace.model.audittrail.AuditTrailData;
import com.researchspace.model.core.GlobalIdPrefix;
import com.researchspace.model.core.GlobalIdentifier;
import com.researchspace.model.record.Record;

/* EcatComment is atomic business object of document comment, it should contains items
 * @sunny
 */

@Entity
@Audited
@Indexed
@AnalyzerDef(name = "axiopeanalyzer", tokenizer = @TokenizerDef(factory = KeywordTokenizerFactory.class), filters = {
		@TokenFilterDef(factory = PatternReplaceFilterFactory.class, params = {
				@org.hibernate.search.annotations.Parameter(name = "pattern", value = "([^a-zA-Z0-9])"),
				@org.hibernate.search.annotations.Parameter(name = "replacement", value = ""),
				@org.hibernate.search.annotations.Parameter(name = "replace", value = "all") }) })
@Table(name = "ecat_comm")
@AuditTrailData
public class EcatComment implements Serializable, IFieldLinkableElement {

	@Transient
	private static final long serialVersionUID = BaseEntity.serialVersionUID;

	private Long comId;
	private Long fieldId;
	private int sequence;

	@Field(analyze = Analyze.YES, store = Store.NO)
	@Analyzer(definition = "axiopeanalyzer")
	private String comName;

	@Field(analyze = Analyze.YES, store = Store.NO)
	private String comDesc;

	@Field(analyze = Analyze.YES, store = Store.NO)
	private String lastUpdater;

	@Field(analyze = Analyze.YES, store = Store.NO)
	private String author;

	private Record record;

	private Date createDate;
	private Date updateDate;

	@IndexedEmbedded
	private List<EcatCommentItem> items;

	/**
	 * Default constructor from Hibernate
	 */
	public EcatComment() {
		comName = "NONE";
		comDesc = "NONE";
		lastUpdater = "NONE";
		author = "NONE";
		createDate = new Date();
		updateDate = new Date();
		items = new ArrayList<>();
	}

	/**
	 * Constructor for use in production/test code
	 * 
	 * @param fieldId
	 *            id of the field this comment belongs too
	 * @param record
	 *            record this comment belongs too
	 * @param commentCreator
	 */
	public EcatComment(Long fieldId, Record record, User commentCreator) {
		this();
		setParentId(fieldId);
		setRecord(record);
		setLastUpdater(commentCreator.getUsername());
		setAuthor(commentCreator.getUsername());
	}

	/**
	 * Max length in DB of a single comment item
	 */
	public static final int MAX_COMMENT_LENGTH = 1000;

	/**
	 * Validates length of a comment item not being too long for database
	 * 
	 * @param commentItem
	 * @return <code>true</code> if comment length will fit OK in DB
	 */
	public static boolean validateLength(String commentItem) {
		if (!StringUtils.isEmpty(commentItem)) {
			return commentItem.length() <= MAX_COMMENT_LENGTH;
		}
		return false;
	}

	/**
	 * Creates a new EcatComment with copied data fields. Database id, and
	 * parent relations are null. Comment items are not copied
	 * 
	 * @return
	 */
	public EcatComment shallowCopy() {
		EcatComment copy = new EcatComment();
		copy.setAuthor(author);
		copy.setComDesc(comDesc);
		copy.setComName(comName);
		// make separate copy of the date object to maintain encapsulation
		copy.setCreateDate(new Date(getCreateDate().getTime()));
		copy.setUpdateDate(new Date(getCreateDate().getTime()));
		copy.setLastUpdater(lastUpdater);
		copy.setSequence(sequence);
		return copy;
	}

	/**
	 * Creates a new EcatComment with copied data fields. Database id, and
	 * parent relations are null. Comment items <em>are</em> copied.
	 * 
	 * @return
	 */
	@Transient
	public EcatComment getCopyWithCopiedCommentItems() {
		EcatComment copy = shallowCopy();
		for (EcatCommentItem item : items) {
			EcatCommentItem itemCopy = item.shallowCopy();
			copy.addCommentItem(itemCopy);
		}
		return copy;
	}

	@Id
	@Column(name = "com_id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getComId() {
		return comId;
	}

	public void setComId(Long comId) {
		this.comId = comId;
	}

	@Column(name = "parent_id")
	public Long getParentId() {
		return fieldId;
	}

	public void setParentId(Long parentId) {
		this.fieldId = parentId;
	}

	@Column(name = "sequence")
	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	@Column(name = "com_name")
	public String getComName() {
		return comName;
	}

	public void setComName(String comName) {
		this.comName = comName;
	}

	@Column(name = "com_desc")
	public String getComDesc() {
		return comDesc;
	}

	public void setComDesc(String comDesc) {
		this.comDesc = comDesc;
	}

	@Column(name = "com_author")
	@XmlElement
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	@XmlElement
	@Column(name = "updater_id")
	public String getLastUpdater() {
		return lastUpdater;
	}

	public void setLastUpdater(String lastUpdater) {
		this.lastUpdater = lastUpdater;
	}

	@XmlElement
	@Column(name = "create_date")
	public Date getCreateDate() {
		return createDate == null ? null : new Date(createDate.getTime());
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate == null ? null : new Date(createDate.getTime());
	}

	@XmlElement
	@Column(name = "update_date")
	public Date getUpdateDate() {
		return updateDate == null ? null : new Date(updateDate.getTime());
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate == null ? null : new Date(updateDate.getTime());
	}

	/**
	 * Unidirectional relation to a record holding this comment
	 * 
	 * @return
	 */
	@ManyToOne
	@JsonIgnore
	public Record getRecord() {
		return record;
	}

	public void setRecord(Record record) {
		this.record = record;
	}

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "com_id")
	@XmlElementWrapper
	@XmlElement(name = "commentItem")
	public List<EcatCommentItem> getItems() {
		return items;
	}

	public void setItems(List<EcatCommentItem> items) {
		this.items = items;
	}

	@Transient
	public void addCommentItem(EcatCommentItem itm) {
		itm.setEcatComment(this);
		itm.setComId(this.comId);
		items.add(itm);
	}

	@Override
	@Transient
	public Long getId() {
		return getComId();
	}

	@Override
	@Transient
	public GlobalIdentifier getOid() {
		return new GlobalIdentifier(GlobalIdPrefix.CT, getId());
	}
}