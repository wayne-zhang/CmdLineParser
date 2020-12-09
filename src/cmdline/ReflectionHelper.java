/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmdline;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Reflection helper class
 * 
 * @author Wayne Zhang
 */
public class ReflectionHelper {
    /**
     * Apply a value to the field of an object by reflection.
     * It tries setter first and falls back to field access if method is not defined.
     * 
     * @param app object 
     * @param fieldName field name
     * @param value field value
     */
    public static void applyValue(Object app, String fieldName, String value){
        Class<?> clazz = app.getClass();
        String methodName = "set" + 
                 Character.toUpperCase(fieldName.charAt(0)) + 
                 fieldName.substring(1);  
        
        if(value.isEmpty()){    // falg attribute
            // Assign value by setter first
            Method method = getMethod(clazz, methodName, boolean.class);
            if(method == null){
                method = getMethod(clazz, methodName, Boolean.class);
            }
            
            if(method != null){
                try {
                    method.invoke(app, true);
                } catch (Exception e){
                    throw buildException(method.getName(), e);
                }
            } else {
                // Assign value by direct field access if no setter
                String booleanFieldName = "is" + 
                    Character.toUpperCase(fieldName.charAt(0)) + 
                    fieldName.substring(1);                
                Field field = getField(clazz, booleanFieldName);
                if(field == null){
                    field = getField(clazz, fieldName);
                }
                
                if(field == null){
                    throw new RuntimeException("Field and method '" + 
                            fieldName +
                            "' not defined on class: " + 
                            clazz.getName()
                    );
                }
                
                try {
                    field.setAccessible(true);
                    field.set(app, true);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw buildException(field.getName(), e);
                }
            }
        } else {
            // assign value by setter
            Method method = getMethod(clazz, methodName, String.class);
            if(method != null){
                try {
                    method.invoke(app, value);
                } catch (Exception e){
                    throw buildException(method.getName(), e);
                }
            } else {
                // assign value by direct field access if no setter
                Field field = getField(clazz, fieldName);
                
                if(field == null){
                    throw new RuntimeException("Field and method '" + 
                            fieldName +
                            "' not defined on class: " + 
                            clazz.getName()
                    );
                }
                
                try {
                    field.setAccessible(true);
                    field.set(app, value);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw buildException(field.getName(), e);
                }
            }            
        }
    }
    
    private static RuntimeException buildException(String fieldName, Exception root){
        return new RuntimeException("Set argument by apply " + fieldName + " failed", root);
    }
    
    /**
     * Get method of a given class
     * 
     * @param clazz class of the method
     * @param methodName method name
     * @param params method parameters
     * @return method or null if not find
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params){
        try{
            return clazz.getDeclaredMethod(methodName, params);
        }catch(NoSuchMethodException | SecurityException e){
            return null;
        }
    }
    
    /**
     * Get a field of given class
     * 
     * @param clazz class of the field
     * @param fieldName field name
     * @return field or null of not find
     */
    public static Field getField(Class<?> clazz, String fieldName){
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (Exception e){
            return null;
        }
    }    
}
