package com.example.crudform

import com.google.gson.JsonObject
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


data class EstadoMesa(
    val _id: String,
    val idMesa: String,
    val ocupada: Boolean
)

data class RespuestaEstadoMesa(
    val type: String,
    val message: String,
    val data: EstadoMesa,
)

data class RespuestaAllMenus(
    val type: String,
    val message: String,
    val data: List<SingleMenu>
)


@Parcelize
data class SingleMenu(
    val id: Number,
    val nombre: String,
    val precio: Number,
    val descripcion: String
) : Parcelable

data class PedidoData(
    val mesa: String,
    val pedidos: List<PedidoItem>
)

data class PedidoItem(
    val id: Number,
    val nombre: String,
    val precio: Number,
    val descripcion: String
)

data class RespuestaPedido(
    val type: String,
    val data: Any? = null
)