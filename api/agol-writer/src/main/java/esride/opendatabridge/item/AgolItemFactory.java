package esride.opendatabridge.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: nik
 * Date: 23.04.13
 * Time: 15:11
 * To change this template use File | Settings | File Templates.
 */
public class AgolItemFactory {

    ObjectMapper objectMapper;
    private static final Logger log = Logger.getLogger(AgolItemFactory.class.getName());
    Boolean _propertiesToStrings;

    /**
     * Constructor
     * @param propertiesToStrings
     */
    public AgolItemFactory(Boolean propertiesToStrings) {
        _propertiesToStrings = propertiesToStrings;
        objectMapper = new ObjectMapper();
    }

    /**
     * Create ArcGIS Online item from Esri JSON
     * @param agolJsonItem
     * @return
     */
    public AgolItem createAgolItem(String agolJsonItem) {
        HashMap agolItemProperties = agolJsonToHashMap(agolJsonItem);
        AgolItem agolItem = new AgolItem(agolItemProperties);
        return agolItem;
    }
    /**
     * Create ArcGIS Online Item from HashMap
     * @param agolItemProperties
     * @return
     */
    public AgolItem createAgolItem(HashMap agolItemProperties) {
        if (_propertiesToStrings) {
            agolItemProperties = cleanAgolItemProperties(agolItemProperties);
        }
        AgolItem agolItem = new AgolItem(agolItemProperties);
        return agolItem;
    }
    /**
     * Transform Esri JSON to Hash Map
     * @param agolJsonItem
     * @return
     */
    private HashMap<String,String> agolJsonToHashMap(String agolJsonItem) {
        HashMap agolItemProperties = new HashMap();
        try {
            agolItemProperties = objectMapper.readValue(agolJsonItem, HashMap.class);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (_propertiesToStrings) {
            agolItemProperties = cleanAgolItemProperties(agolItemProperties);
        }

        return agolItemProperties;
    }

    // ToDo:
    // - List of valid keys for item upload. Throw out invalid keys.
    // - Define keys that are obligatory to prevent cases like this AgolItem: error={code=400, messageCode=CONT_0001, message=Item '2C2cc78b3b57e64967aae845b937e92637' does not exist or is inaccessible., details=}
    /**
     * Clean a HashMap of ArcGIS Online Item from properties that are not accepted when uploading an item, from properties with null values and transform all values to Strings
     * @param agolItemProperties
     * @return
     */
    private HashMap cleanAgolItemProperties(HashMap agolItemProperties) {
        HashMap deleteAgolItemProperties = new HashMap();
        HashMap updateAgolItemProperties = new HashMap();

        // Altering objects in a HashMap while iterating through it leads to null pointer errors - so we do everything in single loops.
        Iterator findNullItemsIterator = agolItemProperties.entrySet().iterator();
        int iteCounter = 0;
        long startTime = System.currentTimeMillis();
        while (findNullItemsIterator.hasNext()) {
            Map.Entry property = (Map.Entry) findNullItemsIterator.next();
            Object propertyValue = property.getValue();
            if (propertyValue == null) {
                deleteAgolItemProperties.put(property.getKey(), propertyValue);
            }
            iteCounter++;
        }
        Iterator removeNullItemsIterator = deleteAgolItemProperties.entrySet().iterator();
        while (removeNullItemsIterator.hasNext()) {
            Map.Entry property = (Map.Entry) removeNullItemsIterator.next();
            agolItemProperties.remove(property.getKey());
            if (log.isInfoEnabled()) {
                log.info("Entry \"" + property.getKey() + "\" with null value removed.");
            }
            iteCounter++;
        }
        Iterator findUpdatePropertiesIterator = agolItemProperties.entrySet().iterator();
        while (findUpdatePropertiesIterator.hasNext()) {
            Map.Entry property = (Map.Entry) findUpdatePropertiesIterator.next();
            Object propertyValue = property.getValue();
            if (propertyValue!=null) {
                Class propertyValueClass = propertyValue.getClass();
                if (!propertyValueClass.equals(String.class)) {
                    String stringPropertyValue = propertyValue.toString().replaceAll("\\[", "").replaceAll("\\]", "");
                    updateAgolItemProperties.put(property.getKey(), stringPropertyValue);
                    if (log.isInfoEnabled()) {
                        log.info(propertyValueClass.toString() + " value of key \"" + property.getKey() + "\" transformed to String value \"" + stringPropertyValue + "\"");
                    }
                }
            }
            iteCounter++;
        }
        Iterator updatePropertiesIterator = updateAgolItemProperties.entrySet().iterator();
        while (updatePropertiesIterator.hasNext()) {
            Map.Entry updateProperty = (Map.Entry) updatePropertiesIterator.next();
            agolItemProperties.remove(updateProperty.getKey());
            agolItemProperties.put(updateProperty.getKey(), updateProperty.getValue());
            iteCounter++;
        }
        if (log.isInfoEnabled()) {
            log.info(iteCounter + " iterations performed in " + (System.currentTimeMillis()-startTime) + " ms.");
        }

        return agolItemProperties;
    }
}