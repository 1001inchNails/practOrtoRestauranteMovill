package com.example.crudform

import com.google.gson.JsonObject
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// interfaces de repuesta para las llamadas a API
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
    var cantidad: Int = 1,
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
    val cantidad: Number,
    val descripcion: String,
    val haSidoServido: Boolean? = null
)

data class RespuestaPedido(
    val type: String,
    val data: Any? = null
)


data class RespuestaDelete(
    val type: String,
    val message: String
)

data class MesaDocument(
    @SerializedName("_id")
    val id: String? = null,

    @SerializedName("pedidos")
    val pedidos: List<PedidoItem>? = null,

    @SerializedName("haSidoServido")
    val haSidoServido: Boolean? = null
)

data class RespuestaMesa(
    @SerializedName("type")
    val type: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<MesaDocument>?
)