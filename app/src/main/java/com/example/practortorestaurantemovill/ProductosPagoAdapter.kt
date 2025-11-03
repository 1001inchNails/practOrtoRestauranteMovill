import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.crudform.PedidoItem
import com.example.practortorestaurantemovill.R

// para el modal de la factura
class ProductosPagoAdapter(private val productos: List<PedidoItem>) :
    RecyclerView.Adapter<ProductosPagoAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidad)
        val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecio)
        val tvSubtotal: TextView = itemView.findViewById(R.id.tvSubtotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_pago, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = productos[position]
        val precio = try {
            producto.precio.toDouble()
        } catch (e: Exception) {
            0.0
        }

        val cantidad = try {
            producto.cantidad.toInt()
        } catch (e: Exception) {
            1
        }

        val subtotal = precio * cantidad

        holder.tvNombre.text = producto.nombre
        holder.tvCantidad.text = "Cantidad: ${producto.cantidad}"
        holder.tvPrecio.text = "Precio: ${String.format("%.2f", precio)}€"
        holder.tvSubtotal.text = "Subtotal: ${String.format("%.2f", subtotal)}€"
    }

    override fun getItemCount() = productos.size
}