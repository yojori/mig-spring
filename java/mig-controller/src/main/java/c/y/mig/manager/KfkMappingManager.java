package c.y.mig.manager;

import c.y.mig.db.DBManager;
import c.y.mig.db.query.Insert;
import c.y.mig.db.query.Select;
import c.y.mig.db.query.Update;
import c.y.mig.model.KfkMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KfkMappingManager extends Manager {

    private static final Logger log = LoggerFactory.getLogger(KfkMappingManager.class);
    private static final String KFK_MAPPING = "KFK_MAPPING";

    public KfkMapping find(String mig_list_seq) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        KfkMapping entity = null;

        try {
            con = DBManager.getConnection();
            Select sql = new Select();
            sql.addField("*");
            sql.addFrom(KFK_MAPPING);
            sql.addWhere("mig_list_seq = ?", mig_list_seq);

            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rs = stmt.executeQuery();

            if (rs.next()) {
                entity = new KfkMapping();
                entity.setMig_list_seq(rs.getString("mig_list_seq"));
                entity.setMapping_name(rs.getString("mapping_name"));
                entity.setTransformation_json(rs.getString("transformation_json"));
                entity.setDescription(rs.getString("description"));
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

    public int insert(KfkMapping master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            Insert sql = new Insert();
            sql.addField("mig_list_seq", master.getMig_list_seq());
            sql.addField("mapping_name", master.getMapping_name());
            sql.addField("transformation_json", master.getTransformation_json());
            sql.addField("description", master.getDescription());
            sql.addFrom(KFK_MAPPING);

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

    public int update(KfkMapping master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            Update sql = new Update();
            sql.addField("mapping_name", master.getMapping_name());
            sql.addField("transformation_json", master.getTransformation_json());
            sql.addField("description", master.getDescription());
            sql.addField("update_date", new java.util.Date());
            sql.addFrom(KFK_MAPPING);
            sql.addWhere("mig_list_seq = ?", master.getMig_list_seq());

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
