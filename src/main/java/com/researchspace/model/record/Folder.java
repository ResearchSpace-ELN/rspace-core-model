package com.researchspace.model.record;

import static com.researchspace.model.core.RecordType.API_INBOX;
import static com.researchspace.model.core.RecordType.IMPORTS;
import static com.researchspace.model.core.RecordType.INDIVIDUAL_SHARED_FOLDER_ROOT;
import static com.researchspace.model.core.RecordType.SHARED_FOLDER;
import static com.researchspace.model.core.RecordType.SHARED_GROUP_FOLDER_ROOT;
import static com.researchspace.model.core.RecordType.TEMPLATE;
import static com.researchspace.model.record.StructuredDocument.MAX_TAG_LENGTH;
import static java.lang.String.format;

import com.researchspace.core.util.CollectionFilter;
import com.researchspace.core.util.MediaUtils;
import com.researchspace.model.AbstractUserOrGroupImpl;
import com.researchspace.model.TaggableElnRecord;
import com.researchspace.model.User;
import com.researchspace.model.audittrail.AuditDomain;
import com.researchspace.model.audittrail.AuditTrailData;
import com.researchspace.model.core.GlobalIdPrefix;
import com.researchspace.model.core.RecordType;
import com.researchspace.model.permissions.DefaultPermissionFactory;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

@Entity
@XmlRootElement
@Audited
@Indexed
@AuditTrailData(auditDomain = AuditDomain.FOLDER)
public class Folder extends BaseRecord implements TaggableElnRecord {
	
	public Folder() {
	}
	
	public Folder(ImportOverride override) {
		super(override);
	}

	/**
	 * Exports Gallery folder name
	 */
	public static final String EXPORTS_FOLDER_NAME = "PdfDocuments";

	/**
	 * Template Gallery folder name
	 */
	public static final String TEMPLATE_MEDIA_FOLDER_NAME = "Templates";

	/**
	 * Top-level gallery folder name
	 */
	public static final String MEDIAROOT = "Gallery";

	/**
	 * Example folder name
	 */
	public static final String EXAMPLES_FOLDER = "Examples";

	/**
	 * Snippets Gallery folder name
	 */
	public static final String SNIPPETS_FOLDER = "Snippets";

	/**
	 * Shared folder name for Gallery and Group folders
	 */
	public static final String SHARED_FOLDER_NAME = "Shared";
	
	/**
	 * API Inbox 
	 */
	public static final String API_INBOX_FOLDER_NAME = "Api Inbox";
	
	/**
	 * Imports Inbox 
	 */
	public static final String IMPORTS_INBOX_FOLDER_NAME = "Imports";

	/**
	 * Folder name for records shared between individuals
	 */
	public static final String INDIVIDUAL_SHARE_ITEMS_FLDER_NAME = "IndividualShareItems";

	/**
	 * Folder name for records shared amongst collaboration groups
	 */
	public static final String COLLABORATION_GROUPS_FLDER_NAME = "CollaborationGroups";

	/**
	 * Folder name for records shared amongst project groups
	 */
	public static final String PROJECT_GROUPS_FOLDER_NAME = "ProjectGroups";

	/**
	 * Folder name for records shared amongst lab groups
	 */
	public static final String LAB_GROUPS_FOLDER_NAME = "LabGroups";

	/**
	 * Default name for an unnamed folder.
	 */
	public static final String DEFAULT_FOLDER_NAME = "Untitled Folder";

	private boolean systemFolder = false;
	/**
	 * Generates a predicate testing whether a folder is Gallery top-level folder, e.g. one of Images, Documents etc.
	 * USeful if you want to test if a target media folder can accept the correct content.
	 * @param mediaTypeRootName  A GalleryContent type as returned by {@link MediaUtils#extractFileType(String fileSuffix)}
	 */
	public static Predicate<BaseRecord> targetFolderIsCorrectTypeForMedia(String mediaTypeRootName) {
		return folder->folder.hasType(RecordType.SYSTEM) &&
				mediaTypeRootName.equals(folder.getName()) &&
				 !folder.hasType(RecordType.ROOT);
	}

	/**
	 * Boolean test for whether this folder is a 'special' folder created by the
	 * system rather than by a user, that may have special behaviour associated
	 * with it; e.g., is a media, root or shared folder
	 */
	public boolean isSystemFolder() {
		return systemFolder;
	}

