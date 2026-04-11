package com.aistudy.api.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;
import jakarta.persistence.Table;

@Entity
@Table(name = "schools")
public class School {
	@Id
	private String id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private boolean active;

	protected School() {
	}

	public School(String id, String name, boolean active) {
		this.id = id;
		this.name = name;
		this.active = active;
	}

	public School(String name) {
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.active = true;
	}

	public String getId() { return id; }
	public String getName() { return name; }
	public boolean isActive() { return active; }

	public void update(String name, boolean active) {
		this.name = name;
		this.active = active;
	}
}
