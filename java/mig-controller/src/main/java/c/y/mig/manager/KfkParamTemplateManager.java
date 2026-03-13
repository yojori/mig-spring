package c.y.mig.manager;

import c.y.mig.db.DBManager;
import c.y.mig.db.query.Insert;
import c.y.mig.db.query.Select;
import c.y.mig.db.query.Update;
import c.y.mig.model.KfkParamTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KfkParamTemplateManager extends Manager {

    private static final Logger log = LoggerFactory.getLogger(KfkParamTemplateManager.class);
    private static final String KFK_PARAM_TEMPLATE = "KFK_PARAM_TEMPLATE";

    public List<KfkParamTemplate> getList(int dp_level, String par_class_id) {
        List<KfkParamTemplate> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBManager.getConnection();
            Select sql = new Select();
            sql.addField("*");
            sql.addFrom(KFK_PARAM_TEMPLATE);
            sql.addWhere("dp_level = ?", dp_level);
            if (par_class_id != null && !"".equals(par_class_id)) {
                sql.addWhere("par_class_id = ?", par_class_id);
            }
            sql.addOrder("dp_order asc");

            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(populate(rs));
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

    public List<KfkParamTemplate> getAllLevelsList(String par_class_id) {
        List<KfkParamTemplate> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBManager.getConnection();
            Select sql = new Select();
            sql.addField("*");
            sql.addFrom(KFK_PARAM_TEMPLATE);
            sql.addWhere("dp_level > 0");
            if (par_class_id != null && !"".equals(par_class_id)) {
                sql.addWhere("par_class_id = ?", par_class_id);
            }
            sql.addOrder("dp_level asc, dp_order asc");

            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(populate(rs));
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

    public KfkParamTemplate find(String id) {
        KfkParamTemplate entity = null;
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBManager.getConnection();
            Select sql = new Select();
            sql.addField("*");
            sql.addFrom(KFK_PARAM_TEMPLATE);
            sql.addWhere("id = ?", id);

            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rs = stmt.executeQuery();

            if (rs.next()) {
                entity = populate(rs);
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

    public int insert(KfkParamTemplate master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            Insert sql = new Insert();
            sql.addField("id", master.getId());
            sql.addField("param_name", master.getParam_name());
            sql.addField("input_method", master.getInput_method());
            sql.addField("param_explain", master.getParam_explain());
            sql.addField("param_key", master.getParam_key());
            sql.addField("par_class_id", master.getPar_class_id());
            sql.addField("dp_level", master.getDp_level());
            sql.addField("dp_order", master.getDp_order());
            sql.addField("hidden_yn", master.getHidden_yn());
            sql.addField("group_cd", master.getGroup_cd());
            sql.addField("par_param_id", master.getPar_param_id());
            sql.addField("par_column_key", master.getPar_column_key());
            sql.addField("column_type", master.getColumn_type());
            sql.addField("required_yn", master.getRequired_yn());
            sql.addField("default_value", master.getDefault_value());
            sql.addFrom(KFK_PARAM_TEMPLATE);

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

    public int update(KfkParamTemplate master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            Update sql = new Update();
            sql.addFrom(KFK_PARAM_TEMPLATE);
            sql.addField("param_name", master.getParam_name());
            sql.addField("input_method", master.getInput_method());
            sql.addField("param_explain", master.getParam_explain());
            sql.addField("param_key", master.getParam_key());
            sql.addField("par_class_id", master.getPar_class_id());
            sql.addField("dp_level", master.getDp_level());
            sql.addField("dp_order", master.getDp_order());
            sql.addField("hidden_yn", master.getHidden_yn());
            sql.addField("group_cd", master.getGroup_cd());
            sql.addField("par_param_id", master.getPar_param_id());
            sql.addField("par_column_key", master.getPar_column_key());
            sql.addField("column_type", master.getColumn_type());
            sql.addField("required_yn", master.getRequired_yn());
            sql.addField("default_value", master.getDefault_value());
            sql.addWhere("id = ?", master.getId());

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

    private KfkParamTemplate populate(ResultSet rs) throws SQLException {
        KfkParamTemplate entity = new KfkParamTemplate();
        entity.setId(rs.getString("id"));
        entity.setParam_name(rs.getString("param_name"));
        entity.setInput_method(rs.getString("input_method"));
        entity.setParam_explain(rs.getString("param_explain"));
        entity.setParam_key(rs.getString("param_key"));
        entity.setPar_class_id(rs.getString("par_class_id"));
        entity.setDp_level(rs.getInt("dp_level"));
        entity.setDp_order(rs.getInt("dp_order"));
        entity.setHidden_yn(rs.getString("hidden_yn"));
        entity.setGroup_cd(rs.getString("group_cd"));
        entity.setPar_param_id(rs.getString("par_param_id"));
        entity.setPar_column_key(rs.getString("par_column_key"));
        entity.setColumn_type(rs.getString("column_type"));
        entity.setRequired_yn(rs.getString("required_yn"));
        entity.setDefault_value(rs.getString("default_value"));
        entity.setCreate_date(rs.getTimestamp("create_date"));
        entity.setUpdate_date(rs.getTimestamp("update_date"));
        return entity;
    }
}
