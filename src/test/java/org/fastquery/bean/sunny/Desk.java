package org.fastquery.bean.sunny;

public class Desk {

	private int id;
	private String dname;
	
	private ClassRoom classRoom;

	public Desk() {
	}
	
	public Desk(int id, String dname) {
		this.id = id;
		this.dname = dname;
	}

	public int getId() {
		return id;
	}

	public String getDname() {
		return dname;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setDname(String dname) {
		this.dname = dname;
	}

	public ClassRoom getClassRoom() {
		return classRoom;
	}

	public void setClassRoom(ClassRoom classRoom) {
		this.classRoom = classRoom;
	}

	@Override
	public String toString() {
		return "Desk [id=" + id + ", dname=" + dname + "]";
	}
	
	

	
	
}
