package com.example.crudform

import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // devuelve todos los productos de menus
    @GET("get/readallmenus")
    fun cargarMenus(): Call<RespuestaAllMenus>

    // devuelve estado de ocupacion de la mesa
    @GET("get/readestadomesa")
    fun leerEstadoMesa(@Query("mesaId") mesaId: String): Call<RespuestaEstadoMesa>

    // manda pedido de mesa a restaurante
    @POST("post/mandarpedidodemesa")
    fun mandarPedido(@Body data: PedidoData): Call<RespuestaPedido>

    // lee pedidos de mesa
    @GET("get/readmesa")
    fun leerMesa(@Query("mesaId") mesaId: String): Call<RespuestaMesa>

    // borra pedidos de mesa
    @DELETE("delete/deletemesa")
    fun deleteMesa(@Query("mesaId") mesaId: String): Call<RespuestaDelete>

    // cambia estado de mesa
    @PATCH("patch/cambiarestadomesa")
    fun cambiarEstadoMesa(
        @Query("mesaId") mesaId: String,
        @Query("ocupada") ocupada: Boolean
    ): Call<RespuestaEstadoMesa>

}