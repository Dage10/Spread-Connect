package adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.daviddam.clickconnect.databinding.ItemPresentacioBinding
import com.daviddam.clickconnect.R
import models.Presentacio
import util.ImageExtension.loadImageOrDefault

class PresentacioAdapter(
    private var presentacions: List<Presentacio>,
    private val idUsuariLoguejat: String?,
    private val onEditar: (Presentacio) -> Unit,
    private val onEliminar: (Presentacio) -> Unit
) : RecyclerView.Adapter<PresentacioAdapter.PresentacioViewHolder>() {

    fun updateData(nous: List<Presentacio>) {
        presentacions = nous
        notifyDataSetChanged()
    }

    inner class PresentacioViewHolder(val binding: ItemPresentacioBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresentacioViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPresentacioBinding.inflate(inflater, parent, false)
        return PresentacioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PresentacioViewHolder, posicio: Int) {
        val presentacio = presentacions[posicio]
        val esDelMateixUsuari = idUsuariLoguejat == presentacio.id_usuari

        holder.binding.apply {
            tvUsuari.text = presentacio.nom_usuari ?: "Usuari"
            tvTitol.text = presentacio.titol
            tvDescripcio.text = presentacio.contingut_presentacio
            
            imgPresentacio.loadImageOrDefault(presentacio.imatge_url, R.drawable.avatar)
            imgPresentacio.visibility = if (presentacio.imatge_url.isNullOrEmpty()) View.GONE else View.VISIBLE

            btnEditar.visibility = if (esDelMateixUsuari) View.VISIBLE else View.GONE
            btnEliminar.visibility = if (esDelMateixUsuari) View.VISIBLE else View.GONE

            btnEditar.setOnClickListener { onEditar(presentacio) }
            btnEliminar.setOnClickListener { onEliminar(presentacio) }
        }
    }

    override fun getItemCount(): Int = presentacions.size
}
