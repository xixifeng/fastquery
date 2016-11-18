package org.fastquery.bean.sunny;

public class Dog {
	private Integer id;
	private String name;
	private Gender gender = Gender.MALE;
	
	public Dog(){}
	
	public Dog(Integer id, String name, Gender gender) {
		this.id = id;
		this.name = name;
		this.gender = gender;
	}
	
	public Integer getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public Gender getGender() {
		return gender;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setGender(Gender gender) {
		this.gender = gender;
	}

	@Override
	public String toString() {
		return "Dog [id=" + id + ", name=" + name + ", gender=" + gender + "]";
	}
}
