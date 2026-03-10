package c.y.mig.manager;

import c.y.mig.db.DBManager;
import c.y.mig.db.query.Insert;
import c.y.mig.db.query.Select;
import c.y.mig.db.query.Update;
import c.y.mig.model.KfkParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KfkParamManager extends Manager {

    private static final Logger log = LoggerFactory.getLogger(KfkParamManager.class);
    private static final String KFK_PARAM = "KFK_PARAM";

    public List<KfkParam> getList(String template_id) {
        List<KfkParam> list = new ArrayList<KfkParam>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBManager.getConnection();
            Select sql = new Select();
            sql.addField("*");
            sql.addFrom(KFK_PARAM);
            sql.addWhere("template_id = ?", template_id);
            sql.addOrder("ordering asc");

            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rs = stmt.executeQuery();

            while (rs.next()) {
                KfkParam entity = new KfkParam();
                entity.setTemplate_id(rs.getString("template_id"));
                entity.setParam_key(rs.getString("param_key"));
                entity.setParam_value(rs.getString("param_value"));
                entity.setDescription(rs.getString("description"));
                entity.setOrdering(rs.getInt("ordering"));
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

    public int insert(KfkParam master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            Insert sql = new Insert();
            sql.addField("template_id", master.getTemplate_id());
            sql.addField("param_key", master.getParam_key());
            sql.addField("param_value", master.getParam_value());
            sql.addField("description", master.getDescription());
            sql.addField("ordering", master.getOrdering());
            sql.addFrom(KFK_PARAM);

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

    public int delete(String template_id, String param_key) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            String sql = "DELETE FROM " + KFK_PARAM + " WHERE template_id = ? AND param_key = ?";
            con = DBManager.getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setString(1, template_id);
            stmt.setString(2, param_key);
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
