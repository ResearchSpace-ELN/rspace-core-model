package com.researchspace.model.inventory;

import static java.util.stream.Collectors.toCollection;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.Validate;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import com.researchspace.model.FileProperty;
import com.researchspace.model.User;
import com.researchspace.model.audittrail.AuditDomain;
import com.researchspace.model.audittrail.AuditTrailData;
import com.researchspace.model.core.GlobalIdPrefix;
import com.researchspace.model.inventory.field.ExtraField;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents RSpace Inventory Container.
 */
@Entity
@Audited
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@AuditTrailData(auditDomain = AuditDomain.INV_CONTAINER)
@Indexed
public class Container extends MovableInventoryRecord implements Serializable {

	private static final long serialVersionUID = 1015505407767174705L;

	public enum ContainerType {
		LIST, GRID, IMAGE, WORKBENCH
	}

	private ContainerType containerType = ContainerType.LIST;

	private User owner;

	@IndexedEmbedded(prefix = "fields.")
	private List<ExtraField> extraFields = new ArrayList<>();

	@IndexedEmbedded
	private List<Barcode> barcodes = new ArrayList<>();

	private List<DigitalObjectIdentifier> identifiers = new ArrayList<>();

	@IndexedEmbedded(prefix = "fields.")
	private List<InventoryFile> files = new ArrayList<>();
	
	private FileProperty locationsImageFileProperty;

	private List<ContainerLocation> locations = new ArrayList<>();
	private int locationsCount;
	
	@Setter(AccessLevel.PRIVATE)
	private int contentCount;
	@Setter(AccessLevel.PRIVATE)
	private int contentCountSubSamples;
	@Setter(AccessLevel.PRIVATE)
	private int contentCountContainers;

	/* Number of columns in grid layout. Will be 0 for non-grid container. */ 
	@Setter(AccessLevel.PRIVATE)
	@Min(0)
	@Max(24)
	private int gridLayoutColumnsNumber;

	/* Number of rows in grid layout. Will be 0 for non-grid container. */
	@Setter(AccessLevel.PRIVATE)
	@Min(0)
	@Max(24)
	private int gridLayoutRowsNumber;
	
	public enum GridLayoutAxisLabelEnum {
		ABC, CBA, N123, N321
	}
	private GridLayoutAxisLabelEnum gridLayoutColumnsLabelType = GridLayoutAxisLabelEnum.N123;
	private GridLayoutAxisLabelEnum gridLayoutRowsLabelType = GridLayoutAxisLabelEnum.ABC;
	
	private boolean canStoreSamples = true;
	private boolean canStoreContainers = true;

	/**
	 * Create a container of specified type.
	 * @param type
	 */
	public Container(ContainerType type) {
		this.containerType = type;
	}
	
	/** for hibernate, record factory & pagination criteria */
	public Container (){}
	
	/**
	 * Makes a valid ListContainer with no content
	 * 
	 * @param canStoreContainers
	 * @param canStoreSamples
	 * @return
	 */
	public static Container createListContainer(boolean canStoreContainers, boolean canStoreSamples) {
		Container rc = new Container(ContainerType.LIST);
		rc.setAllowedStoredTypes(canStoreContainers, canStoreSamples);
		return rc;
	}

	/**
	 * Makes a valid GridContainer of fixed dimensions
	 * 
	 * @param columns
	 * @param rows
	 * @return
	 */
	public static Container createGridContainer(int columns, int rows, boolean canStoreContainers, boolean canStoreSamples) {
		Validate.isTrue(rows > 0 && columns > 0);
		Container rc = new Container(ContainerType.GRID);
		rc.configureAsGridLayoutContainer(columns, rows);
		rc.setAllowedStoredTypes(canStoreContainers, canStoreSamples);
		return rc;
	}

	/**
	 * Makes a valid ImageContainer with no locations defined
	 * 
	 * @return
	 */
	public static Container createImageContainer(boolean canStoreContainers, boolean canStoreSamples) {
		Container rc = new Container(ContainerType.IMAGE);
		rc.setAllowedStoredTypes(canStoreContainers, canStoreSamples);
		return rc;
	}

