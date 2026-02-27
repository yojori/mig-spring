package c.y.mig.manager;

import c.y.mig.db.DBManager;
import c.y.mig.db.query.Insert;
import c.y.mig.db.query.Select;
import c.y.mig.db.query.Update;
import c.y.mig.model.TypeMapping;
import c.y.mig.util.Config;
import c.y.mig.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TypeMappingManager extends Manager {

    private static final Logger log = LoggerFactory.getLogger(TypeMappingManager.class);

    private void buildListQuery(TypeMapping search) {
        Select sql = new Select();
        sql.addField("*");
        sql.addFrom("zxxmig_type_mapping");

        if (!StringUtil.empty(search.getSrc_db_type())) {
            sql.addWhere("src_db_type = ?", search.getSrc_db_type());
        }
        if (!StringUtil.empty(search.getTgt_db_type())) {
            sql.addWhere("tgt_db_type = ?", search.getTgt_db_type());
        }
        if (!StringUtil.empty(search.getUse_yn())) {
            sql.addWhere("use_yn = ?", search.getUse_yn());
        }

        sql.addOrder("src_db_type, priority, mapping_seq");
        setListQuery(sql);
    }

    private void buildCountQuery(TypeMapping search) {
        Select sql = new Select();
        sql.addField("COUNT(*)");
        sql.addFrom("zxxmig_type_mapping");
        setCountQuery(sql);
    }

    public List<TypeMapping> getList(TypeMapping search) {
        List<TypeMapping> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBManager.getConnection();
            setForm(search);
            buildCountQuery(search);
            buildListQuery(search);
            
            stmt = con.prepareStatement(getListQuery().toQuery());
            setParameter(getListQuery(), stmt);
            rs = stmt.executeQuery();

            while (rs.next()) {
                TypeMapping entity = new TypeMapping();
                entity.setMapping_seq(rs.getString("mapping_seq"));
                entity.setSrc_db_type(rs.getString("src_db_type"));
                entity.setSrc_type_name(rs.getString("src_type_name"));
                entity.setTgt_db_type(rs.getString("tgt_db_type"));
                entity.setTgt_type_name(rs.getString("tgt_type_name"));
                entity.setPriority(rs.getInt("priority"));
                entity.setUse_yn(rs.getString("use_yn"));
                entity.setCreate_date(rs.getTimestamp("create_date"));
                entity.setUpdate_date(rs.getTimestamp("update_date"));
                list.add(entity);
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(rs, stmt, con);
        }
        return list;
    }

    public TypeMapping find(String mappingSeq) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBManager.getConnection();
            Select select = new Select();
            select.addField("*");
            select.addFrom("zxxmig_type_mapping");
            select.addWhere("mapping_seq = ?", mappingSeq);

            stmt = con.prepareStatement(select.toQuery());
            setParameter(select, stmt);
            rs = stmt.executeQuery();

            if (rs.next()) {
                TypeMapping entity = new TypeMapping();
                entity.setMapping_seq(rs.getString("mapping_seq"));
                entity.setSrc_db_type(rs.getString("src_db_type"));
                entity.setSrc_type_name(rs.getString("src_type_name"));
                entity.setTgt_db_type(rs.getString("tgt_db_type"));
                entity.setTgt_type_name(rs.getString("tgt_type_name"));
                entity.setPriority(rs.getInt("priority"));
                entity.setUse_yn(rs.getString("use_yn"));
                entity.setCreate_date(rs.getTimestamp("create_date"));
                entity.setUpdate_date(rs.getTimestamp("update_date"));
                return entity;
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(rs, stmt, con);
        }
        return null;
    }

    public int insert(TypeMapping entity) {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            if (StringUtil.empty(entity.getMapping_seq())) {
                entity.setMapping_seq(Config.getOrdNoSequence("TM"));
            }
            entity.setCreate_date(new Date());
            entity.setUpdate_date(new Date());

            Insert sql = new Insert();
            sql.addField("mapping_seq", entity.getMapping_seq());
            sql.addField("src_db_type", entity.getSrc_db_type());
            sql.addField("src_type_name", entity.getSrc_type_name());
            sql.addField("tgt_db_type", entity.getTgt_db_type());
            sql.addField("tgt_type_name", entity.getTgt_type_name());
            sql.addField("priority", entity.getPriority());
            sql.addField("use_yn", entity.getUse_yn());
            sql.addField("create_date", entity.getCreate_date());
            sql.addField("update_date", entity.getUpdate_date());
            sql.addFrom("zxxmig_type_mapping");

            con = DBManager.getConnection();
            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            return stmt.executeUpdate();
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(null, stmt, con);
        }
        return 0;
    }

    public int update(TypeMapping entity) {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            entity.setUpdate_date(new Date());

            Update sql = new Update();
            sql.addField("src_db_type", entity.getSrc_db_type());
            sql.addField("src_type_name", entity.getSrc_type_name());
            sql.addField("tgt_db_type", entity.getTgt_db_type());
            sql.addField("tgt_type_name", entity.getTgt_type_name());
            sql.addField("priority", entity.getPriority());
            sql.addField("use_yn", entity.getUse_yn());
            sql.addField("update_date", entity.getUpdate_date());
            sql.addFrom("zxxmig_type_mapping");
            sql.addWhere("mapping_seq = ?", entity.getMapping_seq());

            con = DBManager.getConnection();
            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            return stmt.executeUpdate();
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(null, stmt, con);
        }
        return 0;
    }

    public int delete(String mappingSeq) {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = DBManager.getConnection();
            String sql = "DELETE FROM zxxmig_type_mapping WHERE mapping_seq = ?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, mappingSeq);
            return stmt.executeUpdate();
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(null, stmt, con);
        }
        return 0;
    }
}
