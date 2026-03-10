package c.y.mig.manager;

import c.y.mig.db.DBManager;
import c.y.mig.db.query.Insert;
import c.y.mig.db.query.Select;
import c.y.mig.db.query.Update;
import c.y.mig.model.KfkOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KfkOffsetManager extends Manager {

    private static final Logger log = LoggerFactory.getLogger(KfkOffsetManager.class);
    private static final String KFK_OFFSET = "KFK_OFFSET";

    public KfkOffset find(String mig_list_seq, String topic_name, int partition_id) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        KfkOffset entity = null;

        try {
            con = DBManager.getConnection();
            Select sql = new Select();
            sql.addField("*");
            sql.addFrom(KFK_OFFSET);
            sql.addWhere("mig_list_seq = ?", mig_list_seq);
            sql.addWhere("topic_name = ?", topic_name);
            sql.addWhere("partition_id = ?", partition_id);

            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rs = stmt.executeQuery();

            if (rs.next()) {
                entity = new KfkOffset();
                entity.setMig_list_seq(rs.getString("mig_list_seq"));
                entity.setTopic_name(rs.getString("topic_name"));
                entity.setPartition_id(rs.getInt("partition_id"));
                entity.setCurrent_offset(rs.getLong("current_offset"));
                entity.setLast_timestamp(rs.getString("last_timestamp"));
                entity.setLast_pk(rs.getString("last_pk"));
                entity.setConsumer_group(rs.getString("consumer_group"));
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

    public int upsert(KfkOffset master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement selStmt = null;
        PreparedStatement execStmt = null;
        ResultSet rs = null;

        try {
            con = DBManager.getConnection();
            
            // Check existence
            String checkSql = "SELECT 1 FROM " + KFK_OFFSET + " WHERE mig_list_seq = ? AND topic_name = ? AND partition_id = ?";
            selStmt = con.prepareStatement(checkSql);
            selStmt.setString(1, master.getMig_list_seq());
            selStmt.setString(2, master.getTopic_name());
            selStmt.setInt(3, master.getPartition_id());
            rs = selStmt.executeQuery();

            if (rs.next()) {
                // Update
                Update sql = new Update();
                sql.addField("current_offset", master.getCurrent_offset());
                sql.addField("last_timestamp", master.getLast_timestamp());
                sql.addField("last_pk", master.getLast_pk());
                sql.addField("consumer_group", master.getConsumer_group());
                sql.addField("update_date", new java.util.Date());
                sql.addFrom(KFK_OFFSET);
                sql.addWhere("mig_list_seq = ?", master.getMig_list_seq());
                sql.addWhere("topic_name = ?", master.getTopic_name());
                sql.addWhere("partition_id = ?", master.getPartition_id());
                
                execStmt = con.prepareStatement(sql.toQuery());
                setParameter(sql, execStmt);
            } else {
                // Insert
                Insert sql = new Insert();
                sql.addField("mig_list_seq", master.getMig_list_seq());
                sql.addField("topic_name", master.getTopic_name());
                sql.addField("partition_id", master.getPartition_id());
                sql.addField("current_offset", master.getCurrent_offset());
                sql.addField("last_timestamp", master.getLast_timestamp());
                sql.addField("last_pk", master.getLast_pk());
                sql.addField("consumer_group", master.getConsumer_group());
                sql.addFrom(KFK_OFFSET);
                
                execStmt = con.prepareStatement(sql.toQuery());
                setParameter(sql, execStmt);
            }
            rtn = execStmt.executeUpdate();

        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(rs, selStmt, null);
            DBManager.close(null, execStmt, con);
        }
        return rtn;
    }
}
