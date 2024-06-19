package com.researchspace.model.inventory.field;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import javax.validation.ConstraintViolationException;

import org.apache.commons.lang.Validate;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.IndexedEmbedded;

import com.researchspace.model.field.FieldType;
import com.researchspace.model.inventory.InventoryFile;

import lombok.AccessLevel;
import lombok.Setter;

@Entity
@Audited
@DiscriminatorValue("attachment")
public class InventoryAttachmentField extends SampleField {

	private static final long serialVersionUID = -5815246533648394407L;

	@Setter(AccessLevel.PRIVATE)
	@IndexedEmbedded
	private List<InventoryFile> files = new ArrayList<>();
	
	public InventoryAttachmentField() {
		super(FieldType.ATTACHMENT,"");
	}
	
	public InventoryAttachmentField(String name) {
		super(FieldType.ATTACHMENT, name);
	}

	/**
	 * All files ever connected to that sample field, deleted or not.
	 * There should be at most one non-deleted file there.
	 */
	@OneToMany(mappedBy = "sampleField", cascade = CascadeType.ALL, orphanRemoval = true)
	List<InventoryFile> getFiles() {
		return files;
	}
	
	@Transient
	public String getData() {
		return super.getData();
	}
	
	/**
	 * @return non-deleted attached file, or null
	 */
	@Transient
	@Override
	public InventoryFile getAttachedFile() {
		return getFiles().stream().filter(iFile -> !iFile.isDeleted()).findFirst().orElse(null);
	}

	@Override
	public void setAttachedFile(InventoryFile newFile) {
		Validate.notNull(newFile);
		
		InventoryFile oldAttachedFile = getAttachedFile();
		if (oldAttachedFile != null) {
			oldAttachedFile.setDeleted(true);
		}
		
		files.add(newFile);
		newFile.setSampleField(this);
	}
	
	@Override
	public InventoryAttachmentField shallowCopy() {
		InventoryAttachmentField copy = new InventoryAttachmentField();
		copyFields(copy);
		InventoryFile attachedFile = getAttachedFile();
		if (attachedFile != null) {
			copy.setAttachedFile(attachedFile.shallowCopy());
		}
		return copy;
	}

	@PrePersist
	@PreUpdate
	public void validateBeforeSave() {
		long attachmentCount = getFiles().stream().filter(iFile -> !iFile.isDeleted()).count();
		if (attachmentCount > 1) { 
			throw new ConstraintViolationException("Inventory attachment field can link only one attachment", null);
		}
	}	
	
}
