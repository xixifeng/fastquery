package org.fastquery.bean.sunny;

import java.util.HashSet;
import java.util.Set;

public class Teacher {
	
	private int id;
	private String tname;
	
	private Card card;
	private Set<Student> students = new HashSet<Student>();
	private Set<ClassRoom> classRooms = new HashSet<ClassRoom>();
	
	public Teacher() {
	}

	public Teacher(String tname) {
		this.tname = tname;
	}

	public int getId() {
		return id;
	}

	public String getTname() {
		return tname;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setTname(String tname) {
		this.tname = tname;
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public Set<Student> getStudents() {
		return students;
	}

	public void setStudents(Set<Student> students) {
		this.students = students;
	}

	public Set<ClassRoom> getClassRooms() {
		return classRooms;
	}
	
	// 增加一条与ClassRoom的关系
	public void addClassRoom(ClassRoom classRoom) {
		this.classRooms.add(classRoom);
	}
	// 删除一条与ClassRoom的关联
	public void removeClassRoom(ClassRoom classRoom) {
		if( classRooms.contains(classRoom) ) {
			classRooms.remove(classRoom);
		}
	}

	public void setClassRooms(Set<ClassRoom> classRooms) {
		this.classRooms = classRooms;
	}

	@Override
	public String toString() {
		return "Teacher [id=" + id + ", tname=" + tname + "]";
	}
	
	
	
	
}
