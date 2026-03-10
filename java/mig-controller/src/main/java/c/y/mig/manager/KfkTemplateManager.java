package c.y.mig.manager;

import c.y.mig.db.DBManager;
import c.y.mig.db.query.Insert;
import c.y.mig.db.query.Select;
import c.y.mig.db.query.Update;
import c.y.mig.model.KfkTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KfkTemplateManager extends Manager {

    private static final Logger log = LoggerFactory.getLogger(KfkTemplateManager.class);

    private static final String KFK_TEMPLATE = "KFK_TEMPLATE";

    public List<KfkTemplate> getList(KfkTemplate search) {
        List<KfkTemplate> list = new ArrayList<KfkTemplate>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBManager.getConnection();
            Select sql = new Select();
            sql.addField("*");
            sql.addFrom(KFK_TEMPLATE);
            sql.addOrder("ordering asc");
            
            if (search.getUse_yn() != null && !search.getUse_yn().isEmpty()) {
                sql.addWhere("use_yn = ?", search.getUse_yn());
            }

            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rs = stmt.executeQuery();

            while (rs.next()) {
                KfkTemplate entity = new KfkTemplate();
                entity.setTemplate_id(rs.getString("template_id"));
                entity.setTemplate_name(rs.getString("template_name"));
                entity.setTemplate_type(rs.getString("template_type"));
                entity.setConnector_class(rs.getString("connector_class"));
                entity.setDescription(rs.getString("description"));
                entity.setUse_yn(rs.getString("use_yn"));
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

    public KfkTemplate find(String template_id) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        KfkTemplate entity = null;

        try {
            con = DBManager.getConnection();
            Select sql = new Select();
            sql.addField("*");
            sql.addFrom(KFK_TEMPLATE);
            sql.addWhere("template_id = ?", template_id);

            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rs = stmt.executeQuery();

            if (rs.next()) {
                entity = new KfkTemplate();
                entity.setTemplate_id(rs.getString("template_id"));
                entity.setTemplate_name(rs.getString("template_name"));
                entity.setTemplate_type(rs.getString("template_type"));
                entity.setConnector_class(rs.getString("connector_class"));
                entity.setDescription(rs.getString("description"));
                entity.setUse_yn(rs.getString("use_yn"));
                entity.setOrdering(rs.getInt("ordering"));
                entity.setCreate_date(rs.getTimestamp("create_date"));
                entity.setUpdate_date(rs.getTimestamp("update_date"));
            }
        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(rs, stmt, con);
        }
        return entity;
    }

    public int insert(KfkTemplate master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            Insert sql = new Insert();
            sql.addField("template_id", master.getTemplate_id());
            sql.addField("template_name", master.getTemplate_name());
            sql.addField("template_type", master.getTemplate_type());
            sql.addField("connector_class", master.getConnector_class());
            sql.addField("description", master.getDescription());
            sql.addField("use_yn", master.getUse_yn());
            sql.addField("ordering", master.getOrdering());
            sql.addFrom(KFK_TEMPLATE);

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

    public int update(KfkTemplate master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            Update sql = new Update();
            sql.addField("template_name", master.getTemplate_name());
            sql.addField("template_type", master.getTemplate_type());
            sql.addField("connector_class", master.getConnector_class());
            sql.addField("description", master.getDescription());
            sql.addField("use_yn", master.getUse_yn());
            sql.addField("ordering", master.getOrdering());
            sql.addField("update_date", new java.util.Date()); // Simplified date handling
            sql.addFrom(KFK_TEMPLATE);
            sql.addWhere("template_id = ?", master.getTemplate_id());

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
