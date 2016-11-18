package org.fastquery.bean.sunny;

import org.fastquery.core.Id;

public class Card {

	@Id
	private Integer id;
	private String number;
	
	private Teacher teacher;
	private Student student;
	
	public Card() {
	}

	public Card(String number) {
		this.number = number;
	}

	public Card(int id, String number) {
		super();
		this.id = id;
		this.number = number;
	}

	public Integer getId() {
		return id;
	}

	public String getNumber() {
		return number;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}
	
}
