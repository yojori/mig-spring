package c.y.mig.controller.taglib;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

public class SubStringTag extends TagSupport {
    private String strText;
    private int length;

    public void setStrText(String strText) {
        this.strText = strText;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            if (strText == null)
                strText = "";
            if (length > 0 && strText.length() > length) {
                strText = strText.substring(0, length) + "...";
            }
            pageContext.getOut().print(strText);
        } catch (IOException e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }
}
