package org.fastquery.bean.sunny;

import java.util.HashSet;
import java.util.Set;


public class Student {
	private int id;
	private String sname;
	
	private Card card;
	private Set<Teacher> teachers = new HashSet<Teacher>();
	private Set<ClassRoom> classRooms = new HashSet<ClassRoom>();
	
	public Student() {
	}

	public Student(String sname) {
		this.sname = sname;
	}

	public Student(int id, String sname) {
		this.id = id;
		this.sname = sname;
	}

	public int getId() {
		return id;
	}

	public String getSname() {
		return sname;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setSname(String sname) {
		this.sname = sname;
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public Set<Teacher> getTeachers() {
		return teachers;
	}

	public void setTeachers(Set<Teacher> teachers) {
		this.teachers = teachers;
	}
	
	//增加一条关联记录
	public void addTeacher(Teacher teacher) {
	   teachers.add(teacher);
	}
	//删除一条关联记录
	public void removeTeacher(Teacher teacher) {
	   if (teachers.contains(teacher)) {
	    teachers.remove(teacher);
	   }
	}

	public Set<ClassRoom> getClassRooms() {
		return classRooms;
	}
	
	// 增加一条与ClassRoom的关联记录
	public void addClassRoom(ClassRoom classRoom) {
		this.classRooms.add(classRoom);
	}
	
	
	// 删除一条与ClassRoom的关联记录
	public void removeClassRoom(ClassRoom classRoom) {
		if(classRooms.contains(classRoom)) {
			this.classRooms.remove(classRoom);
		}
	}
	

	public void setClassRooms(Set<ClassRoom> classRooms) {
		this.classRooms = classRooms;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Student other = (Student) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Student [id=" + id + ", sname=" + sname + "]";
	}

	
}
