package com.yojori.model;

public class SelectColumn extends Search {
	
	private String column_seq;
	private String mig_list_seq;
	
	private String column_name;
	private String column_type;

	private int ordering;

	public String getColumn_seq() {
		return column_seq;
	}
	public void setColumn_seq(String column_seq) {
		this.column_seq = column_seq;
	}
	public String getMig_list_seq() {
		return mig_list_seq;
	}
	public void setMig_list_seq(String mig_list_seq) {
		this.mig_list_seq = mig_list_seq;
	}
	public String getColumn_name() {
		return column_name;
	}
	public void setColumn_name(String column_name) {
		this.column_name = column_name;
	}
	public String getColumn_type() {
		return column_type;
	}
	public void setColumn_type(String column_type) {
		this.column_type = column_type;
	}
	public int getOrdering() {
		return ordering;
	}
	public void setOrdering(int ordering) {
		this.ordering = ordering;
	}
}
