package com.example.flutterdynamic.util

import com.google.gson.Gson
import java.lang.reflect.Type

fun <T> Gson.fromJsonProxy(json: String, classOfT: Class<T> ):T?{
    return try {
        this.fromJson(json, classOfT)
    }catch (e: java.lang.Exception){
        null
    }catch (e: Exception){
        null
    }
}

fun <T> Gson.fromJsonProxy(json: String, classOfT: Type):T?{
    return try {
        this.fromJson(json, classOfT)
    }catch (e: java.lang.Exception){
        null
    }catch (e: Exception){
        null
    }
}