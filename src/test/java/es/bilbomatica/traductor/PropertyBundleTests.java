package es.bilbomatica.traductor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import es.bilbomatica.test.logic.PropertyBundle;

@SpringBootTest
public class PropertyBundleTests {

    private PropertyBundle propertyBundle;

    @Before
    public void setUp() {
        propertyBundle = new PropertyBundle(15);
    }

    @Test
    public void testTryAddProperty() {
        assertTrue(propertyBundle.tryAddProperty("key1", "value1"));
        assertEquals(1, propertyBundle.getSize());
    }

    @Test
    public void testGetPropertyReturnsNullIfDoesntExist() {
        propertyBundle.tryAddProperty("key1", "value1");
        assertEquals("value1", propertyBundle.getProperty("key1"));
        assertNull(propertyBundle.getProperty("nonExistentKey"));
    }

    @Test
    public void testGetAllProperties() {
        propertyBundle.tryAddProperty("key1", "value1");
        propertyBundle.tryAddProperty("key2", "value2");

        Map<String, String> allProperties = propertyBundle.getAllProperties();
        assertEquals(2, allProperties.size());
        assertEquals("value1", allProperties.get("key1"));
        assertEquals("value2", allProperties.get("key2"));
    }

    @Test
    public void testGetRawText() {
        propertyBundle.tryAddProperty("key1", "value1");
        assertEquals("§value1", propertyBundle.getRawText());
    }

    @Test
    public void testTryAddPropertyReturnsFalseWhenExceedingCapacity() {
        assertFalse(propertyBundle.tryAddProperty("key1", "value123456789123456"));
        assertEquals(0, propertyBundle.getSize());
    }

    @Test
    public void testFromReplacingPropertiesExistInNewPropertyBundle() {
        PropertyBundle source = new PropertyBundle(15);
        source.tryAddProperty("key1", "value1");
        source.tryAddProperty("key2", "value2");

        PropertyBundle replaced = PropertyBundle.fromReplacing(source, "§new1§new2");

        assertEquals("§new1§new2", replaced.getRawText());
        assertEquals(2, replaced.getSize());
        
        assertEquals("new1", replaced.getProperty("key1"));
        assertEquals("new2", replaced.getProperty("key2"));
    }
}
