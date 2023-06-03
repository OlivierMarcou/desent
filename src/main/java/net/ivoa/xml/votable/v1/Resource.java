//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.02.11 at 11:48:30 PM CET 
//


package net.ivoa.xml.votable.v1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;


/**
 * 
 *      Added in Version 1.2: INFO for diagnostics in several places
 *   
 * 
 * <p>Java class for Resource complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Resource">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DESCRIPTION" type="{http://www.ivoa.net/xml/VOTable/v1.2}anyTEXT" minOccurs="0"/>
 *         &lt;element name="INFO" type="{http://www.ivoa.net/xml/VOTable/v1.2}Info" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="COOSYS" type="{http://www.ivoa.net/xml/VOTable/v1.2}CoordinateSystem"/>
 *           &lt;element name="GROUP" type="{http://www.ivoa.net/xml/VOTable/v1.2}Group"/>
 *           &lt;element name="PARAM" type="{http://www.ivoa.net/xml/VOTable/v1.2}Param"/>
 *         &lt;/choice>
 *         &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="LINK" type="{http://www.ivoa.net/xml/VOTable/v1.2}Link" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;choice>
 *             &lt;element name="TABLE" type="{http://www.ivoa.net/xml/VOTable/v1.2}Table"/>
 *             &lt;element name="RESOURCE" type="{http://www.ivoa.net/xml/VOTable/v1.2}Resource"/>
 *           &lt;/choice>
 *         &lt;/sequence>
 *         &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}token" />
 *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="utype" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="type" default="results">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *             &lt;enumeration value="results"/>
 *             &lt;enumeration value="meta"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Resource", propOrder = {
    "description",
    "info",
    "coosysOrGROUPOrPARAM",
    "linkAndTABLEOrRESOURCE",
    "any"
})
public class Resource implements Serializable {

    @XmlElement(name = "DESCRIPTION")
    protected AnyTEXT description;
    @XmlElement(name = "INFO")
    protected List<Info> info;
    @XmlElements({
        @XmlElement(name = "COOSYS", type = CoordinateSystem.class),
        @XmlElement(name = "GROUP", type = Group.class),
        @XmlElement(name = "PARAM", type = Param.class)
    })
    protected List<Object> coosysOrGROUPOrPARAM;
    @XmlElements({
        @XmlElement(name = "RESOURCE", type = Resource.class),
        @XmlElement(name = "LINK", type = Link.class),
        @XmlElement(name = "TABLE", type = Table.class)
    })
    protected List<Object> linkAndTABLEOrRESOURCE;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String name;
    @XmlAttribute(name = "ID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute
    protected String utype;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link AnyTEXT }
     *     
     */
    public AnyTEXT getDESCRIPTION() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link AnyTEXT }
     *     
     */
    public void setDESCRIPTION(AnyTEXT value) {
        this.description = value;
    }

    /**
     * Gets the value of the info property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the info property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getINFO().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Info }
     * 
     * 
     */
    public List<Info> getINFO() {
        if (info == null) {
            info = new ArrayList<Info>();
        }
        return this.info;
    }

    /**
     * Gets the value of the coosysOrGROUPOrPARAM property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the coosysOrGROUPOrPARAM property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCOOSYSOrGROUPOrPARAM().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CoordinateSystem }
     * {@link Group }
     * {@link Param }
     * 
     * 
     */
    public List<Object> getCOOSYSOrGROUPOrPARAM() {
        if (coosysOrGROUPOrPARAM == null) {
            coosysOrGROUPOrPARAM = new ArrayList<Object>();
        }
        return this.coosysOrGROUPOrPARAM;
    }

    /**
     * Gets the value of the linkAndTABLEOrRESOURCE property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkAndTABLEOrRESOURCE property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLINKAndTABLEOrRESOURCE().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Resource }
     * {@link Link }
     * {@link Table }
     * 
     * 
     */
    public List<Object> getLINKAndTABLEOrRESOURCE() {
        if (linkAndTABLEOrRESOURCE == null) {
            linkAndTABLEOrRESOURCE = new ArrayList<Object>();
        }
        return this.linkAndTABLEOrRESOURCE;
    }

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Element }
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setID(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the utype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUtype() {
        return utype;
    }

    /**
     * Sets the value of the utype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUtype(String value) {
        this.utype = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        if (type == null) {
            return "results";
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
