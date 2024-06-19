package com.researchspace.model.elninventory;

import com.researchspace.model.core.GlobalIdPrefix;
import com.researchspace.model.core.GlobalIdentifier;
import com.researchspace.model.field.Field;
import com.researchspace.model.inventory.InventoryRecord;
import com.researchspace.model.units.QuantityInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents list of rsinventory materials used in ELN experiment.
 */
@Entity
@Audited
@Getter
@Setter
@EqualsAndHashCode
public class ListOfMaterials implements Serializable {

	private static final long serialVersionUID = -3207293416276907214L;

	private Long id;

	private Field elnField;

	private String name;

	private String description;
	
	private List<MaterialUsage> materials = new ArrayList<>();

	/** for hibernate & pagination criteria */
	public ListOfMaterials() { }
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	public Long getId() {
		return id;
	}

	@Transient
	public GlobalIdentifier getOid() {
		return new GlobalIdentifier(GlobalIdPrefix.LM, getId());
	}

	@ManyToOne
	@JoinColumn(nullable = false)
	public Field getElnField() {
		return elnField;
	}

	/**
	 * @return the materials used in this list
	 */
	@OneToMany(mappedBy = "parentLom", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy(value = "id")
	public List<MaterialUsage> getMaterials() {
		return materials;
	}
	
	public MaterialUsage addMaterial(InventoryRecord invRec, QuantityInfo quantityInfo) {
		MaterialUsage usage = new MaterialUsage();
		usage.setInventoryRecord(invRec);
		usage.setUsedQuantity(quantityInfo);
		usage.setParentLom(this);
		materials.add(usage);
		return usage;
	}

}
