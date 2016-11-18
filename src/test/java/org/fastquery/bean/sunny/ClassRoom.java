package org.fastquery.bean.sunny;

import java.util.HashSet;
import java.util.Set;

public class ClassRoom {

	private int id;
	private String cname;
	
	private Set<Teacher> teachers = new HashSet<Teacher>();
	private Set<Student> students = new HashSet<Student>();
	private Set<Desk> desks = new HashSet<Desk>();
	
	
	public ClassRoom() {
	}
	
	

	public ClassRoom(String cname) {
		this.cname = cname;
	}



	public ClassRoom(int id, String cname) {
		this.id = id;
		this.cname = cname;
	}

	public int getId() {
		return id;
	}

	public String getCname() {
		return cname;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setCname(String cname) {
		this.cname = cname;
	}
	
	public Set<Teacher> getTeachers() {
		return teachers;
	}

	public void setTeachers(Set<Teacher> teachers) {
		this.teachers = teachers;
	}

	public Set<Student> getStudents() {
		return students;
	}

	public void setStudents(Set<Student> students) {
		this.students = students;
	}

	public Set<Desk> getDesks() {
		return desks;
	}

	public void setDesks(Set<Desk> desks) {
		this.desks = desks;
	}

	// 提供增加一个桌子的方法
	public void addDesk(Desk desk) {
		desk.setClassRoom(this); // 建立关系
		this.desks.add(desk);
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
		ClassRoom other = (ClassRoom) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ClassRoom [id=" + id + ", cname=" + cname + "]";
	}
	
	

	
		
}
