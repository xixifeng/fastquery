package org.fastquery.test;

import org.fastquery.bean.Bandit;
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
        assertThat(sql, equalTo(" and  ( id = ? and lastUpdateDateTime <= ? ) and name like ? order by id desc"));
        assertThat(params.toString(), equalTo("[7, 6789, 张三]"));
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
        assertThat(sql, equalTo(" and  ( name in (?) ) and name like ? order by id desc"));
        assertThat(params.toString(), equalTo("[null, 张三]"));
    }

    @Test
    public void conditions5()
    {
        Bandit bandit = new Bandit();

        builder(null, bandit);

        SQLValue sqlValue = bandit.getSqlValue();
        String sql = sqlValue.getSql();
        List<Object> params = sqlValue.getValues();
        assertThat(sql, equalTo(" and  ( name in (?) ) order by id desc"));
        assertThat(params.toString(), equalTo("[null]"));
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