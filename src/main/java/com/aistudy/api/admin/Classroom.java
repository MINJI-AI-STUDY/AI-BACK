package com.aistudy.api.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;
import jakarta.persistence.Table;

@Entity
@Table(name = "classrooms")
public class Classroom {
	@Id
	private String id;

	@Column(name = "school_id", nullable = false)
	private String schoolId;

	@Column(nullable = false)
	private String name;

	@Column
	private Integer grade;

	protected Classroom() {
	}

	public Classroom(String schoolId, String name, Integer grade) {
		this.id = UUID.randomUUID().toString();
		this.schoolId = schoolId;
		this.name = name;
		this.grade = grade;
	}

	public String getId() { return id; }
	public String getSchoolId() { return schoolId; }
	public String getName() { return name; }
	public Integer getGrade() { return grade; }

	public void update(String name, Integer grade) {
		this.name = name;
		this.grade = grade;
	}
}
