package com.yojori.migration.controller.taglib;

import com.yojori.manager.CodeManager;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RadioTag extends TagSupport {
    private String name;
    private String list; // codeId
    private String checked;
    private String id;
    private String type;
    private String styleClass;

    public void setName(String name) { this.name = name; }
    public void setList(String list) { this.list = list; }
    public void setChecked(String checked) { this.checked = checked; }
    public void setId(String id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setStyleClass(String styleClass) { this.styleClass = styleClass; }

    @Override
    public int doStartTag() throws JspException {
        try {
            List<Map<String, String>> options = CodeManager.getCodeList(list);
            StringBuilder sb = new StringBuilder();
            
            boolean isButton = "button".equalsIgnoreCase(type);
            String btnClass = (styleClass != null && !styleClass.isEmpty()) ? styleClass : "btn-outline-secondary";

            if (isButton) {
                sb.append("<div class=\"btn-group\" role=\"group\">");
            }

            for (Map<String, String> opt : options) {
                String val = opt.get("value");
                String txt = opt.get("text");
                String chk = (val.equals(checked)) ? "checked" : "";
                
                if (isButton) {
                     String inputId = (id != null ? id : name) + "_" + val;
                     sb.append("<input type=\"radio\" class=\"btn-check\" name=\"").append(name != null ? name : "")
                       .append("\" id=\"").append(inputId).append("\" value=\"").append(val).append("\" ").append(chk).append(" autocomplete=\"off\">");
                     sb.append("<label class=\"btn ").append(btnClass).append("\" for=\"").append(inputId).append("\">")
                       .append(txt).append("</label>");
                } else {
                    sb.append(
                            "<input type='radio' name='" + name + "' value='" + val + "' " + chk + " /> " + txt + "&nbsp;");
                }
            }
            
            if (isButton) {
                sb.append("</div>");
            }

            pageContext.getOut().print(sb.toString());
        } catch (IOException e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }
}
