package net.md_5.specialsource.dynamic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map.Entry;

import org.objectweb.asm.Type;

import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.NodeType;

public class DynamicReflection {
    public static boolean debug = false;
    private static JarMapping jarMapping;
    
    public static void setJarMapping(JarMapping mapping)
    {
        jarMapping = mapping;
    }
    
    public static Field getDeclaredField(Class<?> src, String name) throws NoSuchFieldException
    {
        if(jarMapping != null)
        {
            // TODO: Caching
            
            String className = Type.getInternalName(src);
            String oldClassName = getDeobfuscatedClassName(className);
                        
            String newName = jarMapping.tryClimb(jarMapping.fields, NodeType.FIELD, oldClassName, name);

            logR("DynamicReflection execute getDeclaredField!");
            logR("- class=" + className);
            
            if(newName != null)
            {
                logR("- " + name + " -> " + newName);
                
                return src.getDeclaredField(newName);
            }
        }
        
        return src.getDeclaredField(name);
    }

    public static Method getDeclaredMethod(Class<?> src, String name, Class<?>[] parameterTypes) throws NoSuchMethodException
    {
        if(jarMapping != null)
        {
            // TODO: Caching
            
            String className = Type.getInternalName(src);
            String oldClassName = getDeobfuscatedClassName(className);
            
            String descriptor = buildDescriptor(parameterTypes);

            String newName = searchMethod(oldClassName, name, descriptor);

            logR("DynamicReflection execute getDeclaredMethod!");
            logR("- class=" + className);
            
            if(newName != null)
            {
                logR("- " + name + " -> " + newName);
                
                return src.getDeclaredMethod(newName, parameterTypes);
            }
        }
        
        return src.getDeclaredMethod(name, parameterTypes);
    }
    
    private static String getDeobfuscatedClassName(String name)
    {
        if(jarMapping != null && jarMapping.classes.containsValue(name))
        {
            Iterator<Entry<String, String>> iter = jarMapping.classes.entrySet().iterator();
            
            while(iter.hasNext())
            {
                Entry<String, String> entry = iter.next();
                
                if(entry.getValue().equals(name))
                {
                    return entry.getKey();
                }
            }
        }
        
        return name;
    }
    
    private static String buildDescriptor(Class<?>[] types)
    {        
        String desc = "";
       
        for(Class<?> type : types)
        {
            desc += Type.getDescriptor(type);
        }
        
        return desc;
    }
    
    private static String searchMethod(String owner, String name, String paramDesc)
    {
        if(jarMapping != null)
        {
            String key = owner + "/" + name + " (" + paramDesc + ")";
            
            Iterator<Entry<String, String>> iterator = jarMapping.methods.entrySet().iterator();
            
            while(iterator.hasNext())
            {
                Entry<String, String> entry = iterator.next();
                
                if(entry.getKey().startsWith(key))
                {
                    return entry.getValue();
                }
            }
        }
        
        return null;
    }
    
    private static void logR(String message) {
        if(debug)
        {
            System.out.println("[DynamicReflection] " + message);
        }
    }
}
