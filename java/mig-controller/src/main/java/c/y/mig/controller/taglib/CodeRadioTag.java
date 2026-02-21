package c.y.mig.controller.taglib;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import c.y.mig.manager.CodeManager;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

public class CodeRadioTag extends TagSupport {
    private String id;
    private String name;
    private String group;
    private String checked;
    private String defaultName;
    private String defaultValue;
    private String type; // "radio" (default) or "button"
    private String styleClass; // e.g. "btn-outline-primary"

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setChecked(String checked) {
        this.checked = checked;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            List<Map<String, String>> options = CodeManager.getCodeList(group);
            StringBuilder sb = new StringBuilder();
            boolean isButton = "button".equalsIgnoreCase(type);
            String btnClass = (styleClass != null && !styleClass.isEmpty()) ? styleClass : "btn-outline-primary";

            if (isButton) {
                sb.append("<div class=\"btn-group\" role=\"group\">");
            }

            if (defaultName != null) {
                String val = (defaultValue != null) ? defaultValue : "";
                String chk = ((val.equals(checked)) || (checked == null && val.isEmpty())) ? "checked" : "";
                
                if (isButton) {
                    // Unique ID generation for label: id_value (sanitize value)
                     String inputId = (id != null ? id : name) + "_default";
                     sb.append("<input type=\"radio\" class=\"btn-check\" name=\"").append(name != null ? name : "")
                       .append("\" id=\"").append(inputId).append("\" value=\"").append(val).append("\" ").append(chk).append(" autocomplete=\"off\">");
                     sb.append("<label class=\"btn ").append(btnClass).append("\" for=\"").append(inputId).append("\">")
                       .append(defaultName).append("</label>");
                } else {
                    sb.append("<input type=\"radio\" name=\"").append(name != null ? name : "")
                            .append("\" value=\"").append(val).append("\" ").append(chk).append("> ")
                            .append(defaultName).append("&nbsp;");
                }
            }

            if (options != null) {
                for (Map<String, String> opt : options) {
                    String val = opt.get("value");
                    String txt = opt.get("text");
                    String chk = (val != null && val.equals(checked)) ? "checked" : "";

                    if (isButton) {
                         String inputId = (id != null ? id : name) + "_" + val;
                         sb.append("<input type=\"radio\" class=\"btn-check\" name=\"").append(name != null ? name : "")
                           .append("\" id=\"").append(inputId).append("\" value=\"").append(val).append("\" ").append(chk).append(" autocomplete=\"off\">");
                         sb.append("<label class=\"btn ").append(btnClass).append("\" for=\"").append(inputId).append("\">")
                           .append(txt).append("</label>");
                    } else {
                        sb.append("<input type=\"radio\" name=\"").append(name != null ? name : "")
                                .append("\" value=\"").append(val).append("\" ").append(chk).append("> ")
                                .append(txt).append("&nbsp;");
                    }
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
