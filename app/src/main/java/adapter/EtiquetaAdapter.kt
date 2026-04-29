package adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.daviddam.spreadconnect.databinding.ItemEtiquetaBinding

class EtiquetaAdapter(
    private var llistaEtiquetes: List<String>,
    private val onEditar: (String) -> Unit,
    private val onEliminar: (String) -> Unit
) : RecyclerView.Adapter<EtiquetaAdapter.EtiquetaViewHolder>() {

    fun updateData(novaLlista: List<String>) {
        llistaEtiquetes = novaLlista
        notifyDataSetChanged()
    }

    inner class EtiquetaViewHolder(val binding: ItemEtiquetaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EtiquetaViewHolder {
        val binding = ItemEtiquetaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EtiquetaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EtiquetaViewHolder, posicio: Int) {
        val nom = llistaEtiquetes[posicio]
        holder.binding.btnArea.text = nom
        holder.binding.btnArea.setOnClickListener {
            onEditar(nom)
        }
        holder.binding.btnEliminarEtiqueta.setOnClickListener {
            onEliminar(nom)
        }
    }

    override fun getItemCount(): Int = llistaEtiquetes.size
}
