package com.yojori.manager;

import com.yojori.db.DBManager;
import com.yojori.db.query.*;
import com.yojori.migration.controller.model.DBConnMaster;
import com.yojori.migration.controller.model.MigrationList;
import com.yojori.migration.controller.model.MigrationSchema;
import com.yojori.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class DBConnMasterManager extends Manager {

    private void setListQuery(DBConnMaster master) {
        Select sql = new Select();
        sql.addField("master_code");
        sql.addField("character_set");
        sql.addField("create_date");
        sql.addField("update_date");
        sql.addField("db_type");
        sql.addField("class_name"); // driverClass
        sql.addField("url");        // jdbcUrl
        sql.addField("user_id");    // username
        sql.addField("user_pswd");  // password
        sql.addFrom(DB_MASTER);
        sql.addOrder("create_date desc");
        setListQuery(sql);
    }

    private void setCountQuery(DBConnMaster master) {
        Select sql = new Select();
        sql.addField("COUNT(master_code)");
        sql.addFrom(DB_MASTER);
        setCountQuery(sql);
    }

    public List<DBConnMaster> getList(DBConnMaster master, int PAGE_GUBUN) {
        List<DBConnMaster> list = new ArrayList<DBConnMaster>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBManager.getConnection();
            setForm(master);
            setPageGubun(PAGE_GUBUN);
            setCountQuery(master);
            setListQuery(master);

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
                DBConnMaster entity = null;
                while (rs.next()) {
                    entity = new DBConnMaster();
                    entity.setListIndex(
                            ((master.getTotalCount() - ((master.getCurrentPage() - 1) * master.getPageSize())) - i));
                    entity.setMaster_code(rs.getString("master_code"));
                    entity.setCharacter_set(rs.getString("character_set"));
                    entity.setDb_type(rs.getString("db_type"));
                    entity.setDriverClass(rs.getString("class_name"));
                    entity.setJdbcUrl(rs.getString("url"));
                    entity.setUsername(rs.getString("user_id"));
                    entity.setPassword(rs.getString("user_pswd"));
                    entity.setCreate_date(rs.getDate("create_date"));
                    entity.setUpdate_date(rs.getDate("update_date"));
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

    public DBConnMaster find(DBConnMaster master) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = DBManager.getConnection();
            Select select = new Select();
            select.addField("*");
            select.addFrom(DB_MASTER);
            select.addWhere("master_code = ?", master.getMaster_code());

            stmt = con.prepareStatement(select.toQuery());
            setParameter(select, stmt);
            rs = stmt.executeQuery();

            if (rs.next()) {
                master.setMaster_code(rs.getString("master_code"));
                master.setCharacter_set(rs.getString("character_set"));
                master.setDb_type(rs.getString("db_type"));
                master.setDriverClass(rs.getString("class_name"));
                master.setJdbcUrl(rs.getString("url"));
                master.setUsername(rs.getString("user_id"));
                master.setPassword(rs.getString("user_pswd"));
                master.setCreate_date(rs.getDate("create_date"));
                master.setUpdate_date(rs.getDate("update_date"));
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

    public int insert(DBConnMaster master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            Insert sql = new Insert();
            sql.addField("master_code", master.getMaster_code());
            sql.addField("character_set", master.getCharacter_set());
            sql.addField("db_type", master.getDb_type());
            sql.addField("class_name", master.getDriverClass());
            sql.addField("url", master.getJdbcUrl());
            sql.addField("user_id", master.getUsername());
            sql.addField("user_pswd", master.getPassword());
            sql.addField("create_date", master.getCreate_date());
            sql.addField("update_date", master.getUpdate_date());
            sql.addFrom(DB_MASTER);

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

    public int update(DBConnMaster master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            Update sql = new Update();
            sql.addField("character_set", master.getCharacter_set());
            sql.addField("db_type", master.getDb_type());
            sql.addField("class_name", master.getDriverClass());
            sql.addField("url", master.getJdbcUrl());
            sql.addField("user_id", master.getUsername());
            sql.addField("user_pswd", master.getPassword());
            sql.addField("update_date", master.getUpdate_date());
            sql.addFrom(DB_MASTER);
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

    // Replaces setMigrationData to just queue/notify workers
    public void setMigrationData(MigrationList migList) {
        log.info("Queueing Migration Task: " + migList.getMig_list_seq());
        // For polling architecture, we just assume the Worker will find this task.
        // We typically update a status field here, but legacy schema uses execute_yn /
        // display_yn.
        // If Worker looks for execute_yn='Y' then it will pick it up.
        // But migration-proc.jsp is triggering this call.
        // If we want to support "Manual Run", we might need a dedicated STATUS table.
        // For this port, we'll log it.
        // TODO: Update STATUS column if exists.
    }

    public void startMigration(MigrationSchema migration) {
        log.info("Simulating startMigration via Worker Queue.");
    }
}
