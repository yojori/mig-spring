<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
	<%@ page import="c.y.mig.util.*" %>
		<%@ page import="c.y.mig.manager.*" %>
			<%@ page import="c.y.mig.model.*" %>
				<%@ page import="java.util.*" %>
					<%@include file="/mig/session-admin-check.jsp" %>
						<% InsertTable search=(InsertTable) RequestUtils.getBean(request, InsertTable.class);
							InsertTableManager manager=new InsertTableManager(); if
							("insertAll".equals(search.getMode())) { String[]
							insert_table_seq=request.getParameterValues("insert_table_seq"); String[]
							source_table=request.getParameterValues("source_table"); String[]
							source_pk=request.getParameterValues("source_pk"); String[]
							target_table=request.getParameterValues("target_table"); for (int i=0; i <
							insert_table_seq.length; i++) { InsertTable entity=new InsertTable();
							entity.setMig_list_seq(search.getMig_list_seq()); entity.setSource_table(source_table[i]);
							entity.setSource_pk(source_pk[i]); entity.setTarget_table(target_table[i]);
							entity.setTruncate_yn("N"); String truncate_yn=request.getParameter("truncate_" +
							insert_table_seq[i]); if (truncate_yn !=null && "Y" .equals(truncate_yn)) {
							entity.setTruncate_yn("Y"); } if (source_table[i]==null || source_table[i].length()==0) {
							continue; } else if (insert_table_seq[i]==null || insert_table_seq[i].length()==0) { //
							insert entity.setInsert_table_seq(Config.getOrdNoSequence("IT")); entity.setCreate_date(new
							Date()); entity.setUpdate_date(new Date()); manager.insert(entity); } else if
							(insert_table_seq[i] !=null && insert_table_seq[i].length()> 0) { // update
							entity.setInsert_table_seq(insert_table_seq[i]);
							entity.setUpdate_date(new Date());

							manager.update(entity);
							}
							}
							} else if ("goDelete".equals(search.getMode())) {
							if (request.getParameter("auto_insert_table_seq") != null) {
							search.setInsert_table_seq(request.getParameter("auto_insert_table_seq"));
							manager.delete(search);
							}
							}

							List<InsertTable> list = manager.getList(search, InterfaceManager.LIST);

								// DB에서 불러오고 Target에 Table Create
								if ("createAll".equals(search.getMode())) {
								MigrationListManager mlm = new MigrationListManager();
								mlm.tableCreate(list);
								}

								request.setAttribute("search", search);
								request.setAttribute("list", list);

								MigrationListManager migManager = new MigrationListManager();
								MigrationList migList = new MigrationList();
								migList.setMig_list_seq(search.getMig_list_seq());
								migList = migManager.find(migList);
								request.setAttribute("migList", migList);

								request.getRequestDispatcher("./insert-table-write-fwd.jsp").forward(request, response);
								%>