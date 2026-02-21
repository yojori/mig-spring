package c.y.mig.controller.taglib;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

public class PagingTag extends TagSupport {
    private int currentPage;
    private int pageSize;
    private int totalCount;
    private String linkUrl;

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    private String skin;
    private String prefix;

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            // Simple paging renderer
            // [Prev] [1] [2] [3] [Next]
            int totalPage = (int) Math.ceil((double) totalCount / pageSize);
            StringBuilder sb = new StringBuilder();

            for (int i = 1; i <= totalPage; i++) {
                if (i == currentPage) {
                    sb.append("<b>[" + i + "]</b> ");
                } else {
                    sb.append("<a href=\"" + linkUrl + "&currentPage=" + i + "\">[" + i + "]</a> ");
                }
            }
            pageContext.getOut().print(sb.toString());
        } catch (IOException e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }
}
