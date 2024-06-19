package com.researchspace.model.views;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A simple POJO to hold some information about Groups for display in the UI
 */
@Getter
@Setter
@EqualsAndHashCode(of= {"displayName", "id"})
@ToString(of= {"displayName", "id"})
public class GroupListResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8208692397022688326L;
	private Long id;
	private String displayName;
	private String piFullname;
	private String piAffiliation;
	/**
	 * Member count in group
	 */
	private Integer groupSize = 0;
	
	/**
	 * Syntax for field values from autocomplete box
	 */
	public static final Pattern INPUT_VALUE_ITEM = Pattern
			.compile(",?\\s*(.+?)\\s*<(\\d+)>");
	
	/**
	 * 
	 */
	public static final Pattern ALL = Pattern
			.compile("(,?\\s*(.+?)\\s*<(\\d+)>)+,?");

	public static Set<Long> getGroupIdsfromMultiGroupAutocomplete(String input) {
		Set<Long> rc = new TreeSet<>();
		if (input == null) {
			return rc;
		}
		Matcher m = INPUT_VALUE_ITEM.matcher(input.trim());
		while (m.find()) {
			rc.add(Long.parseLong(m.group(2)));
		}
		return rc;
	}

	public static boolean validateMultiGroupAutocompleteInput(String input) {
		if (input == null) {
			return false;
		}
		Matcher m = ALL.matcher(input.trim());
		return m.matches();
	}

	public GroupListResult(Long id, String displayName) {
		super();
		this.id = id;
		this.displayName = displayName;
	}

}