	private void setAllowedStoredTypes(boolean canStoreContainers, boolean canStoreSamples) {
		Validate.isTrue(canStoreContainers || canStoreSamples);
		setCanStoreContainers(canStoreContainers);
		setCanStoreSamples(canStoreSamples);
	}

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@NotNull
	public ContainerType getContainerType() {
		return containerType;
	}
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@NotNull
	public GridLayoutAxisLabelEnum getGridLayoutColumnsLabelType() {
		return gridLayoutColumnsLabelType;
	}

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@NotNull
	public GridLayoutAxisLabelEnum getGridLayoutRowsLabelType() {
		return gridLayoutRowsLabelType;
	}
	
	@ManyToOne
	@JoinColumn(nullable = false)
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@IndexedEmbedded
	public User getOwner() {
		return owner;
	}

	/**
	 * @return the list of extra fields of this Container, including deleted fields.
	 */
	@OneToMany(mappedBy = "container", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy(value = "id")
	@Override
	protected List<ExtraField> getExtraFields() {
		return extraFields;
	}

	protected void setExtraFields(List<ExtraField> extraFields) {
		this.extraFields = extraFields;
		refreshActiveExtraFields();
	}

	/**
	 * @return the list of barcodes of this SubSample, including deleted fields.
	 */
	@OneToMany(mappedBy = "container", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy(value = "id")
	@Override
	protected List<Barcode> getBarcodes() {
		return barcodes;
	}

	protected void setBarcodes(List<Barcode> barcodes) {
		this.barcodes = barcodes;
	}

	@OneToMany(mappedBy = "container", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy(value = "id")
	@Override
	protected List<DigitalObjectIdentifier> getIdentifiers() {
		return identifiers;
	}

	protected void setIdentifiers(List<DigitalObjectIdentifier> identifiers) {
		this.identifiers = identifiers;
		refreshActiveIdentifiers();
	}

	/**
	 * @return the list of files attached to this Container
	 */
	@OneToMany(mappedBy = "container", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy(value = "id")
	protected List<InventoryFile> getFiles() {
		return files;
	}

	protected void setFiles(List<InventoryFile> files) {
		this.files = files;
		refreshActiveAttachedFiles();
	}
	
	@ManyToOne
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	public FileProperty getLocationsImageFileProperty() {
		return locationsImageFileProperty;
	}

	@NotAudited
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "container", orphanRemoval = true)
	@OrderBy(value = "coordY, coordX")
	public List<ContainerLocation> getLocations() {
		return locations;
	}

	public boolean removeLocation(ContainerLocation location) {
		boolean result = locations.remove(location);
		if (result) {
			resetLocationsCount();
		}
		return result;
	}

	@Transient
	public List<Container> getStoredContainers() {
		return locations.stream().map(ContainerLocation::getStoredContainer)
				.filter(Objects::nonNull).collect(toCollection(()-> new ArrayList<>()));
	}
	
	@Transient
	public List<SubSample> getStoredSubSamples() {
		return locations.stream().map(ContainerLocation::getStoredSubSample)
				.filter(Objects::nonNull).collect(toCollection(()-> new ArrayList<>()));
	}

	@Transient
	@Override
	public GlobalIdPrefix getGlobalIdPrefix() {
		return isWorkbench() ? GlobalIdPrefix.BE : GlobalIdPrefix.IC;
	}
	
	@Transient
	@Override
	public InventoryRecord.InventoryRecordType getType() {
		return InventoryRecordType.CONTAINER;
	}

	public ContainerLocation addToNewLocation(MovableInventoryRecord record) {
		if (!isListLayoutContainer() && !isWorkbench()) {
			throw new IllegalArgumentException(getContainerType() + " container cannot store content without providing specific coordinates");
		}
		ContainerLocation newLocation = createOrRetrieveLocationWithCoords(locations.size() + 1, 1);
		setRecordInLocation(record, newLocation);
		return newLocation;
	}
	
	public ContainerLocation addToNewLocationWithCoords(MovableInventoryRecord record, Integer coordX, Integer coordY) {
		if (isListLayoutContainer() || isWorkbench()) {
			throw new IllegalArgumentException(getContainerType() + " container can't use explicit location coordinates");
		}
		if (isImageLayoutContainer()) {
			throw new IllegalArgumentException("Image container must provide target location id, not coordinates");
		}
		ContainerLocation targetLocation = createOrRetrieveLocationWithCoords(coordX, coordY);
		setRecordInLocation(record, targetLocation);
		return targetLocation;
	}
	
	public ContainerLocation createNewImageContainerLocation(Integer coordX, Integer coordY) {
		if (!isImageLayoutContainer()) {
			throw new IllegalArgumentException(getContainerType() + " container cannot add locations directly");
		}
		return createOrRetrieveLocationWithCoords(coordX, coordY);
	}

	private ContainerLocation createOrRetrieveLocationWithCoords(Integer coordX, Integer coordY) {
		Validate.notNull(coordX);
		Validate.notNull(coordY);
		validateNewCoordinates(coordX, coordY);
		
		// if location already exists, return it
		Optional<ContainerLocation> existingLocation = findSavedLocationByIdOrCoordinates(null, coordX, coordY);
		if (existingLocation.isPresent()) {
			return existingLocation.get();
		}

		// if coordinates are not saved yet, create new location
		ContainerLocation newLocation = new ContainerLocation(this);
		newLocation.setCoordX(coordX);
		newLocation.setCoordY(coordY);
		locations.add(newLocation);
		resetLocationsCount();
		
		return newLocation;
	}

	public Optional<ContainerLocation> findSavedLocationByIdOrCoordinates(Long id, Integer coordX, Integer coordY) {
		return locations.stream()
				.filter(l -> (id != null && id.equals(l.getId())) || 
						(coordX !=null && coordX.equals(l.getCoordX()) && coordY != null && coordY.equals(l.getCoordY())))
				.findAny();
	}

	private void validateNewCoordinates(Integer coordX, Integer coordY) {
		if (isGridLayoutContainer()) {
			if (coordX > getGridLayoutColumnsNumber() || coordY > getGridLayoutRowsNumber()) {
				throw new IllegalArgumentException(String.format("Requested new location (%d,%d) "
						+ "is outside grid container dimensions (columns:%d, rows:%d)", coordX, coordY, 
						getGridLayoutColumnsNumber(), getGridLayoutRowsNumber()));
			}
		}
	}

	private void resetLocationsCount() {
		switch (containerType) {
			case LIST:
			case WORKBENCH:
				locationsCount = 0;
				break;
			case GRID:
				locationsCount = gridLayoutColumnsNumber * gridLayoutRowsNumber;
				break;
			case IMAGE:
				locationsCount = locations.size();
				break;
		}
	}

	@Transient
	public void setRecordInLocation(MovableInventoryRecord record, ContainerLocation location) {
		Validate.notNull(record);
		Validate.notNull(location);

		assertCanStoreRecord(record);

		InventoryRecord currentlyStoredRecord = location.getStoredRecord();
		if (currentlyStoredRecord != null && !currentlyStoredRecord.equals(record)) {
			throw new IllegalArgumentException("Location: " + location.getId() + " is already taken by the record: "
					+ currentlyStoredRecord.getGlobalIdentifier());
		}

		if (record.isContainer()) {
			Container container = (Container) record;
			location.addStoredRecord(container);
			container.setParentLocation(location);
		}
		if (record.isSubSample()) {
			SubSample subSample = (SubSample) record;
			location.addStoredRecord(subSample);
			subSample.setParentLocation(location);
		}
		record.setLastMoveDate(Instant.now());

		resetContentCount();
	}
	
	private void resetContentCount() {
		contentCountSubSamples = 0;
		contentCountContainers = 0;
		for (ContainerLocation loc : locations) {
			InventoryRecord storedRecord = loc.getStoredRecord();
			if (storedRecord != null) {
				if (storedRecord.isSubSample()) {
					contentCountSubSamples++;
				} else {
					contentCountContainers++;
				}
			}
		}
		contentCount = contentCountSubSamples + contentCountContainers;
	}

	/**
	 * @throws IllegalArgumentException if record is not allowed to be located in this container
	 */
	public void assertCanStoreRecord(InventoryRecord record) {
		if (isWorkbench()) {
			return;
		}

		if (isDeleted()) {
			throw new IllegalArgumentException("Cannot move into deleted container");
		}
		if (record.isContainer()) {
			if (record.getId() != null && record.getId().equals(getId())) {
				throw new IllegalArgumentException("Cannot move container into itself");
			}
			if (hasRecordOnParentList((Container) record)) {
				throw new IllegalArgumentException("Cannot move container into its subcontainer");
			}
			Container container = (Container) record;
			if (container.isWorkbench()) {
				throw new IllegalArgumentException("Workbench cannot be moved into other container");
			}
		}

		if (!(record.isContainer() && canStoreContainers) && !(record.isSubSample() && canStoreSamples)) {
			throw new IllegalArgumentException("Container " + getGlobalIdentifier() + " can't hold record of type: " + record.getType());
		}
	}
	
	private boolean hasRecordOnParentList(Container record) {
		Container currParent = getParentContainer();
		while (currParent != null) {
			if (currParent.getId().equals(record.getId())) {
				return true;
			}
			currParent = currParent.getParentContainer();
		}
		return false;
	}

	public void removeStoredRecord(ContainerLocation location) {
		Validate.notNull(location);

		location.removeStoredRecord();
		resetLocationsAfterRecordRemoval(location);
		resetContentCount();
	}

	private void resetLocationsAfterRecordRemoval(ContainerLocation locationToRemove) {
		if (isImageLayoutContainer()) {
			return; /* for image container we keep defined locations permanently */ 
		}
		
		locations.removeIf(l -> l.getId() == locationToRemove.getId() && l.getStoredRecord() == null);
		resetListLayoutLocationCoords();
	}

	private void resetListLayoutLocationCoords() {
		if (isListLayoutContainer() || isWorkbench()) {
			for (int i = 0; i < locations.size(); i++) {
				locations.get(i).setCoordX(i+1);
			}
		}
	}

	public void configureAsGridLayoutContainer(int gridLayoutColumnsNumber, int gridLayoutRowsNumber) {
		this.gridLayoutColumnsNumber = gridLayoutColumnsNumber;
		this.gridLayoutRowsNumber = gridLayoutRowsNumber;
		
		if (gridLayoutColumnsNumber > 0 && gridLayoutRowsNumber > 0) {
			setContainerType(ContainerType.GRID);
			resetLocationsCount();
		}
	}

	@Transient
	public boolean isListLayoutContainer() {
		return ContainerType.LIST.equals(containerType);
	}

	@Transient
	public boolean isGridLayoutContainer() {
		return ContainerType.GRID.equals(containerType);
	}

	@Transient
	public boolean isImageLayoutContainer() {
		return ContainerType.IMAGE.equals(containerType);
	}
	
	@Transient
	public boolean isWorkbench() {
		return ContainerType.WORKBENCH.equals(containerType);
	}

	@Override
	protected void assertCanStoreAttachments() {
		if (isWorkbench()) {
			throw new IllegalArgumentException("Can't attach files to Workbench");
		}
	}
	
	@PrePersist
	@PreUpdate
	public void validateBeforeSave() {
		if (!canStoreContainers && !canStoreSamples) { 
			throw new ConstraintViolationException("Container cannot have both canStoreSamples and canStoreContainers set to false", null);
		}
		if (!canStoreContainers && !getStoredContainers().isEmpty()) {
			throw new ConstraintViolationException("Container has canStoreContainers set to false, but also has a stored container", null);
		}
		if (!canStoreSamples && !getStoredSubSamples().isEmpty()) {
			throw new ConstraintViolationException("Container has canStoreSamples set to false, but also has a stored subsample", null);
		}
	}

	@Override
	public Container copy(User currentUser) {
		return copy(this::defaultNameCopy, currentUser);
	}
	
	public Container copy(Function<Container, String> nameMapper, User currentUser) {
		Validate.isTrue(!isWorkbench(), "Workbench cannot be copied");
		
		Container copy = shallowCopy();
		copy.setContainerType(containerType);
		// images
		copy.setImageFileProperty(getImageFileProperty());
		copy.setThumbnailFileProperty(getThumbnailFileProperty());
		copy.setLocationsImageFileProperty(getLocationsImageFileProperty());
		copy.setName(nameMapper.apply(this));
		copy.setCreatedBy(currentUser.getUsername());
		copy.setModifiedBy(currentUser.getUsername());
		copy.setOwner(currentUser);
		
		// keep container type/design but will be empty.
		copy.setCanStoreSamples(canStoreSamples);
		copy.setCanStoreContainers(canStoreContainers);
		copy.setGridLayoutColumnsNumber(gridLayoutColumnsNumber);
		copy.setGridLayoutRowsNumber(gridLayoutRowsNumber);
		copy.setGridLayoutColumnsLabelType(gridLayoutColumnsLabelType);
		copy.setGridLayoutRowsLabelType(gridLayoutRowsLabelType);
		copy.setLocationsCount(locationsCount);
		if (isImageLayoutContainer()) {
			for (ContainerLocation loc: locations) {
				copy.createNewImageContainerLocation(loc.getCoordX(), loc.getCoordY());
			}
		}
		return copy;
	}
	
	Container shallowCopy() {
		Container copy = new Container(getContainerType());
		shallowCopyBasicFields(copy);
		return copy;
	}

}

