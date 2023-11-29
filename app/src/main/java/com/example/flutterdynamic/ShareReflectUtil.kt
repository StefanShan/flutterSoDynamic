package com.example.flutterdynamic

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.Arrays

object ShareReflectUtil {

    fun findField(instance: Any, name: String): Field {
        var clazz: Class<*>? = instance.javaClass
        while (clazz != null) {
            try {
                val field = clazz.getDeclaredField(name)
                if (!field.isAccessible) {
                    field.isAccessible = true
                }
                return field
            } catch (e: NoSuchFieldException) {
                // ignore and search next
            }
            clazz = clazz.superclass
        }
        throw NoSuchFieldException("Field " + name + " not found in " + instance.javaClass)
    }

    fun findMethod(instance: Any, name: String, vararg parameterTypes: Class<*>?): Method {
        var clazz: Class<*>? = instance.javaClass
        while (clazz != null) {
            try {
                val method = clazz.getDeclaredMethod(name, *parameterTypes)
                if (!method.isAccessible) {
                    method.isAccessible = true
                }
                return method
            } catch (e: NoSuchMethodException) {
                // ignore and search next
            }
            clazz = clazz.superclass
        }
        throw NoSuchMethodException(
            "Method "
                    + name
                    + " with parameters "
                    + Arrays.asList(*parameterTypes)
                    + " not found in " + instance.javaClass
        )
    }

}