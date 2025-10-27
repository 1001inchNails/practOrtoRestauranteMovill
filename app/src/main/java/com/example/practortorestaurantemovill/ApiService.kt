package com.example.crudform

import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("get/readallmenus")
    fun cargarMenus(): Call<RespuestaAllMenus>

    @GET("get/readestadomesa")
    fun leerEstadoMesa(@Query("mesaId") mesaId: String): Call<RespuestaEstadoMesa>

//    @POST("update")
//    fun actualizarFicha(@Body data: FichaUpdate): Call<Respuesta>
//
//    @POST("delete")
//    fun borrarFicha(@Body data: FichaDelete): Call<Respuesta>
//
//    @GET("readall")
//    fun verTodas(): Call<RespuestaAll>
}