package org.fastquery.bean;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.fastquery.bean.Student;
import org.fastquery.core.Prepared;
import org.fastquery.core.Primarykey;
import org.fastquery.example.StudentDBService;

public class StudentDBServiceProxyImpl implements StudentDBService {
	
	private static StudentDBService instance;

	private StudentDBServiceProxyImpl() {
	}

	public static StudentDBService getInstance() {
		if (instance == null) {
			instance = new StudentDBServiceProxyImpl();
		}
		return instance;
	}

	public JSONArray findAll() {
		return (JSONArray) Prepared.excute("findAll", "()Lcom/alibaba/fastjson/JSONArray;", new Object[0], this);
	}

	@Override
	public BigInteger saveToId(Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigInteger saveToId(Object entity, String dataSourceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigInteger saveToId(String dataSourceName, String dbName, Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <B> int save(boolean ignoreRepeat, Collection<B> entities) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int saveArray(boolean ignoreRepeat, Object... entities) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <B> int save(boolean ignoreRepeat, String dataSourceName, Collection<B> entities) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int saveArray(boolean ignoreRepeat, String dataSourceName, Object... entities) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <B> int save(boolean ignoreRepeat, String dataSourceName, String dbName, Collection<B> entities) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int saveArray(boolean ignoreRepeat, String dataSourceName, String dbName, Object... entities) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <E> E save(E entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> E save(E entity, String dataSourceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> E save(String dataSourceName, String dbName, E entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> E update(E entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> E update(String dataSourceName, E entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> E update(String dataSourceName, String dbName, E entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Object entity, String where) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(String dataSourceName, Object entity, String where) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(String dataSourceName, String dbName, Object entity, String where) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <E> E saveOrUpdate(E entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> E saveOrUpdate(String dataSourceName, E entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> E saveOrUpdate(String dataSourceName, String dbName, E entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void executeBatch(String sqlName, String output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void executeBatch(String sqlName, String output, String dataSourceName) {
		// TODO Auto-generated method stub

	}

	@Override
	public int update(String no, String name, int age) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(String no, int age) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Student[] find() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject findOne(String no) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int updateNameAndDept(String name, String dept, String no) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Student findStudent(String no) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists(String no) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Student findByNo(String no) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int count2() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public JSONObject rows() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int add(String no, String name, String sex, int age, String dept) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Student addStudent(String no, String name, String sex, int age, String dept) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int deleteByNo(String no) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Student[] findBySex(Integer age, String sex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONArray findBySex(String sex, Integer age) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> findBySex2(String sex, Integer age) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updatesx(Integer[] i1, Integer[] i2, Integer[] i3) {
		// TODO Auto-generated method stub

	}

	@Override
	public JSONArray findColumnKey(String table_name, String table_schema) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> addUserInfo(String name, Integer age) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> addUserInfo2(String name, Integer age) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Primarykey saveUserInfo(String name, Integer age) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject saveUserInfo2(String name, Integer age) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int saveUserInfo3(String name, Integer age) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public JSONObject updateUserinfoById(Integer age, int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteUserinfoById(int id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String findOneCourse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer findAgeByStudent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Student[] findAllStudent(String no, String name, Integer age, String dept1, String dept2, String dept3,
			String name2, Integer age2, Integer age3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Student> findAllStudent(String name, Integer age) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> findAges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> findNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Student> findSomeStudent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> findTop1Student() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void db() {
		// TODO Auto-generated method stub
	}

	@Override
	public JSONObject callProcedure(String no, String name, String sex, int age, String dept) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> int update(Collection<E> entities) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <E> int update(String dataSourceName, Collection<E> entities) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <E> int update(String dataSourceName, String dbName, Collection<E> entities) {
		// TODO Auto-generated method stub
		return 0;
	}

}
