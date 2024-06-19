package com.researchspace.model.record;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.researchspace.model.User;

/**
 * Keeps track of favorite records within a specific user.
 */
@Entity
@Table(name = "RecordUserFavorites", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "record_id", "user_id" }) })
public class RecordUserFavorites implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5187044429719979002L;
	private Long id;
	private User user;
	private BaseRecord record;

	@Id()
	@GeneratedValue(strategy = GenerationType.TABLE)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RecordUserFavorites(User user, BaseRecord record) {
		super();
		this.user = user;
		this.record = record;
	}

	/**
	 * For hibernate
	 */
	public RecordUserFavorites() {
	}

	@ManyToOne
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@ManyToOne
	public BaseRecord getRecord() {
		return record;
	}

	public void setRecord(BaseRecord record) {
		this.record = record;
	}
}
