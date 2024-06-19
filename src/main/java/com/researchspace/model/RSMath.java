package com.researchspace.model;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.researchspace.model.core.GlobalIdPrefix;
import com.researchspace.model.core.GlobalIdentifier;
import com.researchspace.model.field.Field;
import com.researchspace.model.record.Record;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a piece of Math content in a text field. <br/>
 * All associations are lazy.
 */
@Entity
@Audited
@Getter
@Setter
@EqualsAndHashCode(of = {"mathSvg", "latex"})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class RSMath implements Serializable, IFieldLinkableElement {


	public static final int LATEX_COLUMN_SIZE = 2000;
	/** */
	private static final long serialVersionUID = -8764723602823933240L;
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;
	
	/**
	 * This will be <code>null</code> if mathelement is in a snippet
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	private Field field;
	
	@Column(nullable = false, length = LATEX_COLUMN_SIZE)
	@Size(max = LATEX_COLUMN_SIZE)
	private String latex;
	
	/**
	 * This can be a structured document or a snippet.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	private Record record;
	
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH},
			 fetch = FetchType.LAZY)
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	private ImageBlob mathSvg;

	/**
	 * For tools/frameworks
	 */
	public RSMath() {
	}

	/**
	 * Public constructor
	 * @param svgStringAsBytes
	 * @param latex
	 * @param parentField
	 */
	public RSMath (byte[] svgStringAsBytes, String latex, Field parentField) {
		this.mathSvg = new ImageBlob(svgStringAsBytes);
		this.latex = latex;
		this.field = parentField;	
	}

	/**
	 * Generates a complete copy of this object's data fields.<br/>
	 * The copy has a null database id and is <b>not</b> connected to the
	 * original owning field.
	 * 
	 * @return A new copy
	 */
	public RSMath shallowCopy() {
		RSMath copy = new RSMath();
		copy.setMathSvg(getMathSvg());
		copy.setLatex(getLatex());
		return copy;
	}

	@Override
	@Transient
	@JsonIgnore
	public GlobalIdentifier getOid() {
		return new GlobalIdentifier(GlobalIdPrefix.MA, getId());
	}
	
	@Override
	public String toString() {
		return "RSMath [id=" + id + ", field=" + ((field != null)? field.getId() + "": "null")
				+ "record=" + ((record != null)? record.getId() + "": "null")
				+ "latex=" + latex
				+ ((getMathSvg() != null)
										? StringUtils.abbreviate(
												new String(getMathSvg().getData(), StandardCharsets.UTF_8), 255)
										: "null");    
	}

	/**
	 * Gets SVG String representation
	 * @return
	 */
	@Transient
	public String  getMathSvgString (){
		return new String ( getMathSvg().getData(), StandardCharsets.UTF_8);
	}

}
