package c.y.mig.manager;

import c.y.mig.db.DBManager;
import c.y.mig.db.query.Insert;
import c.y.mig.model.KfkMigList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KfkMigListManager extends Manager {

    private static final Logger log = LoggerFactory.getLogger(KfkMigListManager.class);
    private static final String KFK_MIG_LIST = "KFK_MIG_LIST";

    public int insert(KfkMigList master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Insert sql = new Insert();
            sql.addField("mig_list_seq", master.getMig_list_seq());
            sql.addField("mig_master", master.getMig_master());
            sql.addField("mig_name", master.getMig_name());
            sql.addField("registration_type", master.getRegistration_type());
            sql.addField("source_connector", master.getSource_connector());
            sql.addField("sink_connector", master.getSink_connector());
            sql.addField("use_yn", master.getUse_yn());
            sql.addFrom(KFK_MIG_LIST);

            con = DBManager.getConnection();
            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rtn = stmt.executeUpdate();

        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(rs, stmt, con);
        }
        return rtn;
    }

    public int saveParams(String migListSeq, String connectorType, String key, String value, int level, int order) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            String sql = "INSERT INTO KFK_MIG_PARAM (mig_list_seq, connector_type, param_key, param_value, dp_level, dp_order) " +
                         "VALUES (?, ?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE param_value = ?, dp_level = ?, dp_order = ?";
            
            con = DBManager.getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setString(1, migListSeq);
            stmt.setString(2, connectorType);
            stmt.setString(3, key);
            stmt.setString(4, value);
            stmt.setInt(5, level);
            stmt.setInt(6, order);
            stmt.setString(7, value);
            stmt.setInt(8, level);
            stmt.setInt(9, order);
            
            rtn = stmt.executeUpdate();
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(null, stmt, con);
        }
        return rtn;
    }

    public int deleteParams(String migListSeq) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            String sql = "DELETE FROM KFK_MIG_PARAM WHERE mig_list_seq = ?";
            con = DBManager.getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setString(1, migListSeq);
            rtn = stmt.executeUpdate();
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(null, stmt, con);
        }
        return rtn;
    }

    public KfkMigList getRecord(String migListSeq) {
        KfkMigList entity = null;
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT * FROM KFK_MIG_LIST WHERE mig_list_seq = ?";
            con = DBManager.getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setString(1, migListSeq);
            rs = stmt.executeQuery();
            if (rs.next()) {
                entity = new KfkMigList();
                entity.setMig_list_seq(rs.getString("mig_list_seq"));
                entity.setMig_master(rs.getString("mig_master"));
                entity.setMig_name(rs.getString("mig_name"));
                entity.setRegistration_type(rs.getString("registration_type"));
                entity.setSource_connector(rs.getString("source_connector"));
                entity.setSink_connector(rs.getString("sink_connector"));
                entity.setUse_yn(rs.getString("use_yn"));
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(rs, stmt, con);
        }
        return entity;
    }
}
