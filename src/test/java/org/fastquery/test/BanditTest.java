package org.fastquery.test;

import lombok.extern.slf4j.Slf4j;
import org.fastquery.bean.Bandit;
import org.fastquery.struct.NulOperator;
import org.fastquery.struct.SQLOperator;
import org.fastquery.struct.SQLValue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


/**
 * @author xixifeng (fastquery@126.com)
 */

@Slf4j
public class BanditTest
{

    @Test
    public void conditions1()
    {
        List<String> names = new ArrayList<>();
        names.add("xx");
        names.add(null);
        names.add("yy");
        names.add("zz");

        Bandit bandit = new Bandit();
        bandit.setId(7L);
        bandit.setName("张三");
        bandit.setCreateDateTime(1234L);
        bandit.setLastUpdateDateTime(6789L);

        builder(names, bandit);

        SQLValue sqlValue = bandit.getSqlValue();
        String sql = sqlValue.getSql();
        List<Object> params = sqlValue.getValues();
        assertThat(sql, equalTo(" and  ( id = ? and createDateTime >= ? and lastUpdateDateTime <= ? and name in (?,?,?) ) and name like ? order by id desc"));
        assertThat(params.toString(), equalTo("[7, 1234, 6789, xx, yy, zz, 张三]"));
    }

    @Test
    public void conditions2()
    {
        List<String> names = new ArrayList<>();
        names.add(null);
        names.add(null);
        names.add(null);

        Bandit bandit = new Bandit();
        bandit.setId(7L);
        bandit.setName("张三");
        bandit.setLastUpdateDateTime(6789L);

        builder(names, bandit);

        SQLValue sqlValue = bandit.getSqlValue();
        String sql = sqlValue.getSql();
        List<Object> params = sqlValue.getValues();
        assertThat(sql, equalTo(" and  ( id = ? and lastUpdateDateTime <= ? and name in (?) ) and name like ? order by id desc"));
        assertThat(params.toString(), equalTo("[7, 6789, null, 张三]"));
    }

    @Test
    public void conditions3()
    {
        List<String> names = new ArrayList<>();

        Bandit bandit = new Bandit();
        bandit.setId(7L);
        bandit.setName("张三");

        builder(names, bandit);

        SQLValue sqlValue = bandit.getSqlValue();
        String sql = sqlValue.getSql();
        List<Object> params = sqlValue.getValues();
        assertThat(sql, equalTo(" and  ( id = ? and name in (?) ) and name like ? order by id desc"));
        assertThat(params.toString(), equalTo("[7, null, 张三]"));
    }

    @Test
    public void conditions4()
    {
        Bandit bandit = new Bandit();
        bandit.setName("张三");

        builder(null, bandit);

        SQLValue sqlValue = bandit.getSqlValue();
        String sql = sqlValue.getSql();
        List<Object> params = sqlValue.getValues();
        assertThat(sql, equalTo(" and name like ? order by id desc"));
        assertThat(params.toString(), equalTo("[张三]"));
    }

    @Test
    public void conditions5()
    {
        Bandit bandit = new Bandit();

        builder(null, bandit);

        SQLValue sqlValue = bandit.getSqlValue();
        String sql = sqlValue.getSql();
        List<Object> params = sqlValue.getValues();
        assertThat(sql, equalTo(" order by id desc"));
        assertThat(params.isEmpty(), equalTo(true));
    }

    @Test
    public void conditions6()
    {
        Bandit bandit = new Bandit();

        bandit.orderBy();

        SQLValue sqlValue = bandit.getSqlValue();
        String sql = sqlValue.getSql();
        List<Object> params = sqlValue.getValues();
        assertThat(sql, equalTo(" order by id desc"));
        assertThat(params.toString(), equalTo("[]"));
    }

    @Test
    public void conditions7()
    {
        Bandit bandit = new Bandit();
        bandit.setId(18L);

        bandit.and(bandit::id, SQLOperator.EQ, bandit.getId());
        bandit.orderBy();

        SQLValue sqlValue = bandit.getSqlValue();
        String sql = sqlValue.getSql();
        List<Object> params = sqlValue.getValues();
        assertThat(sql, equalTo(" and id = ? order by id desc"));
        assertThat(params.toString(), equalTo("[18]"));
    }

    @Test
    public void conditions8()
    {
        Bandit bandit = new Bandit();
        bandit.setId(18L);

        bandit.and(bandit::name, NulOperator.ISNULL);
        bandit.and(Bandit::age, SQLOperator.IN, 3);
        bandit.and(bandit::id, SQLOperator.EQ, bandit.getId());
        bandit.orderBy();

        SQLValue sqlValue = bandit.getSqlValue();
        String sql = sqlValue.getSql();
        List<Object> params = sqlValue.getValues();
        assertThat(params.toString(), equalTo("[3, 18]"));
        assertThat(sql, equalTo(" and name is null and age in (?) and id = ? order by id desc"));
    }

    @Test
    public void conditions9()
    {
        Bandit bandit = new Bandit();

        bandit.increment(Bandit::age, -3).increment(bandit::id, 16L);
        bandit.set(bandit::sort, 7).set(bandit::createDateTime, 123456L);
        bandit.and(bandit::name, NulOperator.ISNULL);
        bandit.and(bandit::id, SQLOperator.EQ, 30L);
        bandit.finish();

        SQLValue sqlValue = bandit.getSqlValue();
        String sql = sqlValue.getSql();
        assertThat(sql,equalTo(" set age=age+-3,id=id+16,sort=?,createDateTime=? where name is null and id = ?"));
        List<Object> values = sqlValue.getValues();
        assertThat(values.size(),is(3));
        assertThat(values.get(0),is(7));
        assertThat(values.get(1),is(123456L));
        assertThat(values.get(2),is(30L));
    }

    private void builder(List<String> names, Bandit bandit)
    {
        bandit
                .and(e -> e
                        .and(bandit::id, SQLOperator.EQ, bandit.getId())
                        .and(bandit::createDateTime, SQLOperator.GE, bandit.getCreateDateTime())
                        .and(bandit::lastUpdateDateTime, SQLOperator.LE, bandit.getLastUpdateDateTime())
                        .and(bandit::name, SQLOperator.IN, names)
                )

                .and(bandit::name, SQLOperator.LIKE, bandit.getName())
                .orderBy();
    }

}