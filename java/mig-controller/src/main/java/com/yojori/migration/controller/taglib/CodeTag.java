package com.yojori.migration.controller.taglib;

import com.yojori.manager.CodeManager;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CodeTag extends TagSupport {
    private String id;
    private String name;
    private String param;
    private String selected;

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    private String defaultSelect;
    private String script;
    private String style;
    private String required;
    private String onchange;
    private String styleClass; // e.g. "form-select"

    public void setGroup(String group) {
        this.param = group; // Alias group to param
    }

    public void setOnchange(String onchange) {
        this.onchange = onchange;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            List<Map<String, String>> options = CodeManager.getCodeList(param);
            StringBuilder sb = new StringBuilder();
            
            String onchangeAttr = (onchange != null) ? " onchange=\"" + onchange + "\"" : "";
            String classAttr = (styleClass != null) ? " class=\"" + styleClass + "\"" : "";
            sb.append("<select name=\"" + (name != null ? name : "") + "\" id=\"" + (id != null ? id : "") + "\"" + onchangeAttr + classAttr + ">");

            for (Map<String, String> opt : options) {
                String val = opt.get("value");
                String txt = opt.get("text");
                String sel = (val.equals(selected)) ? "selected" : "";
                sb.append("<option value=\"" + val + "\" " + sel + ">" + txt + "</option>");
            }
            sb.append("</select>");

            pageContext.getOut().print(sb.toString());
        } catch (IOException e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }
}