	/**
	 * Boolean test for whether this folder is the top-level Templates folder
	 */
	@Transient
	public boolean isTemplateFolder() {
		return systemFolder && TEMPLATE_MEDIA_FOLDER_NAME.equals(getName()) && hasType(TEMPLATE);
	}
	
	/**
	 * Boolean test for whether this folder is the API inbox folder
	 */
	@Transient
	public boolean isApiInboxFolder() {
		return systemFolder && API_INBOX_FOLDER_NAME.equals(getName()) && hasType(API_INBOX);
	}
	
	/**
	 * Boolean test for whether this folder is an additional content folder (Imports, API)
	 */
	@Transient
	public boolean isImportedContentFolder() {
		return isApiInboxFolder() || isImportsFolder();
	}
	
	/**
	 * Boolean test for whether this folder is the API inbox folder
	 */
	@Transient
	public boolean isImportsFolder() {
		return systemFolder && IMPORTS_INBOX_FOLDER_NAME.equals(getName()) && hasType(IMPORTS);
	}

    /**
     * Boolean test for whether this folder is the top-level Shared folder
     * 
     */
    @Transient
    public boolean isTopLevelSharedFolder() {
        return systemFolder && SHARED_FOLDER_NAME.equals(getName());
    }

	/**
	 * Boolean test for whether this folder is Shared folder, or its sub folder
	 */
	@Transient
	public boolean isSharedFolder() {
		return hasType(SHARED_FOLDER) || hasType(INDIVIDUAL_SHARED_FOLDER_ROOT)
				|| hasType(SHARED_GROUP_FOLDER_ROOT);
	}

	public void setSystemFolder(boolean systemFolder) {
		this.systemFolder = systemFolder;
	}

	private static final long serialVersionUID = 7810039088384043137L;

	@Setter(AccessLevel.PACKAGE)
	private Set<RecordToFolder> children = new HashSet<>();

