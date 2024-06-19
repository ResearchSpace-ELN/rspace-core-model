package com.researchspace.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.xml.bind.annotation.XmlElement;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Generic binary data blob object that can be cached.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageBlob implements Serializable {

	private static final long serialVersionUID = 8446877595238278739L;
	private Long id;
	private byte[] data;

	/**
	 * Client constructor
	 * 
	 * @param data
	 */
	public ImageBlob(byte[] data) {
		this.data = data;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	void setId(Long id) {
		this.id = id;
	}

	@Lob
	@XmlElement
	public byte[] getData() {
		return data == null ? null : data.clone();
	}

	public void setData(byte[] data) {
		this.data = data == null ? null : data.clone();
	}

}
