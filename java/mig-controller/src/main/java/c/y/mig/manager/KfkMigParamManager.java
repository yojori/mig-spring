package c.y.mig.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import c.y.mig.db.DBManager;
import c.y.mig.db.query.Insert;
import c.y.mig.model.KfkMigParam;

public class KfkMigParamManager extends Manager {

    private static final Logger log = LoggerFactory.getLogger(KfkMigParamManager.class);
    private static final String KFK_MIG_PARAM = "KFK_MIG_PARAM";

    public int insert(KfkMigParam master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            Insert sql = new Insert();
            sql.addField("mig_list_seq", master.getMig_list_seq());
            sql.addField("connector_type", master.getConnector_type());
            sql.addField("param_key", master.getParam_key());
            sql.addField("param_value", master.getParam_value());
            sql.addField("dp_level", master.getDp_level());
            sql.addField("dp_order", master.getDp_order());
            sql.addFrom(KFK_MIG_PARAM);

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

    public List<KfkMigParam> getList(String mig_list_seq) {
        List<KfkMigParam> list = new java.util.ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBManager.getConnection();
            c.y.mig.db.query.Select sql = new c.y.mig.db.query.Select();
            sql.addField("*");
            sql.addFrom(KFK_MIG_PARAM);
            sql.addWhere("mig_list_seq = ?", mig_list_seq);
            
            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rs = stmt.executeQuery();

            while (rs.next()) {
                KfkMigParam entity = new KfkMigParam();
                entity.setParam_seq(rs.getInt("seq"));
                entity.setMig_list_seq(rs.getString("mig_list_seq"));
                entity.setConnector_type(rs.getString("connector_type"));
                entity.setParam_key(rs.getString("param_key"));
                entity.setParam_value(rs.getString("param_value"));
                entity.setDp_level(rs.getInt("dp_level"));
                entity.setDp_order(rs.getInt("dp_order"));
                entity.setCreate_date(rs.getTimestamp("create_date"));
                entity.setUpdate_date(rs.getTimestamp("update_date"));
                list.add(entity);
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

    public int delete(String mig_list_seq) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            c.y.mig.db.query.Delete sql = new c.y.mig.db.query.Delete();
            sql.addFrom(KFK_MIG_PARAM);
            sql.addWhere("mig_list_seq = ?", mig_list_seq);

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