	/*
	 * For hibernate
	 */
	@NotAudited
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "folder", orphanRemoval = true)
	public Set<RecordToFolder> getChildren() {
		return children;
	}

	@Setter
	private String docTag;
	@Setter
	private String tagMetaData;

	@Column(length = MAX_TAG_LENGTH)
	@Size(max = MAX_TAG_LENGTH)
	@Field
	public String getDocTag() {
		return docTag;
	}

	@Lob
	public String getTagMetaData() {
		return tagMetaData;
	}
	
	// checks if existing parents are child.
	private boolean checkParents(Folder curr, BaseRecord child, Stack<BaseRecord> stack) {

		CycleSafeIterator it = new CycleSafeIterator(curr);
		while (it.hasNext()) {
			BaseRecord br = it.next();
			// if (br == null) { // findbugs complains about this .. can next()
			// not return null?
			// return false;
			// } else
			if (br.equals(child)) {
				return false;
			}
		}
		return !it.isCycleDetected();
	}

	/**
	 * 
	 * @param child
	 * @param policy
	 * @param owner
	 * @param aclPolicy
	 * @throws IllegalAddChildOperation
	 * @see #addChild(BaseRecord child, User owner)
	 * @throws IllegalAddChildOperation
	 *             if adding this child will create a cycle..
	 */
	public RecordToFolder addChild(BaseRecord child, ChildAddPolicy policy, User owner, ACLPropagationPolicy aclPolicy)
			throws IllegalAddChildOperation {

		System.out.println("addChild0: " + Instant.now());
		// no self edges
		if (this.equals(child)) {
			throw new IllegalAddChildOperation("Cannot add this object [" + this + "] as a child of itself.");
		}
		if (!policy.canAdd(this, child)) {
			throw new IllegalAddChildOperation(format("ChildAddPolicy [%s] does not permit child addition of ["+
					"%s] to  folder[%s]", policy, child, this));
		}
		// no cycles for cycles
		if (!checkParents(this, child, new Stack<>())) {
			throw new IllegalAddChildOperation(format("Addition of [%s] to  folder[%s] "+
		    " would produce a cycle in the folder tree, which currently is not permitted.", 
					child.getName(),this.getName() ));
		}

		System.out.println("addChild1: " + Instant.now());
		RecordToFolder newedge = doAdd(child, owner);
		System.out.println("addChild2: " + Instant.now());
		if (newedge != null) {
			postProcess(child, owner, newedge, aclPolicy);
		}
		return newedge;
	}

	/*
	 * Performs any actions required after the parent-child relations are
	 * established.
	 */
	void postProcess(BaseRecord child, User owner, RecordToFolder newedge, ACLPropagationPolicy aclPolicy) {
		if (isTemplateFolder()) {
			new DefaultPermissionFactory().setUpACLForTemplateFolderChildPermissions(child, getOwner());
		} else if (isImportedContentFolder()) {
			new DefaultPermissionFactory().setUpAclForInboxFolderChildPermissions(child, getOwner());
		}
		aclPolicy.onAdd(this, child);
		updateRecordTypeIfAddingSubfolderToSharedFolder(child);
		
	}

	private void updateRecordTypeIfAddingSubfolderToSharedFolder(BaseRecord child) {
		if (child.isFolder()) {
			// propagate the fact a folder is shared, if it is being added
			if (!child.hasType(RecordType.NOTEBOOK)
					&& (this.hasType(RecordType.SHARED_GROUP_FOLDER_ROOT) || this.hasType(RecordType.SHARED_FOLDER))) {
				child.addType(RecordType.SHARED_FOLDER);
			}
		}
	}

	/*
	 * Package scoped for testing; do not use externally; use addChild
	 */
	RecordToFolder doAdd(BaseRecord child, User owner) {
		RecordToFolder newedge = new RecordToFolder(child, this, owner.getUsername());

		boolean addedP = child.parents.add(newedge);
		if (!addedP) {
			return null;
		}
		boolean addedC = children.add(newedge);
		if (addedC && addedP) {
			return newedge;
		}
		return null;
	}

	/**
	 * Adds the child record to this folder, equivalent to addChild (child,
	 * ChildAddPolicy.DEFAULT, owner,ACLPropagationPolicy.DEFAULT_POLICY). There
	 * are some business rules governing what can be added.
	 * <ul>
	 * <li>child cannot be <code>this</code> folder
	 * <li>child cannot be an ancestor of this folder
	 * <li>If this relationship already exists, <code>null</code> is returned
	 * </ul>
	 * If more exclusion criteria are needed, use the overloaded method that
	 * takes a {@link ChildAddPolicy} that can further refine what is allowed to
	 * be added.
	 * 
	 * @param child
	 * @param owner
	 * @return the RecordToFolder relationship, or <code>null</code> if it could
	 *         not be created.
	 * @throws IllegalAddChildOperation
	 */
	public RecordToFolder addChild(BaseRecord child, User owner) throws IllegalAddChildOperation {
		return addChild(child, ChildAddPolicy.DEFAULT, owner, ACLPropagationPolicy.DEFAULT_POLICY);
	}

	public boolean removeChild(BaseRecord child, ACLPropagationPolicy aclPolicy) {
		System.out.println("removeChild1: " + Instant.now());
		RecordToFolder toRemove = findRecordInChildRelations(child);
		System.out.println("removeChild2: " + Instant.now());
		if (toRemove != null) {
			boolean removed1 = children.remove(toRemove);
			boolean removed2 = child.getParents().remove(toRemove);
			boolean removedOK = removed1 && removed2;
			if (removedOK) {
				aclPolicy.onRemove(this, child);
				return true;
			}
		}
		System.out.println("removeChild3: " + Instant.now());
		return false;
	}

	/**
	 * Standard way to remove a child from the object graph of this folder.
	 * 
	 * @param child
	 * @return
	 */
	public boolean removeChild(BaseRecord child) {
		System.out.println("removeChild0: " + Instant.now());
		return removeChild(child, ACLPropagationPolicy.DEFAULT_POLICY);
	}

	RecordToFolder findRecordInChildRelations(BaseRecord child) {
		System.out.println("findRecordInChildRelations0: " + Instant.now());
/*
		children.size();
		System.out.println("findRecordInChildRelations-afterGetChildren): " + Instant.now());
*/

		RecordToFolder toRemove = null;
		for (RecordToFolder reln : children) {
			if (reln.getRecord().getId().equals(child.getId())) {
				toRemove = reln;
				break;
			}
		}
		System.out.println("findRecordInChildRelations01: " + Instant.now() + " id: " + toRemove.getId());
		return toRemove;
	}

	/**
	 * Boolean test as to whether the relationship between this Folder and the
	 * argument record is marked as deleted or not.
	 * 
	 * @param r
	 *            A direct child of this folder
	 * @return <code>true</code> if <code>child</code> is marked as deleted,
	 *         <code>false</code> otherwise.
	 */
	@Transient
	public boolean isMarkedDeleted(BaseRecord r) {
		for (RecordToFolder rtf : getChildren()) {
			if (rtf.getRecord().equals(r)) {
				return rtf.isRecordInFolderDeleted();
			}
		}
		return false;
	}

	/**
	 * Visitor-pattern method that iterates over the folder tree, calling the
	 * <em>process()</em> method of the supplied
	 * {@link RecordContainerProcessor} on each record and folder.
	 * 
	 * @param processor
	 *            A non-null RecordContainerProcessor.
	 */
	public void process(RecordContainerProcessor processor) {
		if (processor == null) {
			throw new IllegalArgumentException("processor was null!");
		}
		boolean toContinue = processor.process(this);
		if (!toContinue) {
			return;
		}
		for (RecordToFolder rtf : getChildren()) {
			if (!rtf.getRecord().isFolder()) {
				processor.process(rtf.getRecord());
			}
		}
		for (BaseRecord child : getChildrens()) {
			if (child.isFolder()) {
				((Folder) child).process(processor);
			}
		}
	}

	/**
	 * Gets children records / subfolders of this folder.
	 */
	@Transient
	public Set<BaseRecord> getChildrens() {
		return getChildrens(DEFAULT_FILTER);
	}

	/**
	 * Gets child records filtered by a {@link CollectionFilter}
	 * 
	 * @param rf
	 */
	@Transient
	public Set<BaseRecord> getChildrens(CollectionFilter<BaseRecord> rf) {
		Set<BaseRecord> rc = new HashSet<>();
		for (RecordToFolder rel : children) {
			if (rf != null && rf.filter(rel.getRecord())) {
				rc.add(rel.getRecord());
			} else if (rf == null) {
				rc.add(rel.getRecord());
			}
		}
		return rc;
	}

	/**
	 * Convenience method to return only subfolders of this folder, ignoring any
	 * records as children.
	 * 
	 * @return a non-null {@link Set} of subfolders
	 */
	@Transient
	public Set<Folder> getSubfolders() {
		Set<Folder> rc = new HashSet<>();
		for (RecordToFolder rel : children) {
			if (rel.getRecord().isFolder()) {
				rc.add((Folder) rel.getRecord());
			}
		}
		return rc;
	}

	/**
	 * Recursively copies this folder and all its subfolders and records to
	 * generate a complete copy.
	 * 
	 * @param user
	 *            the authenticated user performing the copy operation
	 * @param recursiveCopyChildren
	 *            whether or not to recurse into subfolders
	 * @return The newly copied Folder.
	 * @throws IllegalAddChildOperation
	 */
	public Folder copy(User user, boolean recursiveCopyChildren) throws IllegalAddChildOperation {
		Folder clone = new Folder();
		copyBasicFolderFields(clone);
		doCopy(user, recursiveCopyChildren, clone);
		return clone;
	}

	protected void copyBasicFolderFields(Folder clone) {
		clone.setDocTag(getDocTag());
		clone.setTagMetaData(getTagMetaData());
	}

	protected void doCopy(User user, boolean recursiveCopyChildren, Folder clone) throws IllegalAddChildOperation {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.shallowCopyRecordInfo(clone);
		// gen.generateId(clone);
		if (recursiveCopyChildren) {
			for (RecordToFolder r : getChildren()) {
				if (r.getRecord().isFolder()) {
					Folder f = ((Folder) r.getRecord()).copy(user, true);
					// gen.generateId(f);
					clone.addChild(f, user);
				} else {
					BaseRecord r2 = r.getRecord().copy();
					// gen.generateId(r2);
					clone.addChild(r2, user);
				}
			}
		}
	}

	@Transient
	// @SearchableProperty
	public String getIndexContent() {
		return this.getName();
	}

	@Transient
	@Override
	public boolean isFolder() {
		return true;
	}

	/**
	 * Returns <code>true</code> if this folder is a user's root (home) folder;
	 * <code>false</code> otherwise.
	 */
	@Transient
	public boolean isRootFolder() {
		return hasType(RecordType.ROOT);
	}

	/**
	 * Convenience method for whether this folder is the root folder for the
	 * specified user. This will return <code>true</code> if
	 * <ul>
	 * <li>The folder is of type {@link RecordType#ROOT}
	 * <li>The folder is owned by the user
	 * </ul>
	 * 
	 * @param userOrGroup
	 */
	@Transient
	public boolean isRootFolderForUser(AbstractUserOrGroupImpl userOrGroup) {
		return isRootFolder() && getOwner().getUniqueName().equals(userOrGroup.getUniqueName());
	}

	/**
	 * Overloaded method that adds a record to user's 'Shared' Folder if
	 * possible, otherwise to their root folder using the specified
	 * {@link ACLPropagationPolicy}.
	 * 
	 * @param record
	 * @param sharee
	 * @param propagationPolicy
	 * @return The Folder that the records were added to.
	 * @throws IllegalAddChildOperation
	 */
	public Folder addRecordToUsersSharedFolder(BaseRecord record, User sharee, ACLPropagationPolicy propagationPolicy)
			throws IllegalAddChildOperation {
		Folder parent = this;

		if (parent != null && !parent.isRootFolder()) {
			RSPath records = getParentHierarchyForUser(getOwner());
			parent = (Folder) records.getFirstElement().orElseThrow(()->new IllegalStateException("No root folder!"));
		}
	
		Folder toAdd = null;
		toAdd = parent.getSubFolderByName(SHARED_FOLDER_NAME);
		if (toAdd == null) {
			toAdd = parent;
		}
		toAdd.addChild(record, ChildAddPolicy.DEFAULT, sharee, propagationPolicy);
		return toAdd;
	}

	/**
	 * Adds a record to user's 'Shared' Folder if possible, otherwise to their
	 * root folder, using the default {@link ACLPropagationPolicy}
	 * 
	 * @param record
	 * @param sharee
	 * @return The Folder that the records were added to.
	 * @throws IllegalAddChildOperation
	 */
	public Folder addRecordToUsersSharedFolder(BaseRecord record, User sharee) throws IllegalAddChildOperation {
		return addRecordToUsersSharedFolder(record, sharee, ACLPropagationPolicy.DEFAULT_POLICY);
	}

	@Override
	public String toString() {
		return "Folder [ editInfo=" + getEditInfo() + ", deleted=" + deleted + "]";
	}

	@Transient
	public String getRecordInfoType() {
		return isSystemFolder() ? DOCUMENT_CATEGORIES.SYSTEM_FOLDER : DOCUMENT_CATEGORIES.FOLDER;
	}

	/**
	 * 
	 */
	public void setAllSubfoldersAndRecordsDeleted() {
		setDeleted(true);
		for (RecordToFolder rtf : children) {
			rtf.setRecordInFolderDeleted(true);
			if (getOwner().isOwnerOfRecord(rtf.getRecord())) {
				rtf.getRecord().setDeleted(true);
			}
		}
	}

	/**
	 * Gets an immediate subfolder by name, or <code>null</code> if not found.
	 * 
	 * @param name
	 */
	@Transient
	public Folder getSubFolderByName(String name) {
		for (BaseRecord child : getChildrens()) {
			if (child.isFolder() && child.getName().equals(name)) {
				return (Folder) child;
			}
		}
		return null;
	}

	/**
	 * Don't use this - use copy(User, IDGenerator) instead
	 * 
	 * @deprecated
	 */
	@Override
	@Deprecated
	public BaseRecord copy() {
		return null;
	}

	@Override
	@Transient
	protected GlobalIdPrefix getGlobalIdPrefix() {
		boolean galleryFolder = hasAncestorOfType(RecordType.ROOT_MEDIA, true);
		return galleryFolder ? GlobalIdPrefix.GF : GlobalIdPrefix.FL;
	}

	public RecordInformation toRecordInfo() {
		RecordInformation info = super.toRecordInfo();
		info.addType(getRecordInfoType());
		return info;
	}

	/**
	 * Clears all ACLs from this Folder. If <code>recurse == true</code> then
	 * descendants will also have their ACLs removed.
	 */
	public void clearACL(boolean recurse) {
		super.clearACL(recurse);
		if (recurse) {
			for (BaseRecord child : getChildrens()) {
				child.clearACL(true);
			}
		}
	}

}
