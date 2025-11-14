// GetCursOnDateXml.java
package ru.skillfactorydemo.tgbot.dto;

import lombok.Data;
import javax.xml.bind.annotation.*;  // ← ЭТОТ ИМПОРТ РАБОТАЕТ!
import javax.xml.datatype.XMLGregorianCalendar;

@XmlRootElement(name = "GetCursOnDateXML", namespace = "http://web.cbr.ru/")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class GetCursOnDateXml {
    @XmlElement(name = "On_date", required = true, namespace = "http://web.cbr.ru/")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar onDate;
}