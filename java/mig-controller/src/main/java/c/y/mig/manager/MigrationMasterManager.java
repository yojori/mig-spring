package c.y.mig.manager;

import c.y.mig.db.DBManager;
import c.y.mig.db.query.*;
import c.y.mig.model.MigrationMaster;
import c.y.mig.model.Search;
import c.y.mig.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MigrationMasterManager extends Manager {

    private static final Logger log = LoggerFactory.getLogger(MigrationMasterManager.class);

    private void buildListQuery(MigrationMaster master) {
        Select sql = new Select();
        sql.addField("master_code");
        sql.addField("master_name");
        sql.addField("create_date");
        sql.addField("update_date");
        sql.addField("display_yn");
        sql.addField("ordering");

        sql.addFrom(MIGRATION_MASTER);

        if (!StringUtil.empty(master.getDisplay_yn())) {
            sql.addWhere("display_yn = ?", master.getDisplay_yn());
        }
        sql.addOrder("ordering desc");
        setListQuery(sql);
    }

    private void buildCountQuery(MigrationMaster master) {
        Select sql = new Select();
        sql.addField("COUNT(master_code)");
        sql.addFrom(MIGRATION_MASTER);
        if (!StringUtil.empty(master.getDisplay_yn())) {
            sql.addWhere("display_yn = ?", master.getDisplay_yn());
        }
        setCountQuery(sql);
    }

    public List<MigrationMaster> getList(MigrationMaster master, int PAGE_GUBUN) {
        List<MigrationMaster> list = new ArrayList<MigrationMaster>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBManager.getConnection();
            setForm(master);
            setPageGubun(PAGE_GUBUN);
            buildCountQuery(master);
            buildListQuery(master);

            if (getPageGubun() == InterfaceManager.PAGE) {
                stmt = con.prepareStatement(getCountQuery().toQuery());
                setParameter(getCountQuery(), stmt);
                rs = stmt.executeQuery();
                if (rs.next())
                    master.setTotalCount(rs.getInt(1));
                DBManager.close(rs, stmt, null);
            }

            if (master.getPageSize() > 0) {
                stmt = con.prepareStatement(getListQueryString());
                setParameter(getListQuery(), stmt, getPageGubun());
                rs = stmt.executeQuery();
                int i = 0;
                MigrationMaster entity = null;
                while (rs.next()) {
                    entity = new MigrationMaster();
                    entity.setListIndex(
                            ((master.getTotalCount() - ((master.getCurrentPage() - 1) * master.getPageSize())) - i));
                    entity.setMaster_code(rs.getString("master_code"));
                    entity.setMaster_name(rs.getString("master_name"));
                    entity.setCreate_date(rs.getDate("create_date"));
                    entity.setDisplay_yn(rs.getString("display_yn"));
                    entity.setOrdering(rs.getInt("ordering"));
                    list.add(entity);
                    i++;
                }
            }
        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(rs, stmt, con);
        }
        return list;
    }

    public MigrationMaster find(MigrationMaster master) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBManager.getConnection();
            Select select = new Select();
            select.addField("*");
            select.addFrom(MIGRATION_MASTER);
            select.addWhere("master_code = ?", master.getMaster_code());

            stmt = con.prepareStatement(select.toQuery());
            setParameter(select, stmt);
            rs = stmt.executeQuery();

            if (rs.next()) {
                master.setMaster_name(rs.getString("master_name"));
                master.setCreate_date(rs.getDate("create_date"));
                master.setUpdate_date(rs.getDate("update_date"));
                master.setDisplay_yn(rs.getString("display_yn"));
                master.setOrdering(rs.getInt("ordering"));
            } else {
                master = null;
            }
        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(rs, stmt, con);
        }
        return master;
    }

    public int insert(MigrationMaster master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            Insert sql = new Insert();
            sql.addField("master_code", master.getMaster_code());
            sql.addField("master_name", master.getMaster_name());
            sql.addField("display_yn", master.getDisplay_yn());
            sql.addField("create_date", master.getCreate_date());
            sql.addField("update_date", master.getUpdate_date());
            sql.addField("ordering", master.getOrdering());
            sql.addFrom(MIGRATION_MASTER);

            con = DBManager.getConnection();
            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rtn = stmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(null, stmt, con);
        }
        return rtn;
    }

    public int update(MigrationMaster master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            Update sql = new Update();
            sql.addField("master_name", master.getMaster_name());
            sql.addField("display_yn", master.getDisplay_yn());
            sql.addField("update_date", master.getUpdate_date());
            sql.addField("ordering", master.getOrdering());

            sql.addFrom(MIGRATION_MASTER);
            sql.addWhere("master_code = ", master.getMaster_code());

            con = DBManager.getConnection();
            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rtn = stmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(null, stmt, con);
        }
        return rtn;
    }
}
