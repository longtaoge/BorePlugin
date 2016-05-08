package entity;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Element {

    // constants
    private static final Pattern sIdPattern = Pattern.compile("@\\+?(android:)?id/([^$]+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern sValidityPattern = Pattern.compile("^([a-zA-Z_\\$][\\w\\$]*)$", Pattern.CASE_INSENSITIVE);
    public String id;
    public boolean isAndroidNS = false;
    public String nameFull; // element name with package
    public String name; // element name
    public String fieldName; // name of variable
    public boolean isValid = false;
    public boolean used = true;
    public boolean isClickable = false; // Button, view_having_clickable_attr etc.
    public boolean isEditText = false; // EditText
    public XmlTag xml;

    /**
     * Constructs new element
     *
     * @param name Class name of the view
     * @param id Value in android:id attribute
     * @throws IllegalArgumentException When the arguments are invalid
     */
    public Element(String name, String id, XmlTag xml) {
        // id
        final Matcher matcher = sIdPattern.matcher(id);
        if (matcher.find() && matcher.groupCount() > 1) {
            this.id = matcher.group(2);

            String androidNS = matcher.group(1);
            this.isAndroidNS = !(androidNS == null || androidNS.length() == 0);
        }

        if (this.id == null) {
            throw new IllegalArgumentException("Invalid format of view id");
        }

        // name
        String[] packages = name.split("\\.");
        if (packages.length > 1) {
            this.nameFull = name;
            this.name = packages[packages.length - 1];
        } else {
            this.nameFull = null;
            this.name = name;
        }

        this.xml = xml;
        this.fieldName = getFieldName();

        // clickable
        XmlAttribute clickable = xml.getAttribute("android:clickable", null);
        if(xml.getName().contains("RadioButton")) {
            // check
        } else if (xml.getName().contains("Button")
                || (clickable != null && clickable.getValue() != null && clickable.getValue().equals("true"))) {
            isClickable = true;
        }

        // isEditText
        isEditText = xml.getName().contains("EditText");
    }

    /**
     * Create full ID for using in layout XML files
     *
     * @return
     */
    public String getFullID() {
        StringBuilder fullID = new StringBuilder();
        String rPrefix;

        if (isAndroidNS) {
            rPrefix = "android.R.id.";
        } else {
            rPrefix = "R.id.";
        }

        fullID.append(rPrefix);
        fullID.append(id);

        return fullID.toString();
    }

    /**
     * Generate field name if it's not done yet
     *
     * @return
     */
    private String getFieldName() {
        String[] words = this.id.split("_");
        StringBuilder sb = new StringBuilder();
        sb.append(Utils.getPrefix());

        for (int i = 0; i < words.length; i++) {
            if (words[i].isEmpty()) {
                // fixing issues with double underscores - see issue #40
                continue;
            }
            String[] idTokens = words[i].split("\\.");
            char[] chars = idTokens[idTokens.length - 1].toCharArray();
            if (i > 0 || !Utils.isEmptyString(Utils.getPrefix())) {
                chars[0] = Character.toUpperCase(chars[0]);
            }

            sb.append(chars);
        }

        return sb.toString();

        // TODO
    //    return id;
    }

    /**
     * Check validity of field name
     *
     * @return
     */
    public boolean checkValidity() {
        Matcher matcher = sValidityPattern.matcher(fieldName);
        isValid = matcher.find();

        return isValid;
    }
}
